package com.yomu.core.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class QuizSubmission {
    @NotNull
    private UUID readingId;

    @NotNull
    private List<Integer> answers;

    public UUID getReadingId() { return readingId; }
    public void setReadingId(UUID readingId) { this.readingId = readingId; }
    public List<Integer> getAnswers() { return answers; }
    public void setAnswers(List<Integer> answers) { this.answers = answers; }
}
