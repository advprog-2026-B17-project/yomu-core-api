package com.yomu.core.dto;

public class StatsDTO {
    private long readingsCompleted;
    private long quizzesTaken;
    private double averageAccuracy;

    public StatsDTO(long readingsCompleted, long quizzesTaken, double averageAccuracy) {
        this.readingsCompleted = readingsCompleted;
        this.quizzesTaken = quizzesTaken;
        this.averageAccuracy = averageAccuracy;
    }

    public long getReadingsCompleted() { return readingsCompleted; }
    public long getQuizzesTaken() { return quizzesTaken; }
    public double getAverageAccuracy() { return averageAccuracy; }
}
