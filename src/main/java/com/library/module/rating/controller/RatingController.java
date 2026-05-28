package com.library.module.rating.controller;

import com.library.module.book.entity.Book;
import com.library.module.book.service.BookService;
import com.library.module.rating.entity.Rating;
import com.library.module.rating.service.RatingService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.getUserByEmail(principal.getName());
    }

    @GetMapping("/review/{id}")
    public String viewProductReviewPage(@PathVariable("id") Integer id, Model model) {
        Book book = bookService.getBookById(id);

        if (book == null || Boolean.FALSE.equals(book.getIsActive())) {
            return "redirect:/books";
        }

        double averageRating = ratingService.getAverageRating(id);
        List<Rating> ratings = ratingService.getRatingsForBook(id);

        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratings", ratings);
        model.addAttribute("book", book);

        return "/user/book-review";
    }

    @PostMapping("/user/review/save")
    public String addRating(@RequestParam Integer bookId,
                            @RequestParam int score,
                            @RequestParam String review,
                            Principal principal,
                            HttpSession session) {
        User user = getLoggedInUser(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        try {
            ratingService.addRating(bookId, user.getId(), score, review);
            session.setAttribute("succMsg", "Đánh giá của bạn đã được lưu");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/review/" + bookId;
    }
}
