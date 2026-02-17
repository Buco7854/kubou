package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.QuizzApiResponse;
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
class QuizzApiProviderTest {

    private QuizzApiProvider provider;

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
        provider = new QuizzApiProvider(restClient);
    }

    @Test
    void shouldReturnCorrectSourceIdentifier() {
        assertEquals("quizzapi", provider.getSourceIdentifier());
    }

    @Test
    void shouldMapQuestionWithShuffledOptions() {
        // Given
        QuizzApiResponse.QuizzItem item = new QuizzApiResponse.QuizzItem();
        item.setQuestion("Quel est le plus grand océan ?");
        item.setAnswer("Pacifique");
        item.setBadAnswers(List.of("Atlantique", "Indien", "Arctique"));
        item.setCategory("geographie");
        item.setDifficulty("facile");

        QuizzApiResponse response = new QuizzApiResponse();
        response.setCount(1);
        response.setQuizzes(List.of(item));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of("geographie"), 1));

        // Then
        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertEquals("Quel est le plus grand océan ?", q.getText());
        assertEquals(4, q.getOptions().size());
        assertTrue(q.getOptions().contains("Pacifique"));
        assertTrue(q.getOptions().contains("Atlantique"));
        assertTrue(q.getOptions().contains("Indien"));
        assertTrue(q.getOptions().contains("Arctique"));
        assertEquals("Pacifique", q.getOptions().get(q.getCorrectAnswerIndex()));
        assertTrue(q.getTags().contains("geographie"));
        assertEquals(1, q.getDifficultyLevel());
    }

    @Test
    void shouldMapDifficultyCorrectly() {
        // Given
        QuizzApiResponse response = new QuizzApiResponse();
        response.setCount(3);
        response.setQuizzes(List.of(
                createItem("facile"),
                createItem("normal"),
                createItem("difficile")
        ));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response);

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

    @Test
    void shouldReturnEmptyListWhenResponseIsNull() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(null);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1));

        // Then
        assertTrue(questions.isEmpty());
    }

    private QuizzApiResponse.QuizzItem createItem(String difficulty) {
        QuizzApiResponse.QuizzItem item = new QuizzApiResponse.QuizzItem();
        item.setQuestion("Q?");
        item.setAnswer("Correct");
        item.setBadAnswers(List.of("Wrong1", "Wrong2", "Wrong3"));
        item.setCategory("test");
        item.setDifficulty(difficulty);
        return item;
    }
}
