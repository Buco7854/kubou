package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.QuizApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class QuizApiProvider implements QuestionProvider {

    private static final String SOURCE_ID = "quizapi";
    private static final String BASE_URL = "https://quizapi.io/api/v1/questions";
    private static final String[] ANSWER_KEYS = {"answer_a", "answer_b", "answer_c", "answer_d", "answer_e", "answer_f"};

    private final RestClient restClient;
    private final String apiKey;

    public QuizApiProvider(RestClient restClient, @Value("${quizapi.api-key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public String getSourceIdentifier() {
        return SOURCE_ID;
    }

    @Override
    public String getSourceLanguage() {
        return "en";
    }

    @Override
    public List<Question> fetchQuestions(QuestionProviderRequest request) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("limit", request.getAmount());

            if (request.getTags() != null && !request.getTags().isEmpty()) {
                uriBuilder.queryParam("tags", request.getTags().get(0));
            }

            List<QuizApiResponse> responses = restClient.get()
                    .uri(uriBuilder.toUriString())
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (responses == null) {
                return List.of();
            }

            List<Question> questions = new ArrayList<>();
            for (QuizApiResponse r : responses) {
                if ("true".equalsIgnoreCase(r.getMultiple_correct_answers())) {
                    continue;
                }
                Question q = mapToQuestion(r);
                if (q != null) {
                    questions.add(q);
                }
            }
            return questions;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("quizapi.io", "Failed to fetch questions: " + e.getMessage(), e);
        }
    }

    private Question mapToQuestion(QuizApiResponse r) {
        Map<String, String> answersMap = r.getAnswers();
        Map<String, String> correctMap = r.getCorrect_answers();

        if (answersMap == null || correctMap == null) {
            return null;
        }

        List<String> options = new ArrayList<>();
        int correctIndex = -1;

        for (String key : ANSWER_KEYS) {
            String answerText = answersMap.get(key);
            if (answerText != null && !answerText.isBlank()) {
                if ("true".equalsIgnoreCase(correctMap.get(key + "_correct"))) {
                    correctIndex = options.size();
                }
                options.add(answerText);
            }
        }

        if (correctIndex == -1 || options.size() < 2) {
            return null;
        }

        List<String> tags = new ArrayList<>();
        if (r.getTags() != null) {
            for (QuizApiResponse.Tag tag : r.getTags()) {
                if (tag.getName() != null && !tag.getName().isBlank()) {
                    tags.add(tag.getName());
                }
            }
        }
        if (r.getCategory() != null && !r.getCategory().isBlank()) {
            tags.add(r.getCategory());
        }

        int difficultyLevel = mapDifficulty(r.getDifficulty());

        return new Question(null, r.getQuestion(), options, correctIndex, tags, difficultyLevel);
    }

    private int mapDifficulty(String difficulty) {
        if (difficulty == null) return 1;
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> 1;
        };
    }
}
