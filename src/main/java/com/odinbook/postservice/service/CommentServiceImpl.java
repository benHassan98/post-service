package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final MessageChannel notificationRequest;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                             @Qualifier("notificationRequest") MessageChannel notificationRequest) {
        this.commentRepository = commentRepository;
        this.notificationRequest = notificationRequest;
    }

    @Transactional
    @Override
    public Comment createComment(Comment comment) {
        Comment savedComment = commentRepository.saveAndFlush(comment);
        Message<Comment> commentMessage = MessageBuilder
                .withPayload(savedComment)
                .setHeader("notificationType","newComment")
                .build();

        notificationRequest.send(commentMessage);
        return savedComment;
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
    public void deleteCommentById(Long commentId, Long deletingAccountId) throws NoSuchElementException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        commentRepository.deleteById(commentId);

        if(!comment.getAccountId().equals(deletingAccountId)){
            Object commentRemovalObject = new Object(){
                private final Comment removedComment = comment;
                private final Long  accountId = deletingAccountId;
            };


            Message<Object> commentMessage = MessageBuilder
                    .withPayload(commentRemovalObject)
                    .setHeader("notificationType","removeComment")
                    .build();

            notificationRequest.send(commentMessage);
        }
    }



}
