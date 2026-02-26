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
import org.springframework.web.client.RestClient;

import java.util.List;

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
    void shouldMapValidQuestion() {
        // Given
        QuizApiResponse.BrowseQuestion item = new QuizApiResponse.BrowseQuestion();
        item.setText("What is Linux?");
        item.setDifficulty("Easy");
        item.setCategory("linux");
        item.setTags(List.of("Linux"));

        QuizApiResponse.Answer a1 = new QuizApiResponse.Answer();
        a1.setText("An OS");
        a1.setCorrect(true);
        QuizApiResponse.Answer a2 = new QuizApiResponse.Answer();
        a2.setText("A browser");
        a2.setCorrect(false);
        QuizApiResponse.Answer a3 = new QuizApiResponse.Answer();
        a3.setText("A database");
        a3.setCorrect(false);
        item.setAnswers(List.of(a1, a2, a3));

        QuizApiResponse response = new QuizApiResponse();
        response.setSuccess(true);
        response.setData(List.of(item));

        mockRestClient(response);

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
    void shouldSkipQuestionsWithNoCorrectAnswer() {
        // Given
        QuizApiResponse.BrowseQuestion noCorrect = new QuizApiResponse.BrowseQuestion();
        noCorrect.setText("Bad question?");
        QuizApiResponse.Answer a1 = new QuizApiResponse.Answer();
        a1.setText("A");
        a1.setCorrect(false);
        QuizApiResponse.Answer a2 = new QuizApiResponse.Answer();
        a2.setText("B");
        a2.setCorrect(false);
        noCorrect.setAnswers(List.of(a1, a2));

        QuizApiResponse.BrowseQuestion valid = createSimpleItem("Valid?", "Medium");

        QuizApiResponse response = new QuizApiResponse();
        response.setSuccess(true);
        response.setData(List.of(noCorrect, valid));

        mockRestClient(response);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 2));

        // Then
        assertEquals(1, questions.size());
        assertEquals("Valid?", questions.get(0).getText());
    }

    @Test
    void shouldMapDifficultyCorrectly() {
        // Given
        QuizApiResponse response = new QuizApiResponse();
        response.setSuccess(true);
        response.setData(List.of(
                createSimpleItem("Easy Q", "Easy"),
                createSimpleItem("Medium Q", "Medium"),
                createSimpleItem("Hard Q", "Hard")
        ));

        mockRestClient(response);

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

    private QuizApiResponse.BrowseQuestion createSimpleItem(String text, String difficulty) {
        QuizApiResponse.BrowseQuestion item = new QuizApiResponse.BrowseQuestion();
        item.setText(text);
        item.setDifficulty(difficulty);

        QuizApiResponse.Answer a1 = new QuizApiResponse.Answer();
        a1.setText("A");
        a1.setCorrect(true);
        QuizApiResponse.Answer a2 = new QuizApiResponse.Answer();
        a2.setText("B");
        a2.setCorrect(false);
        item.setAnswers(List.of(a1, a2));

        return item;
    }

    private void mockRestClient(QuizApiResponse response) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(QuizApiResponse.class)).thenReturn(response);
    }
}
