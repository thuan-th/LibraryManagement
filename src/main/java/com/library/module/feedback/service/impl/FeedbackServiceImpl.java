package com.library.module.feedback.service.impl;

import com.library.module.feedback.entity.Feedback;
import com.library.module.feedback.repository.FeedbackRepository;
import com.library.module.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setDisplayed(false);

        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Override
    public List<Feedback> getDisplayedFeedbacks() {
        return feedbackRepository.findTop6ByIsDisplayedTrueOrderBySubmittedAtDesc();
    }

    @Override
    public Feedback updateDisplayStatus(Integer id, Boolean displayed) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy feedback"));

        feedback.setDisplayed(Boolean.TRUE.equals(displayed));

        return feedbackRepository.save(feedback);
    }
}
