package com.odinbook.postservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.CommentRecord;
import com.odinbook.postservice.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.print.DocFlavor;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final STOMPService stompService;
    private final StringRedisTemplate stringRedisTemplate;


    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              STOMPService stompService,
                              StringRedisTemplate stringRedisTemplate) {
        this.commentRepository = commentRepository;
        this.stompService = stompService;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public Comment createComment(Comment comment) throws JsonProcessingException {

        comment.setId(commentRepository.saveAndFlush(comment).getId());

        CommentRecord commentRecord = new CommentRecord(
                comment.getId(),
                comment.getPost().getAccountId(),
                comment.getPost().getId(),
                comment.getAccountId()
        );

        String commentJson = new ObjectMapper().writeValueAsString(commentRecord);

        stringRedisTemplate.convertAndSend("newCommentChannel", commentJson);

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

        stompService.sendRemovedCommentToAccounts(comment);

    }




}
