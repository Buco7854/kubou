package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenAiQuestionProviderTest {

    private OpenAiQuestionProvider provider;

    @Mock
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        provider = new OpenAiQuestionProvider(restClient);
    }

    @Test
    void shouldReturnCorrectSourceIdentifier() {
        assertEquals("openai", provider.getSourceIdentifier());
    }

    @Test
    void shouldReturnNullSourceLanguage() {
        assertNull(provider.getSourceLanguage());
    }

    @Test
    void shouldThrowWhenTextIsNull() {
        QuestionProviderRequest request = new QuestionProviderRequest(List.of(), 5, "en", null);
        assertThrows(IllegalArgumentException.class, () -> provider.fetchQuestions(request));
    }

    @Test
    void shouldThrowWhenTextIsBlank() {
        QuestionProviderRequest request = new QuestionProviderRequest(List.of(), 5, "en", "   ");
        assertThrows(IllegalArgumentException.class, () -> provider.fetchQuestions(request));
    }

    @Test
    void shouldParseValidJsonResponse() {
        // Given
        String openAiResponse = """
            {
              "id": "chatcmpl-123",
              "object": "chat.completion",
              "choices": [{
                "index": 0,
                "message": {
                  "role": "assistant",
                  "content": "[{\\"question\\":\\"What is the capital of France?\\",\\"options\\":[\\"London\\",\\"Paris\\",\\"Berlin\\",\\"Madrid\\"],\\"correctAnswerIndex\\":1,\\"difficulty\\":1},{\\"question\\":\\"What is 2+2?\\",\\"options\\":[\\"3\\",\\"4\\",\\"5\\",\\"6\\"],\\"correctAnswerIndex\\":1,\\"difficulty\\":1}]"
                },
                "finish_reason": "stop"
              }]
            }
            """;

        // When
        List<Question> questions = provider.parseResponse(openAiResponse);

        // Then
        assertEquals(2, questions.size());
        Question q1 = questions.get(0);
        assertEquals("What is the capital of France?", q1.getText());
        assertEquals(4, q1.getOptions().size());
        assertEquals(1, q1.getCorrectAnswerIndex());
        assertEquals("Paris", q1.getOptions().get(q1.getCorrectAnswerIndex()));
        assertEquals(1, q1.getDifficultyLevel());

        Question q2 = questions.get(1);
        assertEquals("What is 2+2?", q2.getText());
        assertEquals(1, q2.getCorrectAnswerIndex());
    }

    @Test
    void shouldParseResponseWithMarkdownCodeBlocks() {
        // Given
        String openAiResponse = """
            {
              "choices": [{
                "message": {
                  "content": "```json\\n[{\\"question\\":\\"Test?\\",\\"options\\":[\\"A\\",\\"B\\",\\"C\\",\\"D\\"],\\"correctAnswerIndex\\":0,\\"difficulty\\":2}]\\n```"
                }
              }]
            }
            """;

        // When
        List<Question> questions = provider.parseResponse(openAiResponse);

        // Then
        assertEquals(1, questions.size());
        assertEquals("Test?", questions.get(0).getText());
        assertEquals(0, questions.get(0).getCorrectAnswerIndex());
        assertEquals(2, questions.get(0).getDifficultyLevel());
    }

    @Test
    void shouldThrowExternalServiceExceptionOnInvalidJson() {
        // Given
        String invalidResponse = "not valid json";

        // When / Then
        assertThrows(ExternalServiceException.class, () -> provider.parseResponse(invalidResponse));
    }

    @Test
    void shouldThrowExternalServiceExceptionOnMissingChoices() {
        // Given
        String response = """
            {
              "id": "chatcmpl-123",
              "choices": []
            }
            """;

        // When / Then
        assertThrows(ExternalServiceException.class, () -> provider.parseResponse(response));
    }

    @Test
    void shouldHandleDifficultyLevels() {
        // Given
        String openAiResponse = """
            {
              "choices": [{
                "message": {
                  "content": "[{\\"question\\":\\"Easy?\\",\\"options\\":[\\"A\\",\\"B\\",\\"C\\",\\"D\\"],\\"correctAnswerIndex\\":0,\\"difficulty\\":1},{\\"question\\":\\"Hard?\\",\\"options\\":[\\"A\\",\\"B\\",\\"C\\",\\"D\\"],\\"correctAnswerIndex\\":2,\\"difficulty\\":3}]"
                }
              }]
            }
            """;

        // When
        List<Question> questions = provider.parseResponse(openAiResponse);

        // Then
        assertEquals(2, questions.size());
        assertEquals(1, questions.get(0).getDifficultyLevel());
        assertEquals(3, questions.get(1).getDifficultyLevel());
    }

    @Test
    void shouldReturnEmptyTagsForGeneratedQuestions() {
        // Given
        String openAiResponse = """
            {
              "choices": [{
                "message": {
                  "content": "[{\\"question\\":\\"Q?\\",\\"options\\":[\\"A\\",\\"B\\",\\"C\\",\\"D\\"],\\"correctAnswerIndex\\":0,\\"difficulty\\":1}]"
                }
              }]
            }
            """;

        // When
        List<Question> questions = provider.parseResponse(openAiResponse);

        // Then
        assertEquals(1, questions.size());
        assertTrue(questions.get(0).getTags().isEmpty());
    }
}
