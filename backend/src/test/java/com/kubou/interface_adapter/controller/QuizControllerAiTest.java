package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.usecase.GenerateQuizUseCase;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import com.kubou.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubou.interface_adapter.controller.dto.AiQuizRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizControllerAiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private GenerateQuizUseCase generateQuizUseCase;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGenerateAiQuiz() throws Exception {
        // Given
        AiQuizRequest request = new AiQuizRequest();
        request.setTitle("AI Generated Quiz");
        request.setText("The Earth revolves around the Sun.");
        request.setAmount(3);
        request.setLanguage("en");

        Quiz expectedQuiz = new Quiz(
                "quiz-id-123",
                "AI Generated Quiz",
                List.of(
                        new Question("q1", "Q1?", List.of("A", "B", "C", "D"), 0, List.of(), 1, "user-123"),
                        new Question("q2", "Q2?", List.of("A", "B", "C", "D"), 1, List.of(), 2, "user-123"),
                        new Question("q3", "Q3?", List.of("A", "B", "C", "D"), 2, List.of(), 1, "user-123")
                ),
                "user-123"
        );

        when(generateQuizUseCase.execute(eq("AI Generated Quiz"), eq("user-123"), eq("openai"), any()))
                .thenReturn(expectedQuiz);

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user-123");

        // When / Then
        mockMvc.perform(post("/api/v1/quizzes/generate-ai")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("quiz-id-123"))
                .andExpect(jsonPath("$.title").value("AI Generated Quiz"))
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions.length()").value(3));
    }
}
