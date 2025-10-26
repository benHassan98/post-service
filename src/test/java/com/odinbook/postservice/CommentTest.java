package com.odinbook.postservice;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.CommentRepository;
import com.odinbook.service.ImageService;

import io.minio.MinioClient;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@SuppressWarnings("unused")
public class CommentTest {
  @Autowired
  private TestUtils testUtils;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private CommentRepository commentRepository;
  @Container
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
  @MockitoBean
  private StringRedisTemplate stringRedisTemplate;
  @MockitoBean
  private MinioClient minioClient;
  @MockitoSpyBean
  private ImageService imageService;

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @BeforeEach
  public void beforeEach() {
    testUtils.deleteAllComments();
    testUtils.deleteAllPosts();
    testUtils.deleteAllAccounts();
  }

  @AfterEach
  public void afterEach() {
    testUtils.deleteAllComments();
    testUtils.deleteAllPosts();
    testUtils.deleteAllAccounts();
  }

  @Test
  public void createComment() throws Exception {
    Post post = testUtils.createRandomPost();
    var jsonObject = new Object() {
      public Long accountId = post.getAccountId();
      public Long postId = post.getId();
      public String content = "<div>HEll</div><img src=\'123-456\'/><img src=\'456-789\' /> <img src=\'123-789\' />";
    };

    var jsonString = new ObjectMapper().writeValueAsString(jsonObject);

    mockMvc.perform(
        multipart("/comment/")
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .part(new MockPart("commentDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isOk());

  }

  @Test
  public void findCommentById() throws Exception {
    Post post = testUtils.createRandomPost();

    Comment comment = testUtils.createComment(post.getAccountId(), post.getId());

    MvcResult mvcResult = mockMvc.perform(
        get("/comment/" + comment.getId())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    Comment resComment = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(),
        Comment.class);

    assertEquals(comment.getId(), resComment.getId());

  }

  @Test
  public void findCommentByPost() throws Exception {
    Post post = testUtils.createRandomPost();

    List<Comment> commentList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      var accountId = testUtils.createRandomAccount();
      commentList.add(testUtils.createComment(accountId, post.getId()));
      Thread.sleep(10);
    }
    commentList = commentList.reversed();

    MvcResult mvcResult = mockMvc.perform(
        get("/comment/post/" + post.getId())
            .header("preTime", commentList.get(2).getCreatedDate().getTime())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    List<Comment> resList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(),
        new TypeReference<>() {
        });
    assertEquals(7, resList.size());
  }

  @Test
  public void deleteCommentById() throws Exception {
    Comment comment = testUtils.createRandomComment();
    var jsonObject = new Object() {
      public Long id = comment.getId();
    };

    var jsonString = new ObjectMapper().writeValueAsString(jsonObject);

    mockMvc.perform(
        delete("/comment/")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc.perform(
        get("/comment/" + comment.getId()))
        .andExpect(status().isNotFound());

  }

}
