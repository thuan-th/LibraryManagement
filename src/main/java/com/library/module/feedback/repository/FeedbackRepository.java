package com.library.module.feedback.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.library.module.feedback.entity.Feedback;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    List<Feedback> findAllByOrderBySubmittedAtDesc();

    List<Feedback> findTop6ByIsDisplayedTrueOrderBySubmittedAtDesc();
}
