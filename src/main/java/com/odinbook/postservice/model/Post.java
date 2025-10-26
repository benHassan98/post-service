package com.odinbook.postservice.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "account_id")
  private Long accountId;

  @Column(name = "content")
  private String content;

  @Column(name = "shared_from_posts")
  private List<Long> sharedFromPostList = new ArrayList<>();

  @Transient
  private Long likesCount = 0l;

  @Transient
  private Long commentsCount = 0l;

  @Transient
  private List<byte[]> imageByteList = new ArrayList<>();

  @Column(name = "content_history")
  private List<String> contentHistory = new ArrayList<>();

  @Column(name = "update_time_history")
  private List<Timestamp> updateTimeHistory = new ArrayList<>();

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  @Column(name = "created_date")
  @CreationTimestamp
  private Timestamp createdDate;

  public Timestamp getCreatedDate() {
    return createdDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<Long> getSharedFromPostList() {
    return sharedFromPostList;
  }

  public void setSharedFromPostList(List<Long> sharedFromPostList) {
    this.sharedFromPostList = sharedFromPostList;
  }

  public Long getLikesCount() {
    return likesCount;
  }

  public void setLikesCount(Long likesCount) {
    this.likesCount = likesCount;
  }

  public Long getCommentsCount() {
    return commentsCount;
  }

  public void setCommentsCount(Long commentsCount) {
    this.commentsCount = commentsCount;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public List<byte[]> getImageByteList() {
    return imageByteList;
  }

  public Boolean addImageByte(byte[] byteArr) {
    return this.imageByteList.add(byteArr);
  }

}
