package com.odinbook.postservice.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.LikeNotificationRecord;
import com.odinbook.postservice.record.PostRecord;
import com.odinbook.postservice.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    @PersistenceContext
    private EntityManager entityManager;
    private final PostRepository postRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           StringRedisTemplate stringRedisTemplate
                           ) {
        this.postRepository = postRepository;
        this.stringRedisTemplate = stringRedisTemplate;

    }

    @Override
    public Post createPost(Post post) throws JsonProcessingException {

        post.setId(postRepository.saveAndFlush(post).getId());

        PostRecord postRecord = new PostRecord(
                post.getId(),
                post.getAccountId(),
                Objects.nonNull(post.getSharedFromPost()),
                post.getVisibleToFollowers(),
                post.getFriendsVisibilityType(),
                post.getVisibleToFriendList()
        );

        String postJson = new ObjectMapper().writeValueAsString(postRecord);

        stringRedisTemplate.convertAndSend("findNotifiedAccountsChannel", postJson);

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

        return postRepository
                .findPostsByAccountId(accountId)
                .stream()
                .filter(post->!post.getDeleted())
                .toList();
    }

    @Override
    public List<Post> findPublicPostsByAccountId(Long accountId, Boolean isAccountProfile) {
        return postRepository
                .findPostsByAccountId(accountId)
                .stream()
                .filter(
                        post->
                                post.getAccountId().equals(accountId) && (
                                        isAccountProfile || (post.getVisibleToFollowers() && !post.getFriendsVisibilityType())
                                ) && !post.getDeleted()
                )
                .toList();
    }

    @Override
    public Optional<Post> updatePost(Post newPost){

     return postRepository.findById(newPost.getId())
             .filter(post->!post.getDeleted())
             .map((oldPost)->postRepository.saveAndFlush(newPost));
    }

    @Override
    @Transactional
    public void deletePostById(Long postId) throws NoSuchElementException{

        findPostById(postId)
                .filter(p->!p.getDeleted())
                .orElseThrow();

        entityManager
                .createNativeQuery("UPDATE posts SET is_deleted = true, content= 'This post is deleted' WHERE id = :postId")
                .setParameter("postId",postId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void addLike(Long accountId, Long postId) throws NoSuchElementException{
        Post post = postRepository.findById(postId)
                .filter(p->!p.getDeleted())
                .orElseThrow();

        entityManager
                .createNativeQuery("INSERT INTO likes VALUES (:accountId,:postId)")
                .setParameter("accountId",accountId)
                .setParameter("postId",postId)
                .executeUpdate();

        LikeNotificationRecord likeNotificationRecord = new LikeNotificationRecord(postId, post.getAccountId(), accountId);

        String likeJson;
        try{
            likeJson = new ObjectMapper().writeValueAsString(likeNotificationRecord);
        }
        catch (JsonProcessingException exception){
            exception.printStackTrace();
            return;
        }

        stringRedisTemplate.convertAndSend("newLikeChannel", likeJson);

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
