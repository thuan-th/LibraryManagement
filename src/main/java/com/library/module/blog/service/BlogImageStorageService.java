package com.library.module.blog.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BlogImageStorageService {

    String storeThumbnail(MultipartFile file, String currentImage) throws IOException;

    String storeContentImage(MultipartFile file) throws IOException;
}
