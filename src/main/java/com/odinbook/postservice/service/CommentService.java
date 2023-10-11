package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Comment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface CommentService {
    public Comment createComment(Comment comment);
    public Optional<Comment> findCommentById(Long commentId);
    public List<Comment> findCommentsByPostId(Long postId);
    public List<Comment> findCommentsByAccountId(Long accountId);
    public void deleteCommentById(Long commentId, Long deletingAccountId) throws NoSuchElementException;


}
