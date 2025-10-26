package com.odinbook.postservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.odinbook.postservice.model.Post;

import jakarta.validation.constraints.NotNull;

public class CreatePostDto extends PostDto {

  @NotNull
  private Long accountId;

  private List<Long> sharedFromPostList = new ArrayList<>();

  @Override
  public Post getPost() {
    Post post = new Post();
    post.setAccountId(getAccountId());
    post.setContent(getContent());
    post.setSharedFromPostList(getSharedFromPostList());

    return post;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public List<Long> getSharedFromPostList() {
    return sharedFromPostList;
  }

  public void setSharedFromPostList(List<Long> sharedFromPostList) {
    this.sharedFromPostList = sharedFromPostList;
  }

}
