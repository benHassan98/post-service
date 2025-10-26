package com.odinbook.postservice;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import com.odinbook.postservice.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TestUtils {

  @PersistenceContext
  private EntityManager entityManager;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private CommentRepository commentRepository;

  public String getSaltString() {
    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    Random rnd = new Random();
    while (salt.length() < 18) { // length of the random string.
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }

    return salt.toString();
  }

  @Transactional
  public Long createRandomAccount() {
    String fullName = getSaltString();
    String userName = getSaltString();
    String email = getSaltString() + "@gmail.com";
    String password = "password";
    String picture = "dumb";

    entityManager
        .createNativeQuery("INSERT INTO accounts (fullname, username, email, password, picture_id)" +
            " VALUES (:fullname,:username,:email,:password,:pictureId)")
        .setParameter("fullname", fullName)
        .setParameter("username", userName)
        .setParameter("email", email)
        .setParameter("password", password)
        .setParameter("pictureId", picture)
        .executeUpdate();

    return Long.valueOf(entityManager.createNativeQuery("SELECT id FROM accounts WHERE username = :username")
        .setParameter("username", userName)
        .getSingleResult().toString());
  }

  @Transactional
  public void addFollower(Long followerId, Long followeeId) {

    entityManager
        .createNativeQuery("INSERT INTO followers VALUES (:follower_id, :followee_id)")
        .setParameter("follower_id", followerId)
        .setParameter("followee_id", followeeId)
        .executeUpdate();

  }

  @Transactional
  public void deleteAllAccounts() {
    entityManager
        .createNativeQuery("DELETE FROM accounts WHERE id > 0")
        .executeUpdate();
  }

  @Transactional
  public void deleteAllPosts() {
    entityManager
        .createNativeQuery("DELETE FROM posts WHERE id > 0")
        .executeUpdate();
  }

  @Transactional
  public void deleteAllComments() {
    entityManager
        .createNativeQuery("DELETE FROM comments WHERE id > 0")
        .executeUpdate();
  }

  @Transactional
  public Post createRandomPost() {
    Long accountId = createRandomAccount();
    Post post = new Post();
    post.setAccountId(accountId);
    post.setContent("iam " + getSaltString() + " <img src='dumb.jpg' alt='dumb' class='w-32 h-32'/>  ");

    return postRepository.saveAndFlush(post);
  }

  @Transactional
  public Comment createRandomComment() {
    Long accountId = createRandomAccount();
    Post post = createRandomPost();
    Comment comment = new Comment();
    comment.setAccountId(accountId);
    comment.setPostId(post.getId());
    comment.setContent(getSaltString());

    return commentRepository.saveAndFlush(comment);
  }

  public Post createPost(Long accountId) {
    Post post = new Post();
    post.setAccountId(accountId);
    post.setContent("Hello");

    return postRepository.saveAndFlush(post);
  }

  public Post createPost(Long accountId, String content) {
    Post post = new Post();
    post.setAccountId(accountId);
    post.setContent(content);

    return postRepository.saveAndFlush(post);
  }

  public Comment createComment(Long accountId, Long postId) {
    Comment comment = new Comment();
    comment.setAccountId(accountId);
    comment.setPostId(postId);
    comment.setContent(getSaltString());

    return commentRepository.saveAndFlush(comment);

  }

  public Comment createComment(Long accountId, Long postId, String content) {
    Comment comment = new Comment();
    comment.setAccountId(accountId);
    comment.setPostId(postId);
    comment.setContent(content);

    return commentRepository.saveAndFlush(comment);

  }
}
