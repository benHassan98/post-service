package com.odinbook.postservice.entityListener;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.service.ImageService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentListener {
    private final ImageService imageService;

    @Autowired
    public CommentListener(ImageService imageService) {
        this.imageService = imageService;
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrPreUpdate(Comment comment) {
        imageService.createBlobs(
                comment.getAccountId().toString()+comment.getCreatedDate().getTime(),
                comment.getImageList());
        String newContent = imageService.injectImagesToHTML(comment.getContent(), comment.getImageList());
        comment.setContent(newContent);
    }

    @PreRemove
    public void preRemove(Comment comment) {
        imageService.deleteImages(comment.getAccountId().toString()+comment.getCreatedDate().getTime());
    }

}
