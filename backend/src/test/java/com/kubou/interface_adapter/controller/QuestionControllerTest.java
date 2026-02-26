package com.kubou.interface_adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.domain.entity.Question;
import com.kubou.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateAndRetrieveQuestion() throws Exception {
        // Given: A new question object
        Question savedQuestion = new Question("generated-id", "What is the capital of Japan?",
                Arrays.asList("Beijing", "Seoul", "Tokyo"), 2,
                Collections.singletonList("Geography"), 2, "user-123");

        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);
        when(questionRepository.findById("generated-id")).thenReturn(Optional.of(savedQuestion));

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user-123");

        Question newQuestion = new Question(null, "What is the capital of Japan?",
                Arrays.asList("Beijing", "Seoul", "Tokyo"), 2,
                Collections.singletonList("Geography"), 2);

        // When: We POST the new question to the API
        mockMvc.perform(post("/api/v1/questions")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newQuestion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("What is the capital of Japan?")));

        // Then: We can GET the question by its ID
        mockMvc.perform(get("/api/v1/questions/generated-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("generated-id")))
                .andExpect(jsonPath("$.text", is("What is the capital of Japan?")))
                .andExpect(jsonPath("$.correctAnswerIndex", is(2)));
    }
}
