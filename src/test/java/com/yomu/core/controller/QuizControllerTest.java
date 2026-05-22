package com.yomu.core.controller;

import com.yomu.core.dto.QuestionDTO;
import com.yomu.core.dto.QuizResult;
import com.yomu.core.dto.QuizSubmission;
import com.yomu.core.entity.Reading;
import com.yomu.core.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @Mock
    private Authentication authentication;

    private QuizController quizController;

    @BeforeEach
    void setUp() {
        quizController = new QuizController(quizService);
    }

    @Test
    void getAllReadings_returnsAllReadings() {
        List<Reading> readings = List.of(new Reading(), new Reading());
        when(quizService.getAllReadings()).thenReturn(readings);

        ResponseEntity<List<Reading>> response = quizController.getAllReadings();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(readings, response.getBody());
        verify(quizService).getAllReadings();
        verifyNoInteractions(authentication);
    }

    @Test
    void getReading_whenFound_returnsOk() {
        UUID readingId = UUID.randomUUID();
        Reading reading = new Reading();
        when(quizService.getReadingById(readingId)).thenReturn(Optional.of(reading));

        ResponseEntity<Reading> response = quizController.getReading(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(reading, response.getBody());
        verify(quizService).getReadingById(readingId);
        verifyNoInteractions(authentication);
    }

    @Test
    void getReading_whenMissing_returnsNotFound() {
        UUID readingId = UUID.randomUUID();
        when(quizService.getReadingById(readingId)).thenReturn(Optional.empty());

        ResponseEntity<Reading> response = quizController.getReading(readingId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(quizService).getReadingById(readingId);
        verifyNoInteractions(authentication);
    }

    @Test
    void getQuestions_returnsQuestionDtos() {
        UUID readingId = UUID.randomUUID();
        List<QuestionDTO> dtos = List.of(mock(QuestionDTO.class), mock(QuestionDTO.class));
        when(quizService.getQuestionDTOsForReading(readingId)).thenReturn(dtos);

        ResponseEntity<List<QuestionDTO>> response = quizController.getQuestions(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dtos, response.getBody());
        verify(quizService).getQuestionDTOsForReading(readingId);
        verifyNoInteractions(authentication);
    }

    @Test
    void submitQuiz_setsReadingIdAndReturnsResult() {
        UUID readingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getPrincipal()).thenReturn(userId.toString());

        QuizSubmission submission = new QuizSubmission();
        QuizResult result = mock(QuizResult.class);
        when(quizService.submitQuiz(eq(userId), any(QuizSubmission.class))).thenReturn(result);

        ResponseEntity<QuizResult> response = quizController.submitQuiz(readingId, submission, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(result, response.getBody());
        assertEquals(readingId, submission.getReadingId());

        ArgumentCaptor<QuizSubmission> captor = ArgumentCaptor.forClass(QuizSubmission.class);
        verify(quizService).submitQuiz(eq(userId), captor.capture());
        assertEquals(readingId, captor.getValue().getReadingId());
        verify(authentication).getPrincipal();
    }

    @Test
    void getReadingStatus_returnsServiceResult() {
        UUID readingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getPrincipal()).thenReturn(userId.toString());
        when(quizService.hasCompletedReading(userId, readingId)).thenReturn(true);

        ResponseEntity<Boolean> response = quizController.getReadingStatus(readingId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(quizService).hasCompletedReading(userId, readingId);
        verify(authentication).getPrincipal();
    }

    @Test
    void markReadingComplete_whenNotCompleted_marksCompleteAndReturnsOk() {
        UUID readingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getPrincipal()).thenReturn(userId.toString());
        when(quizService.hasCompletedReading(userId, readingId)).thenReturn(false);

        ResponseEntity<Void> response = quizController.markReadingComplete(readingId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(quizService).hasCompletedReading(userId, readingId);
        verify(quizService).markReadingComplete(userId, readingId);
        verify(authentication).getPrincipal();
    }

    @Test
    void markReadingComplete_whenAlreadyCompleted_returnsConflict() {
        UUID readingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getPrincipal()).thenReturn(userId.toString());
        when(quizService.hasCompletedReading(userId, readingId)).thenReturn(true);

        ResponseEntity<Void> response = quizController.markReadingComplete(readingId, authentication);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
        verify(quizService).hasCompletedReading(userId, readingId);
        verify(quizService, never()).markReadingComplete(any(), any());
        verify(authentication).getPrincipal();
    }

    @Test
    void createReading_returnsSavedReading() {
        UUID userId = UUID.randomUUID();
        UUID savedReadingId = UUID.randomUUID();

        when(authentication.getPrincipal()).thenReturn(userId.toString());

        Reading savedReading = new Reading();
        when(quizService.saveReading(any(Reading.class), eq("Math"))).thenReturn(savedReading);

        ResponseEntity<Reading> response = quizController.createReading(
                Map.of("title", "Algebra", "content", "Quadratic equations", "categoryName", "Math"),
                authentication
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(savedReading, response.getBody());

        ArgumentCaptor<Reading> captor = ArgumentCaptor.forClass(Reading.class);
        verify(quizService).saveReading(captor.capture(), eq("Math"));

        Reading captured = captor.getValue();
        assertEquals("Algebra", captured.getTitle());
        assertEquals("Quadratic equations", captured.getContent());
        assertEquals(userId, captured.getCreatedBy());
        verify(authentication).getPrincipal();
    }

    @Test
    void deleteReading_returnsOk() {
        UUID readingId = UUID.randomUUID();
        // UUID userId = UUID.randomUUID();

        // when(authentication.getPrincipal()).thenReturn(userId.toString());

        ResponseEntity<Void> response = quizController.deleteReading(readingId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(quizService).deleteReading(readingId);
        // verify(authentication).getPrincipal();
    }
}
