package com.odinbook.postservice.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{
    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String webPubSubConnectStr;

    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final MessageChannel notificationRequest;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                             ImageService imageService,
                             @Qualifier("notificationRequest") MessageChannel notificationRequest) {
        this.commentRepository = commentRepository;
        this.imageService = imageService;
        this.notificationRequest = notificationRequest;
    }


    @Override
    public Comment createComment(Comment comment) {

        comment.setId(commentRepository.saveAndFlush(comment).getId());

        try{
            imageService.createBlobs(
                    comment.getId().toString(),
                    comment.getImageList());
            String newContent = imageService.injectImagesToHTML(comment.getContent(), comment.getImageList());
            comment.setContent(newContent);
        }
        catch (RuntimeException exception){
            exception.printStackTrace();
        }


        Message<Comment> commentMessage = MessageBuilder
                .withPayload(comment)
                .setHeader("notificationType","newComment")
                .build();

        notificationRequest.send(commentMessage);
        try{
            this.sendNewCommentToUsers(comment);
        }
        catch (JsonProcessingException exception){
            exception.printStackTrace();
        }
        return commentRepository.saveAndFlush(comment);
    }

    @Override
    public void sendNewCommentToUsers(Comment comment) throws JsonProcessingException {
        String jsonString = new ObjectMapper().writeValueAsString(comment);

        new WebPubSubServiceClientBuilder()
                .connectionString(webPubSubConnectStr)
                .hub("posts")
                .buildClient()
                .sendToGroup(
                        comment.getPost().getId()+".newComment",
                        jsonString,
                        WebPubSubContentType.APPLICATION_JSON);

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

        imageService.deleteImages(comment.getId().toString());

        sendRemovedCommentIdToUsers(commentId);
    }

    @Override
    public void sendRemovedCommentIdToUsers(Long commentId) throws NoSuchElementException{

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        new WebPubSubServiceClientBuilder()
                .connectionString(webPubSubConnectStr)
                .hub("posts")
                .buildClient()
                .sendToGroup(
                        comment.getPost().getId()+".removeComment",
                        commentId.toString(),
                        WebPubSubContentType.TEXT_PLAIN);



    }


}
