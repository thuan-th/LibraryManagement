package com.library.module.book.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.library.module.book.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book,Integer> {

    List<Book> findByIsActiveTrue();

    Page<Book> findByIsActiveTrue(Pageable pageable);

    List<Book> findByCategory(String category);

    List<Book> findByPublisher(String publisher);

    List<Book> findByCategoryAndPublisherAndIsActiveTrue(String category, String publisher);

    Page<Book> findByCategoryAndPublisherAndIsActiveTrue(Pageable pageable, String category, String publisher);

    List<Book> findByCategoryAndIsActiveTrue(String category);

    Page<Book> findByCategoryAndIsActiveTrue(Pageable pageable, String category);

    List<Book> findByPublisherAndIsActiveTrue(String publisher);

    Page<Book> findByPublisherAndIsActiveTrue(Pageable pageable,String publisher);

    Page<Book> findByCategory(Pageable pageable,String category);

    Page<Book> findByBookNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2, Pageable pageable);

    Page<Book> findByCategoryAndPublisherAndIsActiveTrue(String category, String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceGreaterThanEqualAndCategoryAndPublisherAndIsActiveTrue(Double minPrice, String category, String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceLessThanEqualAndCategoryAndPublisherAndIsActiveTrue(Double maxPrice, String category, String publisher, Pageable pageable);

    Page<Book> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

    Page<Book> findByDiscountPriceGreaterThanEqualAndCategoryAndIsActiveTrue(Double minPrice, String category, Pageable pageable);

    Page<Book> findByDiscountPriceLessThanEqualAndCategoryAndIsActiveTrue(Double maxPrice, String category, Pageable pageable);

    Page<Book> findByPublisherAndIsActiveTrue(String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceGreaterThanEqualAndPublisherAndIsActiveTrue(Double minPrice, String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceLessThanEqualAndPublisherAndIsActiveTrue(Double maxPrice, String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceBetweenAndIsActiveTrue(Double minPrice, Double maxPrice, Pageable pageable);

    Page<Book> findByDiscountPriceBetweenAndCategoryAndIsActiveTrue(Double minPrice, Double maxPrice, String category, Pageable pageable);

    Page<Book> findByDiscountPriceBetweenAndPublisherAndIsActiveTrue(Double minPrice, Double maxPrice, String publisher, Pageable pageable);

    Page<Book> findByDiscountPriceGreaterThanEqualAndIsActiveTrue(Double minPrice, Pageable pageable);

    Page<Book> findByDiscountPriceLessThanEqualAndIsActiveTrue(Double maxPrice, Pageable pageable);

    Page<Book> findByDiscountPriceBetweenAndCategoryAndPublisherAndIsActiveTrue(Double minPrice, Double maxPrice, String category, String publisher, Pageable pageable);

    Page<Book> findByIsActiveTrueAndBookNameContainingIgnoreCaseAndCategoryContainingIgnoreCaseAndPublisherContainingIgnoreCase(
            String ch, String ch2, String ch3, Pageable pageable);

    Page<Book> findByIsActiveTrueAndBookNameContainingIgnoreCaseOrIsActiveTrueAndCategoryContainingIgnoreCase(String ch, String ch2,
                                                                                               Pageable pageable);

    @Query("""
        SELECT b 
        FROM Book b
        WHERE b.isActive = true
        AND (LOWER(b.bookName) LIKE LOWER(CONCAT('%', :ch, '%'))
             OR LOWER(b.category) LIKE LOWER(CONCAT('%', :ch, '%')))
        """)
    List<Book> searchActiveBooks(@Param("ch") String ch);

    @Query("""
        SELECT b 
        FROM Book b
        WHERE b.isActive = true
        AND (
            LOWER(b.bookName) LIKE LOWER(CONCAT('%', :ch, '%'))
            OR LOWER(b.category) LIKE LOWER(CONCAT('%', :ch, '%'))
        )
    """)
    Page<Book> searchActiveBooks(@Param("ch") String ch, Pageable pageable);

    @Query("""
    SELECT b
    FROM Book b
    WHERE b.isActive = true
    AND b.id <> :bookId
    AND (
        (:category IS NOT NULL AND b.category = :category)
        OR (:publisher IS NOT NULL AND b.publisher = :publisher)
        OR (:author IS NOT NULL AND b.author = :author)
    )
    ORDER BY
        CASE WHEN b.category = :category THEN 0 ELSE 1 END,
        CASE WHEN b.publisher = :publisher THEN 0 ELSE 1 END,
        CASE WHEN b.author = :author THEN 0 ELSE 1 END,
        b.sold DESC,
        b.createdDate DESC
    """)
    List<Book> findSimilarBooks(@Param("bookId") Integer bookId,
                                @Param("category") String category,
                                @Param("publisher") String publisher,
                                @Param("author") String author,
                                Pageable pageable);

    @Query("""
    SELECT b
    FROM Book b
    WHERE b.isActive = true
    AND (:category IS NULL OR b.category = :category)
    AND (:publisher IS NULL OR b.publisher = :publisher)
    AND (:minPrice IS NULL OR b.discountPrice >= :minPrice)
    AND (:maxPrice IS NULL OR b.discountPrice <= :maxPrice)
    AND (
        LOWER(b.bookName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Book> searchActiveBooksWithFilters(@Param("keyword") String keyword,
                                            @Param("category") String category,
                                            @Param("publisher") String publisher,
                                            @Param("minPrice") Double minPrice,
                                            @Param("maxPrice") Double maxPrice,
                                            Pageable pageable);

    @Query("""
    SELECT b
    FROM Book b
    WHERE LOWER(b.bookName) LIKE LOWER(CONCAT('%', :ch, '%'))
       OR LOWER(b.author) LIKE LOWER(CONCAT('%', :ch, '%'))
       OR LOWER(b.category) LIKE LOWER(CONCAT('%', :ch, '%'))
       OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :ch, '%'))
    """)
    Page<Book> searchBooksForAdmin(@Param("ch") String ch, Pageable pageable);
}
