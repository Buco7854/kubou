package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.QuizApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizApiProviderTest {

    private QuizApiProvider provider;

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        provider = new QuizApiProvider(restClient, "test-api-key");
    }

    @Test
    void shouldReturnCorrectSourceIdentifier() {
        assertEquals("quizapi", provider.getSourceIdentifier());
    }

    @Test
    void shouldMapValidSingleAnswerQuestion() {
        // Given
        QuizApiResponse response = new QuizApiResponse();
        response.setQuestion("What is Linux?");
        response.setMultiple_correct_answers("false");
        response.setAnswers(Map.of(
                "answer_a", "An OS",
                "answer_b", "A browser",
                "answer_c", "A database"
        ));
        response.setCorrect_answers(Map.of(
                "answer_a_correct", "true",
                "answer_b_correct", "false",
                "answer_c_correct", "false"
        ));
        response.setDifficulty("Easy");
        response.setCategory("linux");

        QuizApiResponse.Tag tag = new QuizApiResponse.Tag();
        tag.setName("Linux");
        response.setTags(List.of(tag));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(response));

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1));

        // Then
        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertEquals("What is Linux?", q.getText());
        assertEquals(3, q.getOptions().size());
        assertEquals(0, q.getCorrectAnswerIndex());
        assertEquals("An OS", q.getOptions().get(0));
        assertEquals(1, q.getDifficultyLevel());
        assertTrue(q.getTags().contains("Linux"));
        assertTrue(q.getTags().contains("linux"));
    }

    @Test
    void shouldSkipMultipleCorrectAnswerQuestions() {
        // Given
        QuizApiResponse multiAnswer = new QuizApiResponse();
        multiAnswer.setQuestion("Multi answer?");
        multiAnswer.setMultiple_correct_answers("true");
        multiAnswer.setAnswers(Map.of("answer_a", "A", "answer_b", "B"));
        multiAnswer.setCorrect_answers(Map.of("answer_a_correct", "true", "answer_b_correct", "true"));

        QuizApiResponse singleAnswer = new QuizApiResponse();
        singleAnswer.setQuestion("Single answer?");
        singleAnswer.setMultiple_correct_answers("false");
        singleAnswer.setAnswers(Map.of("answer_a", "A", "answer_b", "B"));
        singleAnswer.setCorrect_answers(Map.of("answer_a_correct", "false", "answer_b_correct", "true"));
        singleAnswer.setDifficulty("Medium");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(multiAnswer, singleAnswer));

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 2));

        // Then
        assertEquals(1, questions.size());
        assertEquals("Single answer?", questions.get(0).getText());
        assertEquals(1, questions.get(0).getCorrectAnswerIndex());
    }

    @Test
    void shouldMapDifficultyCorrectly() {
        // Given
        QuizApiResponse easy = createSimpleResponse("Easy");
        QuizApiResponse medium = createSimpleResponse("Medium");
        QuizApiResponse hard = createSimpleResponse("Hard");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of(easy, medium, hard));

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 3));

        // Then
        assertEquals(3, questions.size());
        assertEquals(1, questions.get(0).getDifficultyLevel());
        assertEquals(2, questions.get(1).getDifficultyLevel());
        assertEquals(3, questions.get(2).getDifficultyLevel());
    }

    @Test
    void shouldThrowExternalServiceExceptionOnApiFailure() {
        // Given
        when(restClient.get()).thenThrow(new RuntimeException("Connection refused"));

        // When / Then
        assertThrows(ExternalServiceException.class,
                () -> provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1)));
    }

    private QuizApiResponse createSimpleResponse(String difficulty) {
        QuizApiResponse r = new QuizApiResponse();
        r.setQuestion("Q?");
        r.setMultiple_correct_answers("false");
        r.setAnswers(Map.of("answer_a", "A", "answer_b", "B"));
        r.setCorrect_answers(Map.of("answer_a_correct", "true", "answer_b_correct", "false"));
        r.setDifficulty(difficulty);
        return r;
    }
}
