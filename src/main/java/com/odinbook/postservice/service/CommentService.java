package com.odinbook.postservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.model.Comment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface CommentService {
    public Comment createComment(Comment comment);
    public void sendNewCommentToUsers(Comment comment) throws JsonProcessingException;
    public Optional<Comment> findCommentById(Long commentId);
    public List<Comment> findCommentsByPostId(Long postId);
    public List<Comment> findCommentsByAccountId(Long accountId);
    public void deleteCommentById(Long commentId) throws NoSuchElementException;
    public void sendRemovedCommentIdToUsers(Long commentId) throws NoSuchElementException;


}
