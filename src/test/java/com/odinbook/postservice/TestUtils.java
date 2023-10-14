package com.odinbook.postservice;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import com.odinbook.postservice.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    public Long createRandomAccount(){
        String fullName = getSaltString();
        String userName = getSaltString();
        String email = getSaltString()+"@gmail.com";
        String roles = "ROLE_USER";
        String password = "password";
        entityManager
                .createNativeQuery("INSERT INTO accounts (fullname, username, email, roles, password)" +
                        " VALUES (:fullname,:username,:email,:roles,:password)")
                .setParameter("fullname",fullName)
                .setParameter("username",userName)
                .setParameter("email",email)
                .setParameter("roles",roles)
                .setParameter("password",password)
                .executeUpdate();




        return (Long) entityManager.createNativeQuery("SELECT id FROM accounts WHERE username = :username")
                .setParameter("username",userName)
                .getSingleResult();
    }
    public void deleteAccounts(){
        entityManager
                .createNativeQuery("DELETE FROM accounts WHERE id > 1")
                .executeUpdate();
    }

    public Post createRandomPost(){
        Long accountId = createRandomAccount();
        Post post = new Post();
        post.setAccountId(accountId);
        post.setContent(getSaltString());
        post.setVisibleToFollowers(true);
        post.setFriendsVisibilityType(false);

        return postRepository.saveAndFlush(post);
    }

    public Comment createRandomComment(){
        Long accountId = createRandomAccount();
        Post post = createRandomPost();
        Comment comment = new Comment();
        comment.setAccountId(accountId);
        comment.setPost(post);
        comment.setContent(getSaltString());

        return commentRepository.saveAndFlush(comment);
    }

    public Post createPost(Long accountId){
        Post post = new Post();
        post.setAccountId(accountId);
        post.setContent(getSaltString());
        post.setVisibleToFollowers(true);
        post.setFriendsVisibilityType(false);

        return postRepository.saveAndFlush(post);
    }

    public Post createRandomPostNotVisibleToFollowers(){
        Long accountId = createRandomAccount();
        Post post = new Post();
        post.setAccountId(accountId);
        post.setContent(getSaltString());
        post.setVisibleToFollowers(false);
        post.setFriendsVisibilityType(false);

        return postRepository.saveAndFlush(post);
    }




    public Comment createComment(Long accountId,Post post){
        Comment comment = new Comment();
        comment.setAccountId(accountId);
        comment.setPost(post);
        comment.setContent(getSaltString());

        return commentRepository.saveAndFlush(comment);


    }


}
