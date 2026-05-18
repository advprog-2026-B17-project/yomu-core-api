package com.yomu.core.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class QuizResult {
    private UUID readingId;
    private int score;
    private BigDecimal accuracy;
    private int totalQuestions;
    private int correctAnswers;

    public QuizResult(UUID readingId, int score, BigDecimal accuracy, int totalQuestions, int correctAnswers) {
        this.readingId = readingId;
        this.score = score;
        this.accuracy = accuracy;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
    }

    public UUID getReadingId() { return readingId; }
    public int getScore() { return score; }
    public BigDecimal getAccuracy() { return accuracy; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
}
