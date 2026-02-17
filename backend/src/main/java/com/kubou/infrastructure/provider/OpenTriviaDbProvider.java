package com.kubou.infrastructure.provider;

import com.kubou.application.service.ExternalServiceException;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.provider.dto.OpenTriviaDbResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OpenTriviaDbProvider implements QuestionProvider {

    private static final String SOURCE_ID = "opentdb";
    private static final String BASE_URL = "https://opentdb.com/api.php";

    // Mapping of category slug -> Open Trivia DB category ID
    private static final Map<String, Integer> CATEGORY_MAP = Map.ofEntries(
            Map.entry("general_knowledge", 9),
            Map.entry("books", 10),
            Map.entry("film", 11),
            Map.entry("music", 12),
            Map.entry("musicals_theatres", 13),
            Map.entry("television", 14),
            Map.entry("video_games", 15),
            Map.entry("board_games", 16),
            Map.entry("science_nature", 17),
            Map.entry("computers", 18),
            Map.entry("mathematics", 19),
            Map.entry("mythology", 20),
            Map.entry("sports", 21),
            Map.entry("geography", 22),
            Map.entry("history", 23),
            Map.entry("politics", 24),
            Map.entry("art", 25),
            Map.entry("celebrities", 26),
            Map.entry("animals", 27),
            Map.entry("vehicles", 28),
            Map.entry("comics", 29),
            Map.entry("gadgets", 30),
            Map.entry("anime_manga", 31),
            Map.entry("cartoon_animations", 32)
    );

    private final RestClient restClient;

    public OpenTriviaDbProvider(RestClient restClient) {
        this.restClient = restClient;
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
                    .queryParam("amount", Math.min(request.getAmount(), 50))
                    .queryParam("type", "multiple")
                    .queryParam("encode", "url3986");

            if (request.getTags() != null && !request.getTags().isEmpty()) {
                String categorySlug = request.getTags().get(0);
                Integer categoryId = CATEGORY_MAP.get(categorySlug);
                if (categoryId != null) {
                    uriBuilder.queryParam("category", categoryId);
                }
            }

            OpenTriviaDbResponse response = restClient.get()
                    .uri(uriBuilder.toUriString())
                    .retrieve()
                    .body(OpenTriviaDbResponse.class);

            if (response == null || response.getResults() == null) {
                return List.of();
            }

            if (response.getResponseCode() != 0) {
                return List.of();
            }

            List<Question> questions = new ArrayList<>();
            for (OpenTriviaDbResponse.TriviaItem item : response.getResults()) {
                questions.add(mapToQuestion(item));
            }
            return questions;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("opentdb.com", "Failed to fetch questions: " + e.getMessage(), e);
        }
    }

    private Question mapToQuestion(OpenTriviaDbResponse.TriviaItem item) {
        String correctAnswer = decodeUrl(item.getCorrectAnswer());
        List<String> options = new ArrayList<>();
        for (String incorrect : item.getIncorrectAnswers()) {
            options.add(decodeUrl(incorrect));
        }
        options.add(correctAnswer);
        Collections.shuffle(options);

        int correctIndex = options.indexOf(correctAnswer);

        List<String> tags = new ArrayList<>();
        if (item.getCategory() != null && !item.getCategory().isBlank()) {
            tags.add(decodeUrl(item.getCategory()));
        }

        int difficultyLevel = mapDifficulty(item.getDifficulty());

        return new Question(null, decodeUrl(item.getQuestion()), options, correctIndex, tags, difficultyLevel);
    }

    private int mapDifficulty(String difficulty) {
        if (difficulty == null) return 1;
        return switch (decodeUrl(difficulty).toLowerCase()) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> 1;
        };
    }

    private String decodeUrl(String encoded) {
        if (encoded == null) return null;
        try {
            return java.net.URLDecoder.decode(encoded, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded;
        }
    }
}
