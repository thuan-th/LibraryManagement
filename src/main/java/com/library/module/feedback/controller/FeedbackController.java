package com.library.module.feedback.controller;

import com.library.module.feedback.entity.Feedback;
import com.library.module.feedback.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/feedback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody Feedback feedback) {
        if (feedback == null) {
            return badRequest("Dữ liệu phản hồi không hợp lệ");
        }

        String name = normalize(feedback.getName());
        String content = normalize(feedback.getContent());

        if (name.isBlank()) {
            return badRequest("Họ tên không được để trống");
        }

        if (name.length() > 100) {
            return badRequest("Họ tên không được vượt quá 100 ký tự");
        }

        if (content.isBlank()) {
            return badRequest("Nội dung phản hồi không được để trống");
        }

        if (content.length() > 1000) {
            return badRequest("Nội dung phản hồi không được vượt quá 1000 ký tự");
        }

        String userEmail = feedback.getUserEmail();

        if (userEmail != null && !userEmail.isBlank()) {
            userEmail = userEmail.trim();

            if (userEmail.length() > 150) {
                return badRequest("Email không được vượt quá 150 ký tự");
            }

            if (!userEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return badRequest("Email không đúng định dạng");
            }

            feedback.setUserEmail(userEmail);
        } else {
            feedback.setUserEmail(null);
        }

        feedback.setName(name);
        feedback.setContent(content);

        try {
            feedbackService.saveFeedback(feedback);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra khi gửi phản hồi. Vui lòng thử lại."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Phản hồi đã được gửi thành công!"
        ));
    }

    @GetMapping("/admin/feedback_list")
    public String getAllFeedbacks(Model model) {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("totalElements", feedbacks.size());

        return "/admin/feedback_list";
    }

    @PostMapping("/admin/feedback/display/{id}")
    public String updateFeedbackDisplayStatus(@PathVariable Integer id,
                                              @RequestParam Boolean displayed,
                                              HttpSession session) {
        try {
            boolean displayStatus = Boolean.TRUE.equals(displayed);

            feedbackService.updateDisplayStatus(id, displayStatus);

            if (displayStatus) {
                session.setAttribute("succMsg", "Feedback đã được hiển thị ở trang chủ");
            } else {
                session.setAttribute("succMsg", "Feedback đã được ẩn khỏi trang chủ");
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/feedback_list";
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
