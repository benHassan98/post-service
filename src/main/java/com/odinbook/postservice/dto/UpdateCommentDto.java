package com.odinbook.postservice.dto;

import com.odinbook.postservice.model.Comment;

import jakarta.validation.constraints.NotNull;

public class UpdateCommentDto extends CommentDto {

  @NotNull
  private Long id;

  @Override
  public Comment getComment() {
    Comment comment = new Comment();
    comment.setId(getId());
    comment.setContent(getContent());

    return comment;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

}
