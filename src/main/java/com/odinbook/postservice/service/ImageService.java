package com.odinbook.postservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    public void createBlobs(String dir, MultipartFile [] imageList);
    public byte[] findBlob(String blobName);
    public String injectImagesToHTML(String html,MultipartFile [] imageList);
    public void deleteImages(String dir,MultipartFile [] imageList);
}
