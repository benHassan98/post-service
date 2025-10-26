package com.odinbook.postservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.model.Comment;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.web.multipart.MultipartFile;

public interface CommentService {
  public Long create(Comment comment, MultipartFile[] imageArr) throws JsonProcessingException;

  public Comment findById(Long id) throws NoSuchElementException;

  public List<Comment> findByPostId(Long postId, String preTimeStr);

  public void deleteById(Long id);

}
