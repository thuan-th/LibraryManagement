package com.library.common.controller;

import java.util.List;
import com.library.module.book.entity.Book;
import com.library.module.book.service.BookService;
import com.library.module.category.entity.Category;
import com.library.module.category.service.CategoryService;
import com.library.module.publisher.entity.Publisher;
import com.library.module.publisher.service.PublisherService;
import com.library.module.feedback.entity.Feedback;
import com.library.module.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/")
    public String index(Model m,
                        @RequestParam(value = "category", defaultValue = "") String category,
                        @RequestParam(value = "publisher", defaultValue = "") String publisher) {

        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categories", allActiveCategory);

        List<Publisher> allActivePublisher = publisherService.getAllActivePublisher();
        m.addAttribute("publishers", allActivePublisher);

        List<Book> allActivebook = bookService.getAllActiveBooks( category, publisher);
        m.addAttribute("books", allActivebook);

        List<Feedback> displayedFeedbacks = feedbackService.getDisplayedFeedbacks();
        m.addAttribute("displayedFeedbacks", displayedFeedbacks);

        return "index";
    }

    @GetMapping("/category")
    public String category() {
        return "category";
    }

    @GetMapping("/intro")
    public String intro() {
        return "intro";
    }

    @GetMapping("/publisher")
    public String publisher(Model m) {
        List<Publisher> publishers = publisherService.getAllActivePublisher();
        m.addAttribute("publishers", publishers);
        return "publisher";
    }
}
