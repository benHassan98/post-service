package com.odinbook.postservice.DTO;

import org.springframework.web.multipart.MultipartFile;

public class TestOb {
    public Long id;
    public MultipartFile file;
    public ImageDTO imageDTO;

    public ImageDTO getImageDTO() {
        return imageDTO;
    }

    public void setImageDTO(ImageDTO imageDTO) {
        this.imageDTO = imageDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
