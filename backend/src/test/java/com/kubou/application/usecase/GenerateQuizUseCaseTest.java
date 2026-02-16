package com.kubou.application.usecase;

import com.kubou.application.repository.QuestionRepository;
import com.kubou.application.repository.QuizRepository;
import com.kubou.application.service.QuestionProvider;
import com.kubou.application.service.QuestionProviderRequest;
import com.kubou.domain.entity.Question;
import com.kubou.domain.entity.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateQuizUseCaseTest {

    private GenerateQuizUseCase generateQuizUseCase;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionProvider mockProvider;

    @Captor
    private ArgumentCaptor<List<Question>> questionsCaptor;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @BeforeEach
    void setUp() {
        when(mockProvider.getSourceIdentifier()).thenReturn("test-source");
        generateQuizUseCase = new GenerateQuizUseCase(
                questionRepository, quizRepository, List.of(mockProvider));
    }

    @Test
    void shouldFetchQuestionsAndCreateQuiz() {
        // Given
        String title = "My Quiz";
        String creatorId = "user-123";
        String source = "test-source";
        QuestionProviderRequest request = new QuestionProviderRequest(List.of("science"), 2);

        List<Question> fetchedQuestions = List.of(
                new Question(null, "Q1?", List.of("A", "B", "C", "D"), 0, List.of("science"), 1),
                new Question(null, "Q2?", List.of("A", "B", "C", "D"), 2, List.of("science"), 1)
        );
        when(mockProvider.fetchQuestions(request)).thenReturn(fetchedQuestions);
        when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Quiz result = generateQuizUseCase.execute(title, creatorId, source, request);

        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(creatorId, result.getCreatorId());
        assertEquals(2, result.getQuestions().size());

        verify(questionRepository).saveAll(questionsCaptor.capture());
        List<Question> savedQuestions = questionsCaptor.getValue();
        for (Question q : savedQuestions) {
            assertNotNull(q.getId());
            assertEquals(creatorId, q.getCreatorId());
        }

        verify(quizRepository).save(quizCaptor.capture());
        Quiz savedQuiz = quizCaptor.getValue();
        assertNotNull(savedQuiz.getId());
        assertEquals(title, savedQuiz.getTitle());
        assertEquals(creatorId, savedQuiz.getCreatorId());
    }

    @Test
    void shouldThrowWhenSourceIsUnknown() {
        // Given
        QuestionProviderRequest request = new QuestionProviderRequest(List.of(), 5);

        // When / Then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> generateQuizUseCase.execute("Title", "user-1", "unknown-source", request));
        assertTrue(ex.getMessage().contains("unknown-source"));
    }

    @Test
    void shouldAssignUniqueIdsToEachQuestion() {
        // Given
        String source = "test-source";
        QuestionProviderRequest request = new QuestionProviderRequest(List.of(), 3);

        List<Question> fetchedQuestions = List.of(
                new Question(null, "Q1?", List.of("A", "B"), 0, List.of(), 1),
                new Question(null, "Q2?", List.of("A", "B"), 1, List.of(), 1),
                new Question(null, "Q3?", List.of("A", "B"), 0, List.of(), 1)
        );
        when(mockProvider.fetchQuestions(request)).thenReturn(fetchedQuestions);
        when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        generateQuizUseCase.execute("Quiz", "user-1", source, request);

        // Then
        verify(questionRepository).saveAll(questionsCaptor.capture());
        List<Question> saved = questionsCaptor.getValue();
        long uniqueIds = saved.stream().map(Question::getId).distinct().count();
        assertEquals(3, uniqueIds);
    }
}
