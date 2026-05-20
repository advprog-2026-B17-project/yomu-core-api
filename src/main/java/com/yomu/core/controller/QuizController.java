package com.yomu.core.controller;

import com.yomu.core.dto.QuizResult;
import com.yomu.core.dto.QuizSubmission;
import com.yomu.core.dto.QuestionDTO;
import com.yomu.core.entity.Reading;
import com.yomu.core.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/readings")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<Reading>> getAllReadings() {
        return ResponseEntity.ok(quizService.getAllReadings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reading> getReading(@PathVariable UUID id) {
        return quizService.getReadingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestions(@PathVariable UUID id) {
        return ResponseEntity.ok(quizService.getQuestionDTOsForReading(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<QuizResult> submitQuiz(
            @PathVariable UUID id,
            @Valid @RequestBody QuizSubmission submission,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        submission.setReadingId(id);
        return ResponseEntity.ok(quizService.submitQuiz(userId, submission));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Boolean> getReadingStatus(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(quizService.hasCompletedReading(userId, id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> markReadingComplete(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        if (quizService.hasCompletedReading(userId, id)) {
            return ResponseEntity.status(409).build();
        }
        quizService.markReadingComplete(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reading> createReading(@RequestBody Map<String, String> body, Authentication auth) {
        Reading reading = new Reading();
        reading.setTitle(body.get("title"));
        reading.setContent(body.get("content"));
        reading.setCreatedBy(UUID.fromString((String) auth.getPrincipal()));
        reading = quizService.saveReading(reading, body.get("categoryName"));
        return ResponseEntity.ok(reading);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReading(@PathVariable UUID id, Authentication auth) {
        quizService.deleteReading(id);
        return ResponseEntity.ok().build();
    }
}
