package com.library.module.book.service.impl;

import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.library.common.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.library.module.book.entity.Book;
import com.library.module.book.repository.BookRepository;
import com.library.module.book.service.BookService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String DEFAULT_BOOK_IMAGE = "default.jpg";
    private static final long MAX_BOOK_IMAGE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public Book saveBook(Book book, MultipartFile image) {
        validateBook(book);

        book.setBookName(book.getBookName().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setDescription(book.getDescription() == null ? "" : book.getDescription().trim());

        Integer discountPercent = book.getDiscount() == null ? 0 : book.getDiscount();
        book.setDiscount(discountPercent);
        book.setDiscountPrice(calculateDiscountPrice(book.getPrice(), discountPercent));

        if (book.getCreatedDate() == null) {
            book.setCreatedDate(LocalDateTime.now());
        }

        if (book.getIsActive() == null) {
            book.setIsActive(true);
        }

        book.setImage(storeBookImage(image, DEFAULT_BOOK_IMAGE));

        return bookRepository.save(book);
    }


    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Integer id) {
        Book book = bookRepository.findById(id).orElse(null);
        return book;
    }

    @Override
    public List<Book> getAllActiveBooks(String category, String publisher) {
        List<Book> books = null;

        if (ObjectUtils.isEmpty(category) && ObjectUtils.isEmpty(publisher)) {
            books = bookRepository.findByIsActiveTrue();
        } else if (!ObjectUtils.isEmpty(category) && !ObjectUtils.isEmpty(publisher)) {
            books = bookRepository.findByCategoryAndPublisherAndIsActiveTrue(category, publisher);
        } else if (!ObjectUtils.isEmpty(category)) {
            books = bookRepository.findByCategoryAndIsActiveTrue(category);
        } else if (!ObjectUtils.isEmpty(publisher)) {
            books = bookRepository.findByPublisherAndIsActiveTrue(publisher);
        }

        if (books != null) {
            for (Book b : books) {

                if (b.getImage() == null || b.getImage().isEmpty()) {
                    b.setImage("default.jpg");
                } else if (b.getImage().startsWith("http")) {
                    b.setImage(b.getImage());
                } else {
                    b.setImage(b.getImage());
                }
            }
        }

        return books;
    }

    @Override
    public Boolean deleteBook(Integer id) {
        Book book = bookRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(book)) {
            book.setIsActive(false);
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    @Override
    public Book updateBook(Book book, MultipartFile image) {

        Book dbBook = getBookById(book.getId());

        if (dbBook == null) {
            throw new RuntimeException("Book not found");
        }

        dbBook.setBookName(book.getBookName());
        dbBook.setDescription(book.getDescription());
        dbBook.setAuthor(book.getAuthor());
        dbBook.setCategory(book.getCategory());
        dbBook.setPublisher(book.getPublisher());

        if (book.getPrice() == null || book.getPrice() <= 0) {
            throw new RuntimeException("Giá phải > 0");
        }
        dbBook.setPrice(book.getPrice());

        if (book.getStock() == null || book.getStock() < 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }
        dbBook.setStock(book.getStock());

        dbBook.setIsActive(book.getIsActive());
        dbBook.setIsbn(book.getIsbn());

        Integer discountPercent = book.getDiscount() == null ? 0 : book.getDiscount();

        if (discountPercent < 0 || discountPercent > 100) {
            throw new RuntimeException("Discount không hợp lệ");
        }

        dbBook.setDiscount(discountPercent);
        dbBook.setDiscountPrice(calculateDiscountPrice(book.getPrice(), discountPercent));

        dbBook.setImage(storeBookImage(image, dbBook.getImage()));


        return bookRepository.save(dbBook);
    }

    @Override
    public List<Book> searchBook(String ch) {
        return bookRepository.searchActiveBooks(ch);
    }

    @Override
    public Page<Book> getAllBooksPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("createdDate")));
        return bookRepository.findAll(pageable);
    }

    @Override
    public Page<Book> searchBookPagination(Integer pageNo, Integer pageSize, String ch) {
        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        pageSize = Math.min(pageSize, 50);

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
        String keyword = ch == null ? "" : ch.trim();

        return bookRepository.searchBooksForAdmin(keyword, pageable);
    }

    @Override
    public Page<Book> getAllActiveBookPagination(Integer pageNo, Integer pageSize, String category, String publisher,
                                                 String sortField, String sortOrder, Double minPrice, Double maxPrice) {

        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 8 : pageSize;
        pageSize = Math.min(pageSize, 24);

        if (sortField == null || sortField.isBlank()) {
            sortField = "createdDate";
        }

        if (!List.of("createdDate", "bookName", "discountPrice", "averageRating").contains(sortField)) {
            sortField = "createdDate";
        }

        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            sortOrder = "desc";
        }

        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Book> pageBook = null;

        if (ObjectUtils.isEmpty(category) && ObjectUtils.isEmpty(publisher)) {
            if (minPrice == null && maxPrice == null) {
                pageBook = bookRepository.findByIsActiveTrue(pageable);
            } else if (minPrice != null && maxPrice != null) {
                pageBook = bookRepository.findByDiscountPriceBetweenAndIsActiveTrue(minPrice, maxPrice, pageable);
            } else if (minPrice != null) {
                pageBook = bookRepository.findByDiscountPriceGreaterThanEqualAndIsActiveTrue(minPrice, pageable);
            } else {
                pageBook = bookRepository.findByDiscountPriceLessThanEqualAndIsActiveTrue(maxPrice, pageable);
            }
        }

        else if (!ObjectUtils.isEmpty(category) && !ObjectUtils.isEmpty(publisher)) {
            if (minPrice == null && maxPrice == null) {
                pageBook = bookRepository.findByCategoryAndPublisherAndIsActiveTrue(category, publisher, pageable);
            } else if (minPrice != null && maxPrice != null) {
                pageBook = bookRepository.findByDiscountPriceBetweenAndCategoryAndPublisherAndIsActiveTrue(minPrice, maxPrice, category, publisher, pageable);
            } else if (minPrice != null) {
                pageBook = bookRepository.findByDiscountPriceGreaterThanEqualAndCategoryAndPublisherAndIsActiveTrue(minPrice, category, publisher, pageable);
            } else {
                pageBook = bookRepository.findByDiscountPriceLessThanEqualAndCategoryAndPublisherAndIsActiveTrue(maxPrice, category, publisher, pageable);
            }
        }

        else if (!ObjectUtils.isEmpty(category)) {
            if (minPrice == null && maxPrice == null) {
                pageBook = bookRepository.findByCategoryAndIsActiveTrue(category, pageable);
            } else if (minPrice != null && maxPrice != null) {
                pageBook = bookRepository.findByDiscountPriceBetweenAndCategoryAndIsActiveTrue(minPrice, maxPrice, category, pageable);
            } else if (minPrice != null) {
                pageBook = bookRepository.findByDiscountPriceGreaterThanEqualAndCategoryAndIsActiveTrue(minPrice, category, pageable);
            } else {
                pageBook = bookRepository.findByDiscountPriceLessThanEqualAndCategoryAndIsActiveTrue(maxPrice, category, pageable);
            }
        }

        else if (!ObjectUtils.isEmpty(publisher)) {
            if (minPrice == null && maxPrice == null) {
                pageBook = bookRepository.findByPublisherAndIsActiveTrue(publisher, pageable);
            } else if (minPrice != null && maxPrice != null) {
                pageBook = bookRepository.findByDiscountPriceBetweenAndPublisherAndIsActiveTrue(minPrice, maxPrice, publisher, pageable);
            } else if (minPrice != null) {
                pageBook = bookRepository.findByDiscountPriceGreaterThanEqualAndPublisherAndIsActiveTrue(minPrice, publisher, pageable);
            } else {
                pageBook = bookRepository.findByDiscountPriceLessThanEqualAndPublisherAndIsActiveTrue(maxPrice, publisher, pageable);
            }
        }

        return pageBook;
    }

    @Override
    public Page<Book> searchActiveBookPagination(Integer pageNo, Integer pageSize,
                                                 String category,
                                                 String publisher,
                                                 String ch,
                                                 String sortField,
                                                 String sortOrder,
                                                 Double minPrice,
                                                 Double maxPrice) {
        pageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        pageSize = pageSize == null || pageSize < 1 ? 8 : pageSize;
        pageSize = Math.min(pageSize, 24);

        if (sortField == null || sortField.isBlank()) {
            sortField = "createdDate";
        }

        if (!List.of("createdDate", "bookName", "discountPrice", "averageRating").contains(sortField)) {
            sortField = "createdDate";
        }

        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            sortOrder = "desc";
        }

        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        String keyword = ch == null ? "" : ch.trim();
        String cleanCategory = StringUtils.hasText(category) ? category.trim() : null;
        String cleanPublisher = StringUtils.hasText(publisher) ? publisher.trim() : null;

        return bookRepository.searchActiveBooksWithFilters(
                keyword,
                cleanCategory,
                cleanPublisher,
                minPrice,
                maxPrice,
                pageable
        );
    }

    @Override
    public List<Book> getSimilarBooks(Integer bookId, Integer limit) {
        Book book = getBookById(bookId);

        if (book == null || Boolean.FALSE.equals(book.getIsActive())) {
            return Collections.emptyList();
        }

        int resultLimit = limit == null || limit <= 0 ? 6 : limit;
        Pageable pageable = PageRequest.of(0, resultLimit);

        return bookRepository.findSimilarBooks(
                book.getId(),
                book.getCategory(),
                book.getPublisher(),
                book.getAuthor(),
                pageable
        );
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new RuntimeException("Sách không hợp lệ");
        }

        if (book.getBookName() == null || book.getBookName().trim().isBlank()) {
            throw new RuntimeException("Tên sách không được để trống");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isBlank()) {
            throw new RuntimeException("Tác giả không được để trống");
        }

        if (book.getPrice() == null || book.getPrice() <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }

        if (book.getStock() == null || book.getStock() < 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        Integer discount = book.getDiscount() == null ? 0 : book.getDiscount();

        if (discount < 0 || discount > 100) {
            throw new RuntimeException("Discount không hợp lệ");
        }
    }

    private Integer calculateDiscountPrice(Integer price, Integer discountPercent) {
        int safeDiscount = discountPercent == null ? 0 : discountPercent;
        int discountAmount = (int) (price * (safeDiscount / 100.0));
        return price - discountAmount;
    }

    private String storeBookImage(MultipartFile file, String currentImage) {
        if (file == null || file.isEmpty()) {
            return currentImage == null || currentImage.isBlank() ? DEFAULT_BOOK_IMAGE : currentImage;
        }

        validateBookImage(file);

        String extension = getImageExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get(uploadPath).resolve("books").normalize();

        try {
            Files.createDirectories(uploadDir);

            Path targetPath = uploadDir.resolve(fileName).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new RuntimeException("Đường dẫn upload ảnh sách không hợp lệ");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh sách thất bại", e);
        }
    }

    private void validateBookImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh");
        }

        if (file.getSize() > MAX_BOOK_IMAGE_SIZE) {
            throw new RuntimeException("Ảnh không được vượt quá 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Chỉ cho phép upload ảnh JPG, JPEG, PNG hoặc WEBP");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        if (originalName.isBlank() || originalName.contains("..")) {
            throw new RuntimeException("Tên file ảnh không hợp lệ");
        }

        String extension = getImageExtension(originalName);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Định dạng ảnh không hợp lệ");
        }
    }

    private String getImageExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new RuntimeException("File ảnh sách phải có phần mở rộng");
        }

        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }
}
