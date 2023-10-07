package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment createComment(Comment comment) {

        return commentRepository.saveAndFlush(comment);
    }

    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public List<Comment> findCommentsByPostId(Long postId) {
        return commentRepository.findCommentsByPostId(postId);
    }

    @Override
    public List<Comment> findCommentsByAccountId(Long accountId) {
        return commentRepository.findCommentsByAccountId(accountId);
    }

    @Override
    public void deleteCommentById(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentsByPostId(Long postId) {
        commentRepository.deleteCommentsByPostId(postId);
    }
}
