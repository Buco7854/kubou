package com.kubou.interface_adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubou.domain.entity.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser // Bypasses JWT for this test by providing a mock authenticated user
    void shouldCreateAndRetrieveQuestion() throws Exception {
        // Given: A new question object
        Question newQuestion = new Question(null, "What is the capital of Japan?",
                Arrays.asList("Beijing", "Seoul", "Tokyo"), 2,
                Collections.singletonList("Geography"), 2);

        // When: We POST the new question to the API
        MvcResult createResult = mockMvc.perform(post("/api/v1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newQuestion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("What is the capital of Japan?")))
                .andReturn();

        // Extract the ID from the response
        String createdQuestionJson = createResult.getResponse().getContentAsString();
        Question createdQuestion = objectMapper.readValue(createdQuestionJson, Question.class);
        String newId = createdQuestion.getId();

        // Then: We can GET the question by its new ID
        mockMvc.perform(get("/api/v1/questions/" + newId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newId)))
                .andExpect(jsonPath("$.text", is("What is the capital of Japan?")))
                .andExpect(jsonPath("$.correctAnswerIndex", is(2)));
    }
}
