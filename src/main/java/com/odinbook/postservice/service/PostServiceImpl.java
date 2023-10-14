package com.odinbook.postservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.PostRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MessageChannel notificationRequest;
    private final ImageService imageService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           @Qualifier("notificationRequest") MessageChannel notificationRequest,
                           ImageService imageService) {
        this.postRepository = postRepository;
        this.notificationRequest = notificationRequest;
        this.imageService = imageService;
    }

    @Override
    public Post createPost(Post post){

        post.setId(postRepository.saveAndFlush(post).getId());

        try{
            imageService.createBlobs(
                    post.getId().toString(),
                    post.getImageList());
            String newContent = imageService.injectImagesToHTML(post.getContent(), post.getImageList());
            post.setContent(newContent);
        }
        catch (RuntimeException exception){
            exception.printStackTrace();
        }

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","newPost")
                .build();

        notificationRequest.send(postMessage);
        return postRepository.saveAndFlush(post);
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findPostById(Long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public List<Post> findPostsByAccountId(Long accountId) {
        return postRepository.findPostsByAccountId(accountId);
    }

    @Override
    public Optional<Post> updatePost(Post newPost){

     return postRepository.findById(newPost.getId())
             .map((oldPost)->{

                 try{
                     imageService.createBlobs(
                             newPost.getId().toString(),
                             newPost.getImageList());
                     String newContent = imageService.injectImagesToHTML(
                             newPost.getContent(),
                             newPost.getImageList());

                     newPost.setContent(newContent);
                 }
                 catch (RuntimeException exception){
                     exception.printStackTrace();
                 }

                 return postRepository.saveAndFlush(newPost);
             });
    }

    @Override
    public void deletePostById(Long postId) throws NoSuchElementException{

        Post post = findPostById(postId)
                .orElseThrow();

        imageService.deleteImages(post.getId().toString());

        postRepository.deleteById(postId);

    }

    @Override
    public void addLike(Long accountId, Long postId) throws NoSuchElementException {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        postRepository.addLike(accountId,postId);

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","addLike")
                .build();

        notificationRequest.send(postMessage);

    }

    @Override
    public void removeLike(Long accountId, Long postId) throws NoSuchElementException {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        postRepository.removeLike(accountId,postId);

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","removeLike")
                .build();

        notificationRequest.send(postMessage);

    }
}
