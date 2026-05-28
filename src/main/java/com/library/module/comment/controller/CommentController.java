package com.library.module.comment.controller;

import com.library.module.comment.service.CommentService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.getUserByEmail(principal.getName());
    }

    @PostMapping("/book/{bookId}/comment")
    public String addComment(@PathVariable int bookId,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer parentCommentId,
                             Principal principal,
                             HttpSession session) {
        User user = getLoggedInUser(principal);

        if (user == null) {
            return "redirect:/signin";
        }

        if (content == null || content.trim().isEmpty()) {
            session.setAttribute("errorMsg", "Nội dung bình luận không được để trống.");
            return "redirect:/book/" + bookId;
        }

        try {
            commentService.addComment(bookId, user.getId(), content.trim(), parentCommentId);
            session.setAttribute("succMsg", "Đã thêm bình luận");
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/book/" + bookId;
    }
}