package com.library.module.rating.service;

import java.util.List;

import com.library.module.rating.entity.Rating;

public interface RatingService {

    public Rating addRating(Integer bookId, Integer userId, int score, String review);

    public List<Rating> getRatingsForBook(Integer bookId);

    public double getAverageRating(Integer bookId);

}