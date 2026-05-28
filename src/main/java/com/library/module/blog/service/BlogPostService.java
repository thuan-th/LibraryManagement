package com.library.module.blog.service;

import com.library.module.blog.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BlogPostService {

    List<BlogPost> getAllPosts();

    BlogPost saveBlogPost(BlogPost blog, MultipartFile image);

    Page<BlogPost> getAllBlogPostPagination(Integer pageNo, Integer pageSize);

    Page<BlogPost> searchBlogPostPagination(Integer pageNo, Integer pageSize, String ch);

    BlogPost getPostById(Long id);

    Boolean deletePost(Long id);

    BlogPost updatePost(BlogPost post, MultipartFile image);
}
