package com.odinbook.postservice.dto;

import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.validation.CheckImageMax;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public abstract class PostDto {

  @NotNull
  @NotEmpty
  @CheckImageMax
  private String content;

  public abstract Post getPost();

  public String getContent() {
    return content;
  }

  protected void setContent(String content) {
    this.content = content;
  }

}
