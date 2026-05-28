package com.library.module.blog.repository;

import com.library.module.blog.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Page<BlogPost> findByTitleContainingIgnoreCase(String ch, Pageable pageable);
}