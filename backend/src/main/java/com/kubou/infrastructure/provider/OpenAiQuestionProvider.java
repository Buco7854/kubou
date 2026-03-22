package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "openai.api-key", matchIfMissing = false)
public class OpenAiQuestionProvider implements QuestionProvider {

    private static final String SOURCE_ID = "openai";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.model}")
    private String model;

    public OpenAiQuestionProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getSourceIdentifier() {
        return SOURCE_ID;
    }

    @Override
    public String getSourceLanguage() {
        // AI generates in the requested language directly
        return null;
    }

    @Override
    public List<Question> fetchQuestions(QuestionProviderRequest request) {
        String language = request.getLanguage() != null ? request.getLanguage() : "en";
        String text = request.getText();
        int amount = Math.min(request.getAmount(), 20);

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content is required for AI quiz generation");
        }

        String systemPrompt = buildSystemPrompt(language, amount);
        String userPrompt = "Generate quiz questions based on the following text:\n\n" + text;

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7
            );

            String responseBody = restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseBody);
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("openai", "Failed to generate questions via OpenAI: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String language, int amount) {
        return String.format(
                "You are a quiz generator. Generate exactly %d multiple-choice questions in %s language " +
                "based on the provided text. Each question must have exactly 4 options with one correct answer. " +
                "Respond ONLY with a JSON array (no markdown, no explanation) where each element has:\n" +
                "- \"question\": the question text\n" +
                "- \"options\": array of 4 answer strings\n" +
                "- \"correctAnswerIndex\": index (0-3) of the correct answer\n" +
                "- \"difficulty\": integer 1-3 (1=easy, 2=medium, 3=hard)\n" +
                "Example: [{\"question\":\"...\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correctAnswerIndex\":0,\"difficulty\":1}]",
                amount, getLanguageName(language)
        );
    }

    private String getLanguageName(String code) {
        return switch (code.toLowerCase()) {
            case "fr" -> "French";
            case "en" -> "English";
            default -> "English";
        };
    }

    List<Question> parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Strip markdown code blocks if present
            content = content.strip();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.strip();

            List<Map<String, Object>> items = objectMapper.readValue(content, new TypeReference<>() {});
            List<Question> questions = new ArrayList<>();

            for (Map<String, Object> item : items) {
                String questionText = (String) item.get("question");
                List<String> options = objectMapper.convertValue(item.get("options"), new TypeReference<>() {});
                int correctIndex = item.get("correctAnswerIndex") instanceof Integer
                        ? (Integer) item.get("correctAnswerIndex")
                        : Integer.parseInt(item.get("correctAnswerIndex").toString());
                int difficulty = item.get("difficulty") instanceof Integer
                        ? (Integer) item.get("difficulty")
                        : Integer.parseInt(item.get("difficulty").toString());

                questions.add(new Question(null, questionText, options, correctIndex, List.of(), difficulty));
            }
            return questions;
        } catch (Exception e) {
            throw new ExternalServiceException("openai", "Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}
