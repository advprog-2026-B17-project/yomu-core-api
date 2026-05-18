package com.yomu.core.controller;

import com.yomu.core.entity.Question;
import com.yomu.core.entity.Reading;
import com.yomu.core.repository.QuestionRepository;
import com.yomu.core.repository.ReadingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final ReadingRepository readingRepository;

    public QuestionController(QuestionRepository questionRepository, ReadingRepository readingRepository) {
        this.questionRepository = questionRepository;
        this.readingRepository = readingRepository;
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<Question>> getQuestionsByReading(@PathVariable UUID readingId) {
        return ResponseEntity.ok(questionRepository.findByReadingId(readingId));
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionRequest request) {
        Reading reading = readingRepository.findById(request.getReadingId())
                .orElseThrow(() -> new RuntimeException("Reading not found"));

        Question question = new Question();
        question.setReading(reading);
        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());

        question = questionRepository.save(question);
        return ResponseEntity.ok(question);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable UUID id, @Valid @RequestBody QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());

        question = questionRepository.save(question);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public static class QuestionRequest {
        @NotNull
        private UUID readingId;
        @NotNull
        private String questionText;
        @NotNull
        private List<String> options;
        @NotNull
        private Integer correctAnswer;
        private String explanation;

        public UUID getReadingId() { return readingId; }
        public void setReadingId(UUID readingId) { this.readingId = readingId; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public Integer getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(Integer correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}
