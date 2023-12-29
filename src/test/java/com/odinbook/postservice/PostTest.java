package com.odinbook.postservice;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.CommentRecord;
import com.odinbook.postservice.record.LikeNotificationRecord;
import com.odinbook.postservice.record.PostRecord;
import com.odinbook.postservice.repository.PostRepository;
import com.odinbook.postservice.service.PostService;
import com.odinbook.postservice.validation.PostForm;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    @Qualifier("notificationRequest")
    private MessageChannel notificationRequest;
    


    @BeforeEach
    public void beforeEach(){
        postRepository.deleteAll();
        testUtils.deleteAccounts();

        Mockito
                .when(notificationRequest.send(any()))
                .thenReturn(true);

    }
    @AfterEach
    public void afterEach(){
        postRepository.deleteAll();
        testUtils.deleteAccounts();
    }

    @Test
    public void createPost() throws Exception{

        Long accountId = testUtils.createRandomAccount();

        mockMvc.perform(
                post("/create")
                        .queryParam("accountId",accountId.toString())
                        .queryParam("content","Hello World")
                        .queryParam("isVisibleToFollowers",String.valueOf(true))
                        .queryParam("friendsVisibilityType",String.valueOf(false))
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    public void createSharedPost() throws Exception{
        Post randomPost = testUtils.createRandomPost();
        Long accountId = testUtils.createRandomAccount();

        String randomPostJson = new ObjectMapper().writeValueAsString(randomPost);
        mockMvc.perform(
                post("/create")
                        .queryParam("accountId",accountId.toString())
                        .queryParam("content","Hello World")
                        .queryParam("sharedFromPost",randomPostJson)
                        .queryParam("isVisibleToFollowers",String.valueOf(true))
                        .queryParam("friendsVisibilityType",String.valueOf(false))
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());


    }
    @Test
    public void findPostsByAccountId() throws Exception{
        Post post1 = testUtils.createRandomPost();
        Post post2 = testUtils.createRandomPost();
        Post post3 = testUtils.createRandomPost();
        Post post4 = testUtils.createPost(post1.getAccountId());

        Long account1 = post1.getAccountId();
        Long account2 = post2.getAccountId();
        Long account3 = post3.getAccountId();

        testUtils.addFriends(account1,account2);
        testUtils.addFriends(account2,account3);
        testUtils.addFriends(account1,account3);

        MvcResult mvcResult = mockMvc.perform(
                get("/account/"+account1)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();


        List<Post> postList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {

        });

        assertEquals(4, postList.size());
        assertEquals(post1.getId(),postList.get(0).getId());
        assertEquals(post2.getId(),postList.get(1).getId());
        assertEquals(post3.getId(),postList.get(2).getId());
        assertEquals(post4.getId(),postList.get(3).getId());


    }

    @Test
    public void findAll()throws Exception{
        testUtils.createRandomPost();
        testUtils.createRandomPost();
        testUtils.createRandomPost();

        MvcResult mvcResult = mockMvc.perform(
                get("/all")
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        List<Post> postList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(3, postList.size());
    }

    @Test
    public void findPostById()throws Exception{
        Post post = testUtils.createRandomPost();


        MvcResult mvcResult = mockMvc.perform(
                        get("/" + post.getId())
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        Post resPost = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Post.class);

        assertEquals(post.getId(), resPost.getId());
    }


    @Test
    public void findPostsVisibleToFollowers() throws Exception{
        Long account = testUtils.createRandomAccount();
        Post post1 = testUtils.createRandomPost();
        Post post2 = testUtils.createRandomPost();
        Post post3 = testUtils.createRandomPostNotVisibleToFollowers();


        testUtils.addFollower(account,post1.getAccountId());
        testUtils.addFollower(account,post2.getAccountId());
        testUtils.addFollower(account,post3.getAccountId());

        MvcResult mvcResult = mockMvc.perform(
                        get("/account/" + account)
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        List<Post> postList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertEquals(2,postList.size());
        assertEquals(post1.getId(),postList.get(0).getId());
        assertEquals(post2.getId(),postList.get(1).getId());


    }


    @Test
    public void findPostsVisibleToFriends() throws Exception{

        Long account = testUtils.createRandomAccount();

        Post post1 = testUtils.createRandomPost();

        Post post2 = testUtils.createRandomPost();
        post2.setVisibleToFriendList(List.of(account));


        Post post3 = testUtils.createRandomPost();
        post3.setFriendsVisibilityType(true);

        Post post4 = testUtils.createRandomPost();
        post4.setFriendsVisibilityType(true);
        post4.setVisibleToFriendList(List.of(account));

        testUtils.addFriends(account,post1.getAccountId());
        testUtils.addFriends(account,post2.getAccountId());
        testUtils.addFriends(account,post3.getAccountId());
        testUtils.addFriends(account,post4.getAccountId());

        postRepository.saveAllAndFlush(List.of(post1,post2,post3,post4));


        MvcResult mvcResult = mockMvc.perform(
                        get("/account/" + account)
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();


        List<Post> postList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });


        assertEquals(2,postList.size());
        assertEquals(post1.getId(),postList.get(0).getId());
        assertEquals(post4.getId(),postList.get(1).getId());



    }


    @Test
    public void deletePostById()throws Exception{
        Post post = testUtils.createRandomPost();
        mockMvc.perform(
                        delete("/" + post.getId())
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/" + post.getId())
                                .characterEncoding("utf-8")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());


    }

//    @Test
//    public void ts() throws IOException {
//
//        testUtils.createRandomPost();
//        testUtils.createRandomPost();
//        testUtils.createRandomPost();
//        testUtils.createRandomPost();
//        testUtils.createRandomPost();
//
//
//    }





}
