package com.odinbook.postservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.model.Post;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface PostService {
    public Post createPost(Post post) throws JsonProcessingException;
    public List<Post> findAll();
    public Optional<Post> findPostById(Long id);
    public List<Post> findPostsByAccountId(Long accountId);
    public List<Post> findPublicPostsByAccountId(Long accountId, Boolean isAccountProfile);
    public Optional<Post> updatePost(Post newPost);
    public void deletePostById(Long postId) throws NoSuchElementException;
    public void addLike(Long accountId,Long postId) throws NoSuchElementException;
    public void removeLike(Long accountId,Long postId);


}
