package com.odinbook.postservice.dto;

import java.sql.Date;

import com.odinbook.postservice.model.Post;

import jakarta.validation.constraints.NotNull;

public class UpdatePostDto extends PostDto {

  @NotNull
  private Long id;

  @NotNull
  private Date updatedAt;

  @Override
  public Post getPost() {
    Post post = new Post();
    post.setId(getId());
    post.setContent(getContent());

    return post;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

}
