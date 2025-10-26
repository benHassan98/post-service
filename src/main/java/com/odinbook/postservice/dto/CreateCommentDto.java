package com.odinbook.postservice.dto;

import com.odinbook.postservice.model.Comment;

import jakarta.validation.constraints.NotNull;

public class CreateCommentDto extends CommentDto {

  @NotNull
  private Long accountId;

  @NotNull
  private Long postId;

  @Override
  public Comment getComment() {
    Comment comment = new Comment();
    comment.setAccountId(getAccountId());
    comment.setPostId(getPostId());
    comment.setContent(getContent());

    return comment;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public Long getPostId() {
    return postId;
  }

  public void setPostId(Long postId) {
    this.postId = postId;
  }

}
