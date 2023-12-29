package com.odinbook.postservice.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PostNotNullValidator implements ConstraintValidator<PostNotNull, String> {

    private final PostService postService;

    @Autowired
    public PostNotNullValidator(PostService postService) {
        this.postService = postService;
    }

    @Override
    public boolean isValid(String postJson, ConstraintValidatorContext constraintValidatorContext) {
        Post post;
        try{
            post = new ObjectMapper().readValue(postJson, Post.class);
        }
        catch(JsonProcessingException exception){
            exception.printStackTrace();
            return false;
        }

        return postService
                .findPostById(post.getId())
                .isPresent();
    }
}
