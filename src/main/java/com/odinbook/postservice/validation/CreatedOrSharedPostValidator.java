package com.odinbook.postservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class CreatedOrSharedPostValidator implements ConstraintValidator<CreatedOrSharedPost,PostForm> {
    @Override
    public boolean isValid(PostForm postForm, ConstraintValidatorContext constraintValidatorContext) {
        return !Objects.isNull(postForm.getSharedFromPost()) || !postForm.getContent().isEmpty();
    }
}
