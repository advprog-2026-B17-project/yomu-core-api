package com.yomu.core.dto;

import java.util.List;
import java.util.UUID;

public class QuestionDTO {
    private UUID id;
    private String questionText;
    private List<String> options;

    public QuestionDTO() {}

    public QuestionDTO(UUID id, String questionText, List<String> options) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
