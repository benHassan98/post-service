package com.odinbook.postservice.validation;

import org.jsoup.Jsoup;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckImageMaxValidator implements ConstraintValidator<CheckImageMax, String> {

  @Override
  public boolean isValid(String content, ConstraintValidatorContext constraintValidatorContext) {
    return Jsoup.parse(content).body().getElementsByTag("img").size() <= 4;
  }

}
