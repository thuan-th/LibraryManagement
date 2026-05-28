package com.library.module.admin.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.library.module.book.entity.Book;
import com.library.module.category.entity.Category;
import com.library.module.publisher.entity.Publisher;
import com.library.module.user.entity.User;
import com.library.module.book.service.BookService;
import com.library.module.category.service.CategoryService;
import com.library.module.publisher.service.PublisherService;
import com.library.module.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;


@Controller
@RequestMapping("/admin")
public class BookManagementController {

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

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

    @GetMapping("/books/add")
    public String loadAddBook(Model model) {
        List<Category> categories = categoryService.getAllCategory();
        List<Publisher> publishers = publisherService.getAllPublisher();

        model.addAttribute("categories", categories);
        model.addAttribute("publishers", publishers);

        return "admin/add_book";
    }

    @PostMapping("/saveBook")
    public String saveProduct(@ModelAttribute Book book,
                              @RequestParam("file") MultipartFile image,
                              HttpSession session) {
        try {
            Book saved = bookService.saveBook(book, image);

            if (saved != null) {
                session.setAttribute("succMsg", "Thêm sách thành công");
            } else {
                session.setAttribute("errorMsg", "Không thể thêm sách");
            }

            return "redirect:/admin/books";
        } catch (Exception e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/books/add";
        }
    }

    @GetMapping("/books")
    public String loadViewBook(Model model,
                               @RequestParam(defaultValue = "") String ch,
                               @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        pageSize = Math.min(pageSize, 50);

        String keyword = ch == null ? "" : ch.trim();

        Page<Book> page;

        if (!keyword.isBlank()) {
            page = bookService.searchBookPagination(pageNo, pageSize, keyword);
        } else {
            page = bookService.getAllBooksPagination(pageNo, pageSize);
        }

        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber();

        int startPage = Math.max(1, currentPage + 1 - 2);
        int endPage = Math.min(totalPages, currentPage + 1 + 2);

        if (endPage - startPage < 4) {
            if (startPage == 1) {
                endPage = Math.min(totalPages, startPage + 4);
            } else if (endPage == totalPages) {
                startPage = Math.max(1, endPage - 4);
            }
        }

        model.addAttribute("books", page.getContent());
        model.addAttribute("ch", keyword);
        model.addAttribute("pageNo", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin/books";
    }

    @PostMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable int id, HttpSession session) {
        Boolean deleted = bookService.deleteBook(id);

        if (deleted) {
            session.setAttribute("succMsg", "Đã cập nhật thông tin");
        } else {
            session.setAttribute("errorMsg", "Có lỗi xảy ra");
        }

        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editBook(@PathVariable int id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("publishers", publisherService.getAllPublisher());

        return "admin/edit_book";
    }

    @PostMapping("/updateBook")
    public String updateBook(@ModelAttribute Book book,
                             @RequestParam("file") MultipartFile image,
                             HttpSession session) {
        try {
            bookService.updateBook(book, image);
            session.setAttribute("succMsg", "Cập nhật thành công");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/books";
    }
}
