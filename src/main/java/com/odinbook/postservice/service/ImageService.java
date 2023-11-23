package com.odinbook.postservice.service;

import com.odinbook.postservice.DTO.ImageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public void createBlobs(List<ImageDTO> imageList) throws RuntimeException;
    public String injectImagesToHTML(String html, List<String> imageNameList);
    public void deleteImages(String content);
    public void deleteUnusedImages(String oldContent, String newContent);
}
