package com.library.module.rating.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.module.rating.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    List<Rating> findByBookId(Integer bookId);

    Optional<Rating> findByBookIdAndUserId(Integer bookId, Integer userId);
}