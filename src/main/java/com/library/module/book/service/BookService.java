package com.library.module.book.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import com.library.module.book.entity.Book;

public interface BookService {

    public Book saveBook(Book book, MultipartFile image);

    public List<Book> getAllBooks();

    public Boolean deleteBook(Integer id);

    public Book getBookById(Integer id);

    public Book updateBook(Book product, MultipartFile file);

    public List<Book> getAllActiveBooks(String category, String publisher);

    public List<Book> searchBook(String ch);

    public List<Book> getSimilarBooks(Integer bookId, Integer limit);

    public Page<Book> searchActiveBookPagination(Integer pageNo, Integer pageSize,
                                                 String category,
                                                 String publisher,
                                                 String ch,
                                                 String sortField,
                                                 String sortOrder,
                                                 Double minPrice,
                                                 Double maxPrice);

    public Page<Book> searchBookPagination(Integer pageNo, Integer pageSize, String ch);

    public Page<Book> getAllBooksPagination(Integer pageNo, Integer pageSize);

    public Page<Book> getAllActiveBookPagination(Integer pageNo, Integer pageSize, String category, String publisher,
                                                 String sortField, String sortOrder, Double minPrice, Double maxPrice);

}
