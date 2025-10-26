package com.odinbook.postservice.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckImageMaxValidator.class)
public @interface CheckImageMax {
  String message() default "Images Count should not exceed 4";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
