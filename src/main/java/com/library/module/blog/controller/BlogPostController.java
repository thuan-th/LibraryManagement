package com.library.module.blog.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

import com.library.module.blog.entity.BlogPost;
import com.library.module.user.entity.User;
import com.library.module.blog.service.BlogPostService;
import com.library.module.feedback.service.FeedbackService;
import com.library.module.user.service.UserService;
import com.library.module.blog.service.BlogImageStorageService;
import org.springframework.http.MediaType;

@Controller
public class BlogPostController {

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private BlogImageStorageService blogImageStorageService;

    @ModelAttribute
    public void getUserDetails(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            User userDtls = userService.getUserByEmail(email);
            if (userDtls != null) {
                m.addAttribute("user", userDtls);
            }
        }
        m.addAttribute("feedbacks", feedbackService.getAllFeedbacks());

        List<BlogPost> allActivePost = blogPostService.getAllPosts();
        m.addAttribute("posts", allActivePost);
    }

    @GetMapping("/blog_list")
    public String posts(Model m,
                        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                        @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
                        @RequestParam(name = "sortField", defaultValue = "createdDate:desc") String sortParam,
                        @RequestParam(defaultValue = "") String ch) {

        String[] sortParams = sortParam.split(":");
        String sortField = sortParams[0];
        String sortOrder = sortParams.length > 1 ? sortParams[1] : "asc";


        Page<BlogPost> page = null;
        if (ch != null && ch.length() > 0) {
            page = blogPostService.searchBlogPostPagination(pageNo, pageSize, ch);
        } else {
            page = blogPostService.getAllBlogPostPagination(pageNo, pageSize);
        }

        List<BlogPost> posts = page.getContent();
        m.addAttribute("posts", posts);
        m.addAttribute("postsSize", posts.size());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        m.addAttribute("sortField", sortField);
        m.addAttribute("ch", ch);

        return "blog_list";
    }

    @GetMapping("/admin/add_blog_post")
    public String add_blog_post(Model m) {
        return "admin/add_blog_post";
    }

    @PostMapping("/admin/savePost")
    public String savePost(@ModelAttribute BlogPost blog,
                              @RequestParam("file") MultipartFile image,
                              HttpSession session) {
        try {
            blogPostService.saveBlogPost(blog, image);
            session.setAttribute("succMsg", "Bài đăng được thêm thành công");
            return "redirect:/admin/admin_blog_list";
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/add_blog_post";
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Không thêm được bài đăng");
            return "redirect:/admin/add_blog_post";
        }
    }

    @GetMapping("/admin/admin_blog_list")
    public String loadViewBlogPost(Model m, @RequestParam(defaultValue = "") String ch,
                                   @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<BlogPost> page = null;
        if (ch != null && ch.length() > 0) {
            page = blogPostService.searchBlogPostPagination(pageNo, pageSize, ch);
        } else {
            page = blogPostService.getAllBlogPostPagination(pageNo, pageSize);
        }
        m.addAttribute("blogs", page.getContent());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        return "admin/admin_blog_list";
    }


    @GetMapping("/blog_list/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        BlogPost post = blogPostService.getPostById(id);
        model.addAttribute("post", post);
        return "single_blog_post";
    }

    @PostMapping("/admin/deletePost/{id}")
    public String deletePost(@PathVariable Long id, HttpSession session) {
        Boolean deleted = blogPostService.deletePost(id);

        if (deleted) {
            session.setAttribute("succMsg", "Bài đăng được xóa khỏi hệ thống");
        } else {
            session.setAttribute("errorMsg", "Có lỗi xảy ra");
        }

        return "redirect:/admin/admin_blog_list";
    }

    @GetMapping("/admin/editPost/{id}")
    public String editPost(@PathVariable Long id, Model m) {
        m.addAttribute("post", blogPostService.getPostById(id));
        return "admin/edit_blog";
    }

    @PostMapping("/admin/updatePost")
    public String updatePost(@ModelAttribute BlogPost post,
                             @RequestParam("file") MultipartFile image,
                             HttpSession session) {
        try {
            blogPostService.updatePost(post, image);
            session.setAttribute("succMsg", "Bài đăng được cập nhật thành công");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Có lỗi xảy ra");
        }

        return "redirect:/admin/editPost/" + post.getId();
    }

    @PostMapping(value = "/admin/blog/upload-content-image", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String uploadBlogContentImage(@RequestParam("upload") MultipartFile file,
                                         @RequestParam("CKEditorFuncNum") String callbackNumber) {
        String safeCallbackNumber = callbackNumber == null ? "0" : callbackNumber.replaceAll("[^0-9]", "");

        if (safeCallbackNumber.isBlank()) {
            safeCallbackNumber = "0";
        }

        try {
            String imageUrl = escapeForJs(blogImageStorageService.storeContentImage(file));

            return "<script>window.parent.CKEDITOR.tools.callFunction("
                    + safeCallbackNumber
                    + ", '"
                    + imageUrl
                    + "', '');</script>";

        } catch (Exception e) {
            String message = escapeForJs(e.getMessage() == null ? "Upload ảnh thất bại." : e.getMessage());

            return "<script>window.parent.CKEDITOR.tools.callFunction("
                    + safeCallbackNumber
                    + ", '', '"
                    + message
                    + "');</script>";
        }
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("</", "<\\/")
                .replace("<", "\\x3C")
                .replace(">", "\\x3E")
                .replace("&", "\\x26");
    }


}
