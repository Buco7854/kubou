package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.OpenTriviaDbResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenTriviaDbProviderTest {

    private OpenTriviaDbProvider provider;

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
        provider = new OpenTriviaDbProvider(restClient);
    }

    @Test
    void shouldReturnCorrectSourceIdentifier() {
        assertEquals("opentdb", provider.getSourceIdentifier());
    }

    @Test
    void shouldReturnEnglishSourceLanguage() {
        assertEquals("en", provider.getSourceLanguage());
    }

    @Test
    void shouldMapValidMultipleChoiceQuestion() {
        // Given
        OpenTriviaDbResponse.TriviaItem item = new OpenTriviaDbResponse.TriviaItem();
        item.setQuestion(encode("What is the capital of France?"));
        item.setCorrectAnswer(encode("Paris"));
        item.setIncorrectAnswers(List.of(
                encode("London"),
                encode("Berlin"),
                encode("Madrid")
        ));
        item.setCategory(encode("Geography"));
        item.setDifficulty(encode("easy"));
        item.setType("multiple");

        OpenTriviaDbResponse response = new OpenTriviaDbResponse();
        response.setResponseCode(0);
        response.setResults(List.of(item));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1));

        // Then
        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertEquals("What is the capital of France?", q.getText());
        assertEquals(4, q.getOptions().size());
        assertTrue(q.getOptions().contains("Paris"));
        assertTrue(q.getOptions().contains("London"));
        assertTrue(q.getOptions().contains("Berlin"));
        assertTrue(q.getOptions().contains("Madrid"));
        assertEquals("Paris", q.getOptions().get(q.getCorrectAnswerIndex()));
        assertTrue(q.getTags().contains("Geography"));
        assertEquals(1, q.getDifficultyLevel());
    }

    @Test
    void shouldMapDifficultyCorrectly() {
        // Given
        OpenTriviaDbResponse response = new OpenTriviaDbResponse();
        response.setResponseCode(0);
        response.setResults(List.of(
                createItem("easy"),
                createItem("medium"),
                createItem("hard")
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
    void shouldReturnEmptyListWhenResponseCodeIsNotZero() {
        // Given
        OpenTriviaDbResponse response = new OpenTriviaDbResponse();
        response.setResponseCode(1); // No results
        response.setResults(List.of());

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1));

        // Then
        assertTrue(questions.isEmpty());
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

    @Test
    void shouldThrowExternalServiceExceptionOnApiFailure() {
        // Given
        when(restClient.get()).thenThrow(new RuntimeException("Connection refused"));

        // When / Then
        assertThrows(ExternalServiceException.class,
                () -> provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1)));
    }

    @Test
    void shouldDecodeUrlEncodedStrings() {
        // Given
        OpenTriviaDbResponse.TriviaItem item = new OpenTriviaDbResponse.TriviaItem();
        item.setQuestion(encode("Don't forget that \u03C0 = 3.14 & doesn't equal 3."));
        item.setCorrectAnswer(encode("True"));
        item.setIncorrectAnswers(List.of(
                encode("False"),
                encode("Maybe"),
                encode("N/A")
        ));
        item.setCategory(encode("Science & Nature"));
        item.setDifficulty(encode("medium"));
        item.setType("multiple");

        OpenTriviaDbResponse response = new OpenTriviaDbResponse();
        response.setResponseCode(0);
        response.setResults(List.of(item));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(Class.class))).thenReturn(response);

        // When
        List<Question> questions = provider.fetchQuestions(new QuestionProviderRequest(List.of(), 1));

        // Then
        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertTrue(q.getText().contains("\u03C0"));
        assertTrue(q.getText().contains("&"));
        assertTrue(q.getTags().contains("Science & Nature"));
    }

    private OpenTriviaDbResponse.TriviaItem createItem(String difficulty) {
        OpenTriviaDbResponse.TriviaItem item = new OpenTriviaDbResponse.TriviaItem();
        item.setQuestion(encode("Q?"));
        item.setCorrectAnswer(encode("Correct"));
        item.setIncorrectAnswers(List.of(encode("Wrong1"), encode("Wrong2"), encode("Wrong3")));
        item.setCategory(encode("Test"));
        item.setDifficulty(encode(difficulty));
        item.setType("multiple");
        return item;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
