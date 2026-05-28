package com.library.module.book.controller;

import java.util.List;

import com.library.module.category.entity.Category;
import com.library.module.comment.entity.Comment;
import com.library.module.book.entity.Book;
import com.library.module.book.service.BookService;
import com.library.module.publisher.entity.Publisher;
import com.library.module.publisher.service.PublisherService;
import com.library.module.category.service.CategoryService;
import com.library.module.comment.service.CommentService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Set;

@Controller
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PublisherService publisherService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdDate",
            "bookName",
            "discountPrice",
            "averageRating"
    );

    @GetMapping("/books")
    public String books(Model m,
                        @RequestParam(value = "category", defaultValue = "") String category,
                        @RequestParam(value = "publisher", defaultValue = "") String publisher,
                        @RequestParam(value = "priceRange", defaultValue = "") String priceRange,
                        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                        @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
                        @RequestParam(name = "sortField", defaultValue = "createdDate:desc") String sortParam,
                        @RequestParam(defaultValue = "") String ch) {

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramCategory", category);
        m.addAttribute("categories", categories);

        List<Publisher> publishers = publisherService.getAllActivePublisher();
        m.addAttribute("paramPublisher", publisher);
        m.addAttribute("publishers", publishers);

        Double minPrice = null;
        Double maxPrice = null;

        switch (priceRange) {
            case "under100":
                maxPrice = 100000.0;
                break;
            case "100to200":
                minPrice = 100000.0;
                maxPrice = 200000.0;
                break;
            case "200to300":
                minPrice = 200000.0;
                maxPrice = 300000.0;
                break;
            case "300to400":
                minPrice = 300000.0;
                maxPrice = 400000.0;
                break;
            case "400to500":
                minPrice = 400000.0;
                maxPrice = 500000.0;
                break;
            case "above500":
                minPrice = 500000.0;
                break;
        }

        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 8 : pageSize;
        pageSize = Math.min(pageSize, 24);

        String[] sortParams = sortParam.split(":");
        String sortField = sortParams.length > 0 ? sortParams[0] : "createdDate";
        String sortOrder = sortParams.length > 1 ? sortParams[1] : "desc";

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "createdDate";
        }

        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            sortOrder = "desc";
        }

        Page<Book> page;

        if (StringUtils.isEmpty(ch)) {
            page = bookService.getAllActiveBookPagination(
                    pageNo,
                    pageSize,
                    category,
                    publisher,
                    sortField,
                    sortOrder,
                    minPrice,
                    maxPrice
            );
        } else {
            page = bookService.searchActiveBookPagination(
                    pageNo,
                    pageSize,
                    category,
                    publisher,
                    ch,
                    sortField,
                    sortOrder,
                    minPrice,
                    maxPrice
            );
        }

        List<Book> books = page.getContent();

        m.addAttribute("books", books);
        m.addAttribute("booksSize", books.size());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        m.addAttribute("sortField", sortField);
        m.addAttribute("sortOrder", sortOrder);
        m.addAttribute("priceRange", priceRange);
        m.addAttribute("ch", ch);
        m.addAttribute("sortParam", sortField + ":" + sortOrder);

        return "book";
    }

    @GetMapping("/book/{id}")
    public String book(@PathVariable int id, Model m) {
        Book bookById = bookService.getBookById(id);

        if (bookById == null || Boolean.FALSE.equals(bookById.getIsActive())) {
            return "redirect:/books";
        }

        List<Comment> comments = commentService.getCommentsByBook(id);
        List<Book> similarBooks = bookService.getSimilarBooks(id, 8);

        m.addAttribute("book", bookById);
        m.addAttribute("comments", comments);
        m.addAttribute("similarBooks", similarBooks);

        return "view_book";
    }
}
