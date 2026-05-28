package com.library.module.rating.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.library.module.book.entity.Book;
import com.library.module.rating.entity.Rating;
import com.library.module.user.entity.User;
import com.library.module.book.repository.BookRepository;
import com.library.module.rating.repository.RatingRepository;
import com.library.module.user.repository.UserRepository;
import com.library.module.rating.service.RatingService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Rating addRating(Integer bookId, Integer userId, int score, String review) {
        if (score < 1 || score > 5) {
            throw new RuntimeException("Điểm đánh giá không hợp lệ");
        }

        if (review == null || review.trim().isEmpty()) {
            throw new RuntimeException("Nội dung đánh giá không được để trống");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Rating rating = ratingRepository.findByBookIdAndUserId(bookId, userId)
                .orElseGet(Rating::new);

        rating.setBook(book);
        rating.setUser(user);
        rating.setScore(score);
        rating.setReview(review.trim());
        rating.setCreatedAt(LocalDateTime.now());

        Rating savedRating = ratingRepository.save(rating);

        updateAverageRating(book);

        return savedRating;
    }

    private void updateAverageRating(Book book) {
        List<Rating> ratings = ratingRepository.findByBookId(book.getId());

        double averageRating = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);

        book.setAverageRating(averageRating);
        bookRepository.save(book);
    }

    @Override
    public List<Rating> getRatingsForBook(Integer bookId) {
        List<Rating> ratings = ratingRepository.findByBookId(bookId);

        ratings.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        return ratings;

    }

    @Override
    public double getAverageRating(Integer bookId) {
        List<Rating> ratings = ratingRepository.findByBookId(bookId);

        double averageRating = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);

        return Double.parseDouble(String.format("%.1f", averageRating));
    }
}
