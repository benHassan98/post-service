package com.odinbook.postservice.service;



import com.odinbook.postservice.DTO.ImageDTO;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.LikeNotificationRecord;
import com.odinbook.postservice.record.PostRecord;
import com.odinbook.postservice.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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

    @PersistenceContext
    private EntityManager entityManager;
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
            imageService.createBlobs(post.getImageList());
        }
        catch (RuntimeException exception){
            exception.printStackTrace();
        }
        PostRecord postRecord = new PostRecord(
                post.getId(),
                post.getAccountId(),
                Objects.nonNull(post.getSharedFromPost()),
                post.getVisibleToFollowers(),
                post.getFriendsVisibilityType(),
                post.getVisibleToFriendList()
        );

        Message<PostRecord> notificationMessage = MessageBuilder
                .withPayload(postRecord)
                .setHeader("notificationType","newPost")
                .build();

        notificationRequest.send(notificationMessage);
        return postRepository.saveAndFlush(post);
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findPostById(Long postId) {
        return postRepository.findById(postId).filter(post->!post.getDeleted());
    }

    @Override
    public List<Post> findPostsByAccountId(Long accountId) {
        return postRepository.findPostsByAccountId(accountId);
    }

    @Override
    public Optional<Post> updatePost(Post newPost){

     return postRepository.findById(newPost.getId())
             .filter(post->!post.getDeleted())
             .map((oldPost)->{

                 try{
                     imageService.createBlobs(newPost.getImageList());

                     imageService.deleteUnusedImages(oldPost.getContent(),newPost.getContent());
                 }
                 catch (RuntimeException exception){
                     exception.printStackTrace();
                 }

                 return postRepository.saveAndFlush(newPost);
             });
    }

    @Override
    @Transactional
    public void deletePostById(Long postId) throws NoSuchElementException{

        Post post = findPostById(postId)
                .filter(p->!p.getDeleted())
                .orElseThrow();

        imageService.deleteImages(post.getContent());

        entityManager
                .createNativeQuery("UPDATE posts SET is_deleted = 1, content= 'This post is deleted' WHERE id = :postId")
                .setParameter("postId",postId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void addLike(Long accountId, Long postId) throws NoSuchElementException {
        Post post = postRepository.findById(postId)
                .filter(p->!p.getDeleted())
                .orElseThrow();

        entityManager
                .createNativeQuery("INSERT INTO likes VALUES (:accountId,:postId)")
                .setParameter("accountId",accountId)
                .setParameter("postId",postId)
                .executeUpdate();


        Message<LikeNotificationRecord> notificationMessage = MessageBuilder
                .withPayload(new LikeNotificationRecord(postId, post.getAccountId(), accountId))
                .setHeader("notificationType","newLike")
                .build();

        notificationRequest.send(notificationMessage);

    }

    @Override
    @Transactional
    public void removeLike(Long accountId, Long postId){


        entityManager
                .createNativeQuery("DELETE FROM likes WHERE account_id=:accountId AND post_id=:postId")
                .setParameter("accountId",accountId)
                .setParameter("postId",postId)
                .executeUpdate();

    }
}
