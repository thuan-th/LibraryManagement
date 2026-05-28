package com.library.module.blog.service.impl;

import com.library.module.blog.entity.BlogPost;
import com.library.module.blog.repository.BlogPostRepository;
import com.library.module.blog.service.BlogImageStorageService;
import com.library.module.blog.service.BlogPostService;
import com.library.module.blog.util.BlogHtmlSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlogPostServiceImpl implements BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogHtmlSanitizer blogHtmlSanitizer;

    @Autowired
    private BlogImageStorageService blogImageStorageService;

    @Override
    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
    }

    @Override
    public BlogPost saveBlogPost(BlogPost blog, MultipartFile image) {
        try {
            validateBlogPost(blog);

            blog.setTitle(blog.getTitle().trim());
            blog.setAuthor(blog.getAuthor().trim());
            blog.setContents(blogHtmlSanitizer.sanitize(blog.getContents()));
            blog.setImage(blogImageStorageService.storeThumbnail(image, null));
            blog.setCreatedAt(LocalDateTime.now());
            blog.setUpdatedAt(LocalDateTime.now());

            return blogPostRepository.save(blog);
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể lưu ảnh bài viết.");
        }
    }

    @Override
    public Page<BlogPost> getAllBlogPostPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = createPageable(pageNo, pageSize);
        return blogPostRepository.findAll(pageable);
    }

    @Override
    public Page<BlogPost> searchBlogPostPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = createPageable(pageNo, pageSize);
        String keyword = ch == null ? "" : ch.trim();

        return blogPostRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public BlogPost getPostById(Long id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    @Override
    public Boolean deletePost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id).orElse(null);

        if (blogPost == null) {
            return false;
        }

        blogPostRepository.delete(blogPost);
        return true;
    }

    @Override
    public BlogPost updatePost(BlogPost post, MultipartFile image) {
        try {
            BlogPost dbPost = getPostById(post.getId());

            if (dbPost == null) {
                throw new IllegalArgumentException("Không tìm thấy bài viết.");
            }

            validateBlogPost(post);

            dbPost.setTitle(post.getTitle().trim());
            dbPost.setAuthor(post.getAuthor().trim());
            dbPost.setContents(blogHtmlSanitizer.sanitize(post.getContents()));
            dbPost.setImage(blogImageStorageService.storeThumbnail(image, dbPost.getImage()));
            dbPost.setUpdatedAt(LocalDateTime.now());

            return blogPostRepository.save(dbPost);
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể lưu ảnh bài viết.");
        }
    }

    private Pageable createPageable(Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo < 0 ? 0 : pageNo;
        int safePageSize = pageSize == null || pageSize <= 0 ? 8 : Math.min(pageSize, 50);

        return PageRequest.of(safePageNo, safePageSize, Sort.by(Sort.Order.desc("createdAt")));
    }

    private void validateBlogPost(BlogPost blog) {
        if (blog == null) {
            throw new IllegalArgumentException("Dữ liệu bài viết không hợp lệ.");
        }

        if (blog.getTitle() == null || blog.getTitle().trim().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề bài viết không được để trống.");
        }

        if (blog.getTitle().trim().length() > 255) {
            throw new IllegalArgumentException("Tiêu đề bài viết không được vượt quá 255 ký tự.");
        }

        if (blog.getAuthor() == null || blog.getAuthor().trim().isBlank()) {
            throw new IllegalArgumentException("Tác giả không được để trống.");
        }

        if (blog.getAuthor().trim().length() > 100) {
            throw new IllegalArgumentException("Tên tác giả không được vượt quá 100 ký tự.");
        }

        if (blogHtmlSanitizer.isBlankContent(blog.getContents())) {
            throw new IllegalArgumentException("Nội dung bài viết không được để trống.");
        }
    }
}
