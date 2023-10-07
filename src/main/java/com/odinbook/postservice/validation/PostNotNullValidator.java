package com.odinbook.postservice.validation;

import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostNotNullValidator implements ConstraintValidator<PostNotNull, Post> {

    private final PostService postService;

    @Autowired
    public PostNotNullValidator(PostService postService) {
        this.postService = postService;
    }

    @Override
    public boolean isValid(Post post, ConstraintValidatorContext constraintValidatorContext) {

        return postService
                .findPostById(post.getId())
                .isPresent();
    }
}
