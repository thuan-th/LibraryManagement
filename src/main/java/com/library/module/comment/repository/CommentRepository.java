package com.library.module.comment.repository;

import com.library.module.book.entity.Book;
import com.library.module.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository  extends JpaRepository<Comment, Integer> {

    List<Comment> findByBook(Book book);

    List<Comment> findByParentComment(Comment parentComment);

}
