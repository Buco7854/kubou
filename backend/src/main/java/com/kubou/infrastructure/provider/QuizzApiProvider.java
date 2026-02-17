package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.QuizzApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class QuizzApiProvider implements QuestionProvider {

    private static final String SOURCE_ID = "quizzapi";
    private static final String BASE_URL = "https://quizzapi.jomoreschi.fr/api/v2/quiz";

    private final RestClient restClient;

    public QuizzApiProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String getSourceIdentifier() {
        return SOURCE_ID;
    }

    @Override
    public String getSourceLanguage() {
        return "fr";
    }

    @Override
    public List<Question> fetchQuestions(QuestionProviderRequest request) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("limit", request.getAmount());

            if (request.getTags() != null && !request.getTags().isEmpty()) {
                uriBuilder.queryParam("category", request.getTags().get(0));
            }

            QuizzApiResponse response = restClient.get()
                    .uri(uriBuilder.toUriString())
                    .retrieve()
                    .body(QuizzApiResponse.class);

            if (response == null || response.getQuizzes() == null) {
                return List.of();
            }

            List<Question> questions = new ArrayList<>();
            for (QuizzApiResponse.QuizzItem item : response.getQuizzes()) {
                questions.add(mapToQuestion(item));
            }
            return questions;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("quizzapi.jomoreschi.fr", "Failed to fetch questions: " + e.getMessage(), e);
        }
    }

    private Question mapToQuestion(QuizzApiResponse.QuizzItem item) {
        List<String> options = new ArrayList<>(item.getBadAnswers());
        options.add(item.getAnswer());
        Collections.shuffle(options);

        int correctIndex = options.indexOf(item.getAnswer());

        List<String> tags = new ArrayList<>();
        if (item.getCategory() != null && !item.getCategory().isBlank()) {
            tags.add(item.getCategory());
        }

        int difficultyLevel = mapDifficulty(item.getDifficulty());

        return new Question(null, item.getQuestion(), options, correctIndex, tags, difficultyLevel);
    }

    private int mapDifficulty(String difficulty) {
        if (difficulty == null) return 1;
        return switch (difficulty.toLowerCase()) {
            case "facile" -> 1;
            case "normal" -> 2;
            case "difficile" -> 3;
            default -> 1;
        };
    }
}
