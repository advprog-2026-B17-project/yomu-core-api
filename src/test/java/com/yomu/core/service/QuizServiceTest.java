package com.yomu.core.service;

import com.yomu.core.dto.QuestionDTO;
import com.yomu.core.dto.QuizResult;
import com.yomu.core.dto.QuizSubmission;
import com.yomu.core.entity.Category;
import com.yomu.core.entity.CompletedReading;
import com.yomu.core.entity.Question;
import com.yomu.core.entity.QuizAttempt;
import com.yomu.core.entity.Reading;
import com.yomu.core.repository.CategoryRepository;
import com.yomu.core.repository.CompletedReadingRepository;
import com.yomu.core.repository.QuestionRepository;
import com.yomu.core.repository.QuizAttemptRepository;
import com.yomu.core.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Tests")
class QuizServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CompletedReadingRepository completedReadingRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private QuizService quizService;

    private UUID readingId;
    private UUID userId;
    private Reading testReading;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        readingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testReading = new Reading();
        testReading.setId(readingId);
        testReading.setTitle("Test Reading");
        testReading.setContent("Test content");

        testQuestion = new Question();
        testQuestion.setId(UUID.randomUUID());
        testQuestion.setReading(testReading);
        testQuestion.setQuestionText("What is this?");
        testQuestion.setOptions(List.of("A", "B", "C", "D"));
        testQuestion.setCorrectAnswer(0);
    }

    @Test
    @DisplayName("Should get all readings")
    void testGetAllReadings() {
        when(readingRepository.findAll()).thenReturn(List.of(testReading));

        List<Reading> result = quizService.getAllReadings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReading.getTitle(), result.get(0).getTitle());
        verify(readingRepository).findAll();
    }

    @Test
    @DisplayName("Should get reading by ID")
    void testGetReadingById() {
        when(readingRepository.findById(readingId)).thenReturn(Optional.of(testReading));

        Optional<Reading> result = quizService.getReadingById(readingId);

        assertTrue(result.isPresent());
        assertEquals(testReading.getTitle(), result.get().getTitle());
        verify(readingRepository).findById(readingId);
    }

    @Test
    @DisplayName("Should return empty when reading not found")
    void testGetReadingByIdNotFound() {
        when(readingRepository.findById(readingId)).thenReturn(Optional.empty());

        Optional<Reading> result = quizService.getReadingById(readingId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get questions for reading")
    void testGetQuestionsForReading() {
        when(questionRepository.findByReadingId(readingId)).thenReturn(List.of(testQuestion));

        List<Question> result = quizService.getQuestionsForReading(readingId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion.getQuestionText(), result.get(0).getQuestionText());
        verify(questionRepository).findByReadingId(readingId);
    }

    @Test
    @DisplayName("Should get question DTOs for reading")
    void testGetQuestionDTOsForReading() {
        when(questionRepository.findByReadingId(readingId)).thenReturn(List.of(testQuestion));

        List<QuestionDTO> result = quizService.getQuestionDTOsForReading(readingId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion.getQuestionText(), result.get(0).getQuestionText());
        assertEquals(testQuestion.getOptions(), result.get(0).getOptions());
    }

    @Nested
    @DisplayName("submitQuiz")
    class SubmitQuizTests {

        @Test
        @DisplayName("Should throw exception when already attempted quiz")
        void shouldThrowWhenAlreadyAttempted() {
            QuizSubmission submission = new QuizSubmission();
            submission.setReadingId(readingId);
            submission.setAnswers(List.of(0));

            when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);

            assertThrows(RuntimeException.class, () -> quizService.submitQuiz(userId, submission));
            verify(questionRepository, never()).findByReadingId(any());
            verify(quizAttemptRepository, never()).save(any(QuizAttempt.class));
            verify(completedReadingRepository, never()).save(any(CompletedReading.class));
            verify(eventPublisher, never()).publishQuizCompleted(any(), any(), anyInt(), anyDouble());
        }

        @Test
        @DisplayName("Should throw exception when no questions for reading")
        void shouldThrowWhenNoQuestions() {
            QuizSubmission submission = new QuizSubmission();
            submission.setReadingId(readingId);
            submission.setAnswers(List.of(0));

            when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);
            when(questionRepository.findByReadingId(readingId)).thenReturn(List.of());

            assertThrows(RuntimeException.class, () -> quizService.submitQuiz(userId, submission));
            verify(quizAttemptRepository, never()).save(any(QuizAttempt.class));
            verify(completedReadingRepository, never()).save(any(CompletedReading.class));
            verify(eventPublisher, never()).publishQuizCompleted(any(), any(), anyInt(), anyDouble());
        }

        @Test
        @DisplayName("Should submit quiz successfully")
        void shouldSubmitQuizSuccessfully() {
            QuizSubmission submission = new QuizSubmission();
            submission.setReadingId(readingId);
            submission.setAnswers(List.of(0));

            when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);
            when(questionRepository.findByReadingId(readingId)).thenReturn(List.of(testQuestion));
            when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);

            QuizResult result = quizService.submitQuiz(userId, submission);

            assertNotNull(result);
            assertEquals(readingId, result.getReadingId());
            assertEquals(100, result.getScore());
            assertEquals(1, result.getTotalQuestions());
            assertEquals(1, result.getCorrectAnswers());
            assertEquals(new BigDecimal("1.0000"), result.getAccuracy());

            verify(quizAttemptRepository).save(any(QuizAttempt.class));
            verify(completedReadingRepository).save(any(CompletedReading.class));
            verify(eventPublisher).publishQuizCompleted(userId, readingId, 100, 1.0);
        }

        @Test
        @DisplayName("Should submit quiz successfully but not save completed reading if already exists")
        void shouldSubmitQuizWithoutSavingCompletedReading() {
            QuizSubmission submission = new QuizSubmission();
            submission.setReadingId(readingId);
            submission.setAnswers(List.of(1));

            when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);
            when(questionRepository.findByReadingId(readingId)).thenReturn(List.of(testQuestion));
            when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);

            QuizResult result = quizService.submitQuiz(userId, submission);

            assertNotNull(result);
            assertEquals(0, result.getScore());
            assertEquals(new BigDecimal("0.0000"), result.getAccuracy());

            verify(quizAttemptRepository).save(any(QuizAttempt.class));
            verify(completedReadingRepository, never()).save(any(CompletedReading.class));
            verify(eventPublisher).publishQuizCompleted(userId, readingId, 0, 0.0);
        }

        @Test
        @DisplayName("Should handle null answers as empty list")
        void shouldHandleNullAnswers() {
            QuizSubmission submission = new QuizSubmission();
            submission.setReadingId(readingId);
            submission.setAnswers(null);

            when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);
            when(questionRepository.findByReadingId(readingId)).thenReturn(List.of(testQuestion));
            when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);

            QuizResult result = quizService.submitQuiz(userId, submission);

            assertNotNull(result);
            assertEquals(0, result.getCorrectAnswers());
            assertEquals(0, result.getScore());
            verify(quizAttemptRepository).save(any(QuizAttempt.class));
            verify(completedReadingRepository).save(any(CompletedReading.class));
        }
    }

    @Test
    @DisplayName("Should return if user completed reading")
    void testHasCompletedReading() {
        when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);

        boolean result = quizService.hasCompletedReading(userId, readingId);

        assertTrue(result);
        verify(completedReadingRepository).existsByUserIdAndReadingId(userId, readingId);
    }

    @Test
    @DisplayName("Should mark reading complete successfully")
    void testMarkReadingCompleteSuccess() {
        when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);
        when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);

        quizService.markReadingComplete(userId, readingId);

        verify(completedReadingRepository).save(any(CompletedReading.class));
        verify(eventPublisher).publishReadingCompleted(userId, readingId);
    }

    @Test
    @DisplayName("Should not save completed reading or publish event when already completed")
    void testMarkReadingCompleteAlreadyCompleted() {
        when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);
        when(completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(true);

        quizService.markReadingComplete(userId, readingId);

        verify(completedReadingRepository, never()).save(any(CompletedReading.class));
        verify(eventPublisher, never()).publishReadingCompleted(userId, readingId);
    }

    @Test
    @DisplayName("Should throw exception when marking complete without quiz attempt")
    void testMarkReadingCompleteNoQuizAttempt() {
        when(quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> quizService.markReadingComplete(userId, readingId));
        verify(completedReadingRepository, never()).save(any(CompletedReading.class));
        verify(eventPublisher, never()).publishReadingCompleted(userId, readingId);
    }

    @Test
    @DisplayName("Should save reading with existing category")
    void testSaveReadingWithExistingCategory() {
        Category category = new Category();
        category.setName("Technology");

        when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(category));
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading reading = new Reading();
        reading.setTitle("Spring Boot");

        Reading result = quizService.saveReading(reading, "Technology");

        assertNotNull(result);
        assertEquals(category, result.getCategory());
        verify(readingRepository).save(reading);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should save reading and create new category when it does not exist")
    void testSaveReadingWithNewCategory() {
        Category newCategory = new Category();
        newCategory.setName("Science");

        when(categoryRepository.findByName("Science")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading reading = new Reading();
        reading.setTitle("Quantum Physics");

        Reading result = quizService.saveReading(reading, "Science");

        assertNotNull(result);
        assertEquals(newCategory, result.getCategory());
        verify(categoryRepository).save(any(Category.class));
        verify(readingRepository).save(reading);
    }

    @Test
    @DisplayName("Should save reading without category when categoryName is blank or null")
    void testSaveReadingWithBlankCategory() {
        when(readingRepository.save(any(Reading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reading reading = new Reading();
        reading.setTitle("General Article");

        Reading resultNull = quizService.saveReading(reading, null);
        Reading resultBlank = quizService.saveReading(reading, "  ");

        assertNotNull(resultNull);
        assertNull(resultNull.getCategory());
        assertNotNull(resultBlank);
        assertNull(resultBlank.getCategory());
        verify(categoryRepository, never()).findByName(anyString());
        verify(readingRepository, times(2)).save(reading);
    }

    @Test
    @DisplayName("Should delete reading successfully")
    void testDeleteReading() {
        quizService.deleteReading(readingId);

        verify(readingRepository).deleteById(readingId);
    }
}
