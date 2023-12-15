package com.odinbook.postservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import com.odinbook.postservice.repository.PostRepository;
import com.odinbook.postservice.service.ImageServiceImpl;
import com.odinbook.postservice.service.STOMPServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentTest {
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @MockBean
    private ImageServiceImpl imageService;
    @MockBean
    private STOMPServiceImpl stompService;
    @MockBean
    @Qualifier("notificationRequest")
    private MessageChannel notificationRequest;


    @BeforeEach
    public void beforeEach(){
        commentRepository.deleteAll();
        postRepository.deleteAll();
        testUtils.deleteAccounts();

        Mockito
                .doNothing()
                .when(imageService)
                .createBlobs(any());

        Mockito
                .doNothing()
                .when(imageService)
                .deleteImages(anyString());
        Mockito
                .when(notificationRequest.send(any()))
                .thenReturn(true);
        Mockito
                .doNothing()
                .when(stompService)
                .sendNewCommentToAccounts(any());
        Mockito
                .doNothing()
                .when(stompService)
                .sendRemovedLikeToAccounts(any());

    }

    @AfterEach
    public void afterEach() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        testUtils.deleteAccounts();
    }


    @Test
    public void createComment()throws Exception{


        Long accountId = testUtils.createRandomAccount();
        Post post = testUtils.createPost(accountId);


        String postJson = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(post);

        mockMvc.perform(
                post("/comment/create")
                        .queryParam("accountId",accountId.toString())
                        .queryParam("postJson",postJson)
                        .queryParam("content","Hello")
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

    }

    @Test
    public void findCommentById()throws Exception{
        Post post = testUtils.createRandomPost();

        Comment comment = testUtils.createComment(post.getAccountId(),post);


        MvcResult mvcResult = mockMvc.perform(
                get("/comment/"+comment.getId())
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        Comment resComment = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(mvcResult.getResponse().getContentAsString(), Comment.class);


        assertEquals(comment.getId(),resComment.getId());

    }

    @Test
    public void deleteCommentById()throws Exception{
        Comment comment = testUtils.createRandomComment();
        mockMvc.perform(
                delete("/comment/"+comment.getId())
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/comment/"+comment.getId()).contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());

    }







}
