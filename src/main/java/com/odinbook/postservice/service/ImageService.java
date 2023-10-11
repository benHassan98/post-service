package com.odinbook.postservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public void createBlobs(String dir, MultipartFile [] imageList) throws RuntimeException;
    public String injectImagesToHTML(String html,MultipartFile [] imageList);
    public void deleteImages(String dir);
}
