package com.odinbook.postservice.dto;

import com.odinbook.postservice.model.Comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public abstract class CommentDto {

  public abstract Comment getComment();

  @NotNull
  @NotEmpty
  protected String content;

  protected String getContent() {
    return content;
  }

  protected void setContent(String content) {
    this.content = content;
  }

}
