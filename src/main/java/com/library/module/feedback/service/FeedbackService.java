package com.library.module.feedback.service;

import com.library.module.feedback.entity.Feedback;
import java.util.List;

public interface FeedbackService {
    Feedback saveFeedback(Feedback feedback);
    List<Feedback> getAllFeedbacks();
    List<Feedback> getDisplayedFeedbacks();
    Feedback updateDisplayStatus(Integer id, Boolean displayed);
}
