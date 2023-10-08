package com.odinbook.postservice.entityListener;

import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.CommentService;
import com.odinbook.postservice.service.ImageService;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostListener {

    private final ImageService imageService;
    @Autowired
    public PostListener(ImageService imageService) {
        this.imageService = imageService;
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrPreUpdate(Post post) {

        imageService.createBlobs(
                post.getAccountId().toString()+post.getCreatedDate().getTime(),
                post.getImageList());
        String newContent = imageService.injectImagesToHTML(post.getContent(), post.getImageList());
        post.setContent(newContent);

    }
    @PreRemove
    public void preRemove(Post post) {
        imageService.deleteImages(post.getAccountId().toString()+post.getCreatedDate().getTime(),
                post.getImageList());

    }

}
