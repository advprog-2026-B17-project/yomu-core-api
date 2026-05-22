package com.yomu.core.service;

import com.yomu.core.dto.QuizResult;
import com.yomu.core.dto.QuizSubmission;
import com.yomu.core.dto.QuestionDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuizService {

    private final ReadingRepository readingRepository;
    private final QuestionRepository questionRepository;
    private final CompletedReadingRepository completedReadingRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CategoryRepository categoryRepository;
    private final EventPublisher eventPublisher;

    public QuizService(ReadingRepository readingRepository,
                       QuestionRepository questionRepository,
                       CompletedReadingRepository completedReadingRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       CategoryRepository categoryRepository,
                       EventPublisher eventPublisher) {
        this.readingRepository = readingRepository;
        this.questionRepository = questionRepository;
        this.completedReadingRepository = completedReadingRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Reading> getAllReadings() {
        return readingRepository.findAll();
    }

    public Optional<Reading> getReadingById(UUID id) {
        return readingRepository.findById(id);
    }

    public List<Question> getQuestionsForReading(UUID readingId) {
        return questionRepository.findByReadingId(readingId);
    }

    public List<QuestionDTO> getQuestionDTOsForReading(UUID readingId) {
        return questionRepository.findByReadingId(readingId).stream()
                .map(question -> new QuestionDTO(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptions()
                ))
                .toList();
    }

    @Transactional
    public QuizResult submitQuiz(UUID userId, QuizSubmission submission) {
        UUID readingId = submission.getReadingId();
        validateQuizSubmission(userId, readingId);

        List<Question> questions = loadQuestionsForReading(readingId);
        List<Integer> answers = normalizeAnswers(submission.getAnswers());
        QuizEvaluation evaluation = evaluateQuiz(questions, answers);

        OffsetDateTime submittedAt = OffsetDateTime.now();
        saveQuizAttempt(userId, readingId, answers, evaluation, submittedAt);
        saveCompletedReadingIfNeeded(userId, readingId, evaluation, submittedAt);
        publishQuizCompletedEvent(userId, readingId, evaluation);

        return new QuizResult(
                readingId,
                evaluation.score(),
                evaluation.accuracy(),
                evaluation.totalQuestions(),
                evaluation.correctAnswers()
        );
    }

    public boolean hasCompletedReading(UUID userId, UUID readingId) {
        return completedReadingRepository.existsByUserIdAndReadingId(userId, readingId);
    }

    @Transactional
    public void markReadingComplete(UUID userId, UUID readingId) {
        if (!quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)) {
            throw new RuntimeException("Submit the quiz before completing this reading");
        }
        if (!completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)) {
            CompletedReading completed = new CompletedReading();
            completed.setUserId(userId);
            completed.setReadingId(readingId);
            completed.setScore(0);
            completed.setAccuracy(BigDecimal.ZERO);
            completed.setCompletedAt(OffsetDateTime.now());
            completedReadingRepository.save(completed);
            eventPublisher.publishReadingCompleted(userId, readingId);
        }
    }

    @Transactional
    public Reading saveReading(Reading reading, String categoryName) {
        if (categoryName != null && !categoryName.isBlank()) {
            Category cat = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(categoryName);
                        return categoryRepository.save(newCategory);
                    });
            reading.setCategory(cat);
        }
        return readingRepository.save(reading);
    }

    @Transactional
    public void deleteReading(UUID id) {
        readingRepository.deleteById(id);
    }

    private void validateQuizSubmission(UUID userId, UUID readingId) {
        if (quizAttemptRepository.existsByUserIdAndReadingId(userId, readingId)) {
            throw new RuntimeException("You have already completed this quiz");
        }
    }

    private List<Question> loadQuestionsForReading(UUID readingId) {
        List<Question> questions = questionRepository.findByReadingId(readingId);
        if (questions.isEmpty()) {
            throw new RuntimeException("No questions found for this reading");
        }
        return questions;
    }

    private List<Integer> normalizeAnswers(List<Integer> answers) {
        return answers == null ? Collections.emptyList() : answers;
    }

    private QuizEvaluation evaluateQuiz(List<Question> questions, List<Integer> answers) {
        int correctAnswers = 0;
        int totalQuestions = questions.size();

        for (int i = 0; i < totalQuestions && i < answers.size(); i++) {
            if (Objects.equals(questions.get(i).getCorrectAnswer(), answers.get(i))) {
                correctAnswers++;
            }
        }

        int score = (int) Math.round((double) correctAnswers / totalQuestions * 100);
        BigDecimal accuracy = BigDecimal.valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP);

        return new QuizEvaluation(totalQuestions, correctAnswers, score, accuracy);
    }

    private void saveQuizAttempt(UUID userId,
                                 UUID readingId,
                                 List<Integer> answers,
                                 QuizEvaluation evaluation,
                                 OffsetDateTime submittedAt) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setReadingId(readingId);
        attempt.setAnswers(answers);
        attempt.setScore(evaluation.score());
        attempt.setAccuracy(evaluation.accuracy());
        attempt.setCompletedAt(submittedAt);
        quizAttemptRepository.save(attempt);
    }

    private void saveCompletedReadingIfNeeded(UUID userId,
                                              UUID readingId,
                                              QuizEvaluation evaluation,
                                              OffsetDateTime completedAt) {
        if (completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)) {
            return;
        }

        CompletedReading completed = new CompletedReading();
        completed.setUserId(userId);
        completed.setReadingId(readingId);
        completed.setScore(evaluation.score());
        completed.setAccuracy(evaluation.accuracy());
        completed.setCompletedAt(completedAt);
        completedReadingRepository.save(completed);
    }

    private void publishQuizCompletedEvent(UUID userId, UUID readingId, QuizEvaluation evaluation) {
        eventPublisher.publishQuizCompleted(
                userId,
                readingId,
                evaluation.score(),
                evaluation.accuracy().doubleValue()
        );
    }

    private record QuizEvaluation(int totalQuestions, int correctAnswers, int score, BigDecimal accuracy) {
    }
}
