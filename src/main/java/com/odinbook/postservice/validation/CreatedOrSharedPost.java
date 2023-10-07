package com.odinbook.postservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreatedOrSharedPostValidator.class)
public @interface CreatedOrSharedPost {
    String message() default "You must Provide a Content to post";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
