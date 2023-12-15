package com.odinbook.postservice.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.CommentRecord;
import com.odinbook.postservice.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.jsoup.UncheckedIOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final MessageChannel notificationRequest;
    private final STOMPService stompService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                             ImageService imageService,
                             @Qualifier("notificationRequest") MessageChannel notificationRequest,
                              STOMPService stompService) {
        this.commentRepository = commentRepository;
        this.imageService = imageService;
        this.notificationRequest = notificationRequest;
        this.stompService = stompService;
    }


    @Override
    public Comment createComment(Comment comment) {

        comment.setId(commentRepository.saveAndFlush(comment).getId());

        CommentRecord commentRecord = new CommentRecord(
                comment.getId(),
                comment.getPost().getAccountId(),
                comment.getPost().getId(),
                comment.getAccountId()
        );

        Message<CommentRecord> notificationMessage = MessageBuilder
                .withPayload(commentRecord)
                .setHeader("notificationType","newComment")
                .build();

        notificationRequest.send(notificationMessage);

        stompService.sendNewCommentToAccounts(comment);

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
    public void deleteCommentById(Long commentId) throws NoSuchElementException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        commentRepository.deleteById(commentId);

        imageService.deleteImages(comment.getContent());

        stompService.sendRemovedCommentToAccounts(comment);

    }




}
