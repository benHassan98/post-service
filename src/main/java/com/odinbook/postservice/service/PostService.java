package com.odinbook.postservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.dto.CreatePostDto;
import com.odinbook.postservice.dto.UpdatePostDto;
import com.odinbook.postservice.model.Post;

public interface PostService {
  public Long create(CreatePostDto postDto, MultipartFile[] imageArr)
      throws JsonProcessingException;

  public List<Post> findAll();

  public Post findById(Long id) throws NoSuchElementException;

  public List<Post> findByAccountId(Long accountId, String preTimeStr);

  public List<Post> findFeed(Long accountId, String preTimeStr);

  public void update(UpdatePostDto postDto);

  public void deleteById(Long id) throws NoSuchElementException;

  public void addLike(Long accountId, Long postAccountId, Long postId) throws JsonProcessingException;

  public void removeLike(Long accountId, Long postId);

  public List<String> findImagesIds(Long id);

}
