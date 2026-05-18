package com.yomu.core.service;

import com.yomu.core.dto.QuizResult;
import com.yomu.core.dto.QuizSubmission;
import com.yomu.core.entity.Category;
import com.yomu.core.entity.CompletedReading;
import com.yomu.core.entity.Question;
import com.yomu.core.entity.QuizAttempt;
import com.yomu.core.entity.Reading;
import com.yomu.core.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

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

    @Transactional
    public QuizResult submitQuiz(UUID userId, QuizSubmission submission) {
        // Check if already attempted this quiz (prevent re-submission)
        if (quizAttemptRepository.existsByUserIdAndReadingId(userId, submission.getReadingId())) {
            throw new RuntimeException("You have already completed this quiz");
        }

        List<Question> questions = questionRepository.findByReadingId(submission.getReadingId());
        if (questions.isEmpty()) {
            throw new RuntimeException("No questions found for this reading");
        }

        List<Integer> answers = submission.getAnswers();
        int correct = 0;
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            if (questions.get(i).getCorrectAnswer().equals(answers.get(i))) {
                correct++;
            }
        }

        int total = questions.size();
        int score = (int) Math.round((double) correct / total * 100);
        BigDecimal accuracy = BigDecimal.valueOf(correct).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

        // Save quiz attempt for tracking
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setReadingId(submission.getReadingId());
        attempt.setAnswers(answers);
        attempt.setScore(score);
        attempt.setAccuracy(accuracy);
        attempt.setCompletedAt(OffsetDateTime.now());
        quizAttemptRepository.save(attempt);

        // Save completed reading (one-time completion) - only if not already exists
        if (!completedReadingRepository.existsByUserIdAndReadingId(userId, submission.getReadingId())) {
            CompletedReading completed = new CompletedReading();
            completed.setUserId(userId);
            completed.setReadingId(submission.getReadingId());
            completed.setScore(score);
            completed.setAccuracy(accuracy);
            completed.setCompletedAt(OffsetDateTime.now());
            completedReadingRepository.save(completed);
        }

        // Publish quiz.completed event - gamification engine handles mission progress & achievements
        eventPublisher.publishQuizCompleted(userId, submission.getReadingId(), score, accuracy.doubleValue());

        return new QuizResult(submission.getReadingId(), score, accuracy, total, correct);
    }

    public boolean hasCompletedReading(UUID userId, UUID readingId) {
        return completedReadingRepository.existsByUserIdAndReadingId(userId, readingId);
    }

    @Transactional
    public void markReadingComplete(UUID userId, UUID readingId) {
        if (!completedReadingRepository.existsByUserIdAndReadingId(userId, readingId)) {
            CompletedReading completed = new CompletedReading();
            completed.setUserId(userId);
            completed.setReadingId(readingId);
            completed.setScore(0);
            completed.setAccuracy(BigDecimal.ZERO);
            completed.setCompletedAt(java.time.OffsetDateTime.now());
            completedReadingRepository.save(completed);
            eventPublisher.publishReadingCompleted(userId, readingId);
        }
    }

    @Transactional
    public Reading saveReading(Reading reading, String categoryName) {
        if (categoryName != null && !categoryName.isBlank()) {
            Category cat = new Category();
            cat.setName(categoryName);
            cat = categoryRepository.save(cat);
            reading.setCategory(cat);
        }
        return readingRepository.save(reading);
    }

    @Transactional
    public void deleteReading(UUID id) {
        readingRepository.deleteById(id);
    }
}
