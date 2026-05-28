package com.library.module.comment.service.impl;

import com.library.module.book.entity.Book;
import com.library.module.comment.entity.Comment;
import com.library.module.user.entity.User;
import com.library.module.book.repository.BookRepository;
import com.library.module.comment.repository.CommentRepository;
import com.library.module.user.repository.UserRepository;
import com.library.module.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<Comment> getCommentsByBook(int bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
        return commentRepository.findByBook(book);
    }

    @Override
    public List<Comment> getReplies(int parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));
        return commentRepository.findByParentComment(parentComment);
    }

    @Override
    public Comment addComment(int bookId, int userId, String content, Integer parentCommentId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Comment comment = new Comment();
        comment.setBook(book);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCommentDate(LocalDateTime.now());

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

            if (parentComment.getBook().getId() != bookId) {
                throw new RuntimeException("Bình luận cha không thuộc sách này");
            }

            comment.setParentComment(parentComment);
        }

        return commentRepository.save(comment);
    }
}
