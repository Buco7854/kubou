package com.kubou.application.service;

import com.kubou.application.repository.PlayerResponseRepository;
import com.kubou.domain.entity.PlayerResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final PlayerResponseRepository responseRepository;

    public AnalyticsService(PlayerResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    public Map<String, Object> getPlayerAnalytics(String playerId, String period) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (period.toLowerCase()) {
            case "week":
                start = end.minusWeeks(1);
                break;
            case "month":
                start = end.minusMonths(1);
                break;
            case "year":
                start = end.minusYears(1);
                break;
            default:
                start = end.minusDays(1); // Default to last 24h
        }

        List<PlayerResponse> responses = responseRepository.findByPlayerIdAndDateRange(playerId, start, end);

        int totalQuestions = responses.size();
        long correctCount = responses.stream().filter(PlayerResponse::isCorrect).count();
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0.0;
        
        double averageTimeMs = responses.stream()
                .mapToLong(PlayerResponse::getTimeTakenMs)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("playerId", playerId);
        stats.put("period", period);
        stats.put("totalQuestions", totalQuestions);
        stats.put("correctCount", correctCount);
        stats.put("accuracy", accuracy);
        stats.put("averageTimeMs", averageTimeMs);
        
        // Could add evolution graph data here (group by day)
        
        return stats;
    }
}
