package com.library.module.admin.controller;

import com.library.module.book.entity.Book;
import com.library.module.book.service.BookService;
import com.library.module.order.entity.BookOrder;
import com.library.module.order.service.OrderService;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.order.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    public DashboardController(UserService userService,
                               CategoryService categoryService,
                               OrderService orderService,
                               BookService bookService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.bookService = bookService;
    }

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

    @GetMapping("/")
    public String index(Model model) {
        List<BookOrder> orders = orderService.getAllOrders();
        List<User> users = userService.getUsers("ROLE_USER");
        List<Book> books = bookService.getAllActiveBooks(null, null);

        int totalRevenue = 0;

        for (BookOrder order : orders) {
            if ("CANCELLED".equals(order.getStatus())) {
                continue;
            }

            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                continue;
            }

            if (order.getTotalAmount() != null) {
                totalRevenue += order.getTotalAmount();
            }
        }

        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalBooks", books.size());
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/index";
    }
}
