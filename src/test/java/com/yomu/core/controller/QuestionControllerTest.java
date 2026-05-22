package com.yomu.core.controller;

import com.yomu.core.entity.Question;
import com.yomu.core.entity.Reading;
import com.yomu.core.repository.QuestionRepository;
import com.yomu.core.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ReadingRepository readingRepository;

    private QuestionController questionController;

    @BeforeEach
    void setUp() {
        questionController = new QuestionController(questionRepository, readingRepository);
    }

    @Test
    void getQuestionsByReading_returnsQuestionsFromRepository() {
        UUID readingId = UUID.randomUUID();
        List<Question> questions = List.of(new Question(), new Question());

        when(questionRepository.findByReadingId(readingId)).thenReturn(questions);

        ResponseEntity<List<Question>> response = questionController.getQuestionsByReading(readingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(questions, response.getBody());
        verify(questionRepository).findByReadingId(readingId);
        verifyNoMoreInteractions(questionRepository, readingRepository);
    }

    @Test
    void createQuestion_savesQuestionAndCopiesRequestFields() {
        UUID readingId = UUID.randomUUID();
        Reading reading = new Reading();

        QuestionController.QuestionRequest request = new QuestionController.QuestionRequest();
        request.setReadingId(readingId);
        request.setQuestionText("What is Java?");
        request.setOptions(List.of("Language", "Animal", "Car", "Planet"));
        request.setCorrectAnswer(0);
        request.setExplanation("Java is a programming language.");

        when(readingRepository.findById(readingId)).thenReturn(Optional.of(reading));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Question> response = questionController.createQuestion(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());

        Question savedQuestion = captor.getValue();
        assertSame(reading, savedQuestion.getReading());
        assertEquals("What is Java?", savedQuestion.getQuestionText());
        assertEquals(List.of("Language", "Animal", "Car", "Planet"), savedQuestion.getOptions());
        assertEquals(0, savedQuestion.getCorrectAnswer());
        assertEquals("Java is a programming language.", savedQuestion.getExplanation());
        verify(readingRepository).findById(readingId);
        verifyNoMoreInteractions(questionRepository, readingRepository);
    }

    @Test
    void createQuestion_whenReadingDoesNotExist_throwsRuntimeException() {
        UUID readingId = UUID.randomUUID();

        QuestionController.QuestionRequest request = new QuestionController.QuestionRequest();
        request.setReadingId(readingId);
        request.setQuestionText("Question?");
        request.setOptions(List.of("A", "B"));
        request.setCorrectAnswer(0);

        when(readingRepository.findById(readingId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> questionController.createQuestion(request));
        assertEquals("Reading not found", ex.getMessage());

        verify(readingRepository).findById(readingId);
        verifyNoInteractions(questionRepository);
    }

    @Test
    void updateQuestion_updatesExistingQuestionAndSavesIt() {
        UUID questionId = UUID.randomUUID();
        Question existingQuestion = new Question();

        QuestionController.QuestionRequest request = new QuestionController.QuestionRequest();
        request.setQuestionText("Updated text");
        request.setOptions(List.of("A", "B", "C"));
        request.setCorrectAnswer(2);
        request.setExplanation("Updated explanation");

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Question> response = questionController.updateQuestion(questionId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());

        Question updatedQuestion = captor.getValue();
        assertEquals("Updated text", updatedQuestion.getQuestionText());
        assertEquals(List.of("A", "B", "C"), updatedQuestion.getOptions());
        assertEquals(2, updatedQuestion.getCorrectAnswer());
        assertEquals("Updated explanation", updatedQuestion.getExplanation());
        verify(questionRepository).findById(questionId);
        verifyNoMoreInteractions(questionRepository, readingRepository);
    }

    @Test
    void updateQuestion_whenQuestionDoesNotExist_throwsRuntimeException() {
        UUID questionId = UUID.randomUUID();

        QuestionController.QuestionRequest request = new QuestionController.QuestionRequest();
        request.setQuestionText("Updated text");
        request.setOptions(List.of("A", "B"));
        request.setCorrectAnswer(1);

        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> questionController.updateQuestion(questionId, request));
        assertEquals("Question not found", ex.getMessage());

        verify(questionRepository).findById(questionId);
        verifyNoInteractions(readingRepository);
        verify(questionRepository, never()).save(any());
    }

    @Test
    void deleteQuestion_deletesById() {
        UUID questionId = UUID.randomUUID();

        ResponseEntity<Void> response = questionController.deleteQuestion(questionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(questionRepository).deleteById(questionId);
        verifyNoInteractions(readingRepository);
    }
}
