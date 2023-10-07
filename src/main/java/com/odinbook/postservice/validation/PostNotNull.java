package com.odinbook.postservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostNotNullValidator.class)
public @interface PostNotNull {
    String message() default "Post doesn't exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
