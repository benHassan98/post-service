package com.odinbook.postservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {

    public void deleteImages(String content);
    public void deleteUnusedImages(String oldContent, String newContent);
}
