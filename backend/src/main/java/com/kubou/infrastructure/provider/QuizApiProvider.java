package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.QuizApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuizApiProvider implements QuestionProvider {

    private static final String SOURCE_ID = "quizapi";
    private static final String BASE_URL = "https://quizapi.io/api/v1/questions";

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

            QuizApiResponse response = restClient.get()
                    .uri(uriBuilder.toUriString())
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .body(QuizApiResponse.class);

            if (response == null || !response.isSuccess() || response.getData() == null) {
                return List.of();
            }

            List<Question> questions = new ArrayList<>();
            for (QuizApiResponse.BrowseQuestion item : response.getData()) {
                Question q = mapToQuestion(item);
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

    private Question mapToQuestion(QuizApiResponse.BrowseQuestion item) {
        List<QuizApiResponse.Answer> answers = item.getAnswers();
        if (answers == null || answers.size() < 2) {
            return null;
        }

        List<String> options = new ArrayList<>();
        int correctIndex = -1;

        for (QuizApiResponse.Answer answer : answers) {
            if (answer.getText() != null && !answer.getText().isBlank()) {
                if (answer.isCorrect()) {
                    correctIndex = options.size();
                }
                options.add(answer.getText());
            }
        }

        if (correctIndex == -1 || options.size() < 2) {
            return null;
        }

        List<String> tags = new ArrayList<>();
        if (item.getTags() != null) {
            tags.addAll(item.getTags());
        }
        if (item.getCategory() != null && !item.getCategory().isBlank()) {
            tags.add(item.getCategory());
        }

        int difficultyLevel = mapDifficulty(item.getDifficulty());

        return new Question(null, item.getText(), options, correctIndex, tags, difficultyLevel);
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
