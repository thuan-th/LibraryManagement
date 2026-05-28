package com.library.module.admin.controller;

import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.order.entity.BookOrder;
import com.library.module.order.enums.OrderStatus;
import com.library.module.order.service.OrderService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import com.library.common.service.AsyncMailService;
import com.library.util.CommonUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class OrderManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AsyncMailService asyncMailService;

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
            }
        }

        List<Category> activeCategories = categoryService.getAllActiveCategory();
        model.addAttribute("categorys", activeCategories);
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model,
                               @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        pageSize = Math.min(pageSize, 50);

        Page<BookOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);

        addOrderPaginationAttributes(model, page, pageSize);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("srch", false);
        model.addAttribute("orderId", "");

        return "/admin/orders";
    }

    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam Integer id,
                                    @RequestParam Integer st,
                                    @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                    @RequestParam(name = "orderId", defaultValue = "") String orderId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderStatus status = OrderStatus.fromId(st);
            BookOrder updatedOrder = orderService.updateOrderStatus(id, status);

            try {
                asyncMailService.sendOrderMail(updatedOrder, status.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            session.setAttribute("succMsg", "Cập nhật trạng thái thành công");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMsg", "Trạng thái không hợp lệ");
        } catch (IllegalStateException e) {
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Lỗi hệ thống");
        }

        String keyword = orderId == null ? "" : orderId.trim();

        if (!keyword.isBlank()) {
            redirectAttributes.addAttribute("orderId", keyword);
            return "redirect:/admin/search-order";
        }

        redirectAttributes.addAttribute("pageNo", pageNo);
        redirectAttributes.addAttribute("pageSize", pageSize);
        return "redirect:/admin/orders";
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<Integer, Integer>> getMonthlyRevenue(
            @RequestParam(name = "year", defaultValue = "2026") int year) {
        Map<Integer, Integer> monthlyRevenue = orderService.getMonthlyRevenue(year);
        return ResponseEntity.ok(monthlyRevenue);
    }

    @GetMapping("/daily-revenue")
    public ResponseEntity<Map<Integer, Integer>> getDailyRevenue(@RequestParam int year,
                                                                 @RequestParam int month) {
        Map<Integer, Integer> dailyRevenue = orderService.getDailyRevenue(year, month);
        return ResponseEntity.ok(dailyRevenue);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.getOrderStatistics(startDate, endDate));
    }

    @GetMapping("/search-order")
    public String searchOrder(@RequestParam(name = "orderId", defaultValue = "") String orderId,
                              Model model,
                              HttpSession session,
                              @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        String keyword = orderId == null ? "" : orderId.trim();

        if (keyword.isBlank()) {
            return "redirect:/admin/orders";
        }

        BookOrder order = orderService.getOrdersByOrderId(keyword);

        if (ObjectUtils.isEmpty(order)) {
            session.setAttribute("errorMsg", "Không thấy mã vận đơn");
            model.addAttribute("orderDtls", null);
            model.addAttribute("totalElements", 0);
        } else {
            model.addAttribute("orderDtls", order);
            model.addAttribute("totalElements", 1);
        }

        model.addAttribute("srch", true);
        model.addAttribute("orderId", keyword);
        model.addAttribute("pageNo", 0);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", 0);
        model.addAttribute("isFirst", true);
        model.addAttribute("isLast", true);
        model.addAttribute("startPage", 1);
        model.addAttribute("endPage", 1);

        return "/admin/orders";
    }

    private void addOrderPaginationAttributes(Model model, Page<BookOrder> page, Integer pageSize) {
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber();

        int startPage = Math.max(1, currentPage + 1 - 2);
        int endPage = Math.min(totalPages, currentPage + 1 + 2);

        if (totalPages > 0 && endPage - startPage < 4) {
            if (startPage == 1) {
                endPage = Math.min(totalPages, startPage + 4);
            } else if (endPage == totalPages) {
                startPage = Math.max(1, endPage - 4);
            }
        }

        model.addAttribute("pageNo", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
    }
}
