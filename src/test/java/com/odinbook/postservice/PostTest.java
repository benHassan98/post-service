package com.odinbook.postservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.PostRepository;
import com.odinbook.service.ImageService;
import com.redis.testcontainers.RedisContainer;

import io.minio.MinioClient;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@SuppressWarnings("unused")
public class PostTest {
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private TestUtils testUtils;
  @Autowired
  private MockMvc mockMvc;
  @Container
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
  @Container
  private static RedisContainer redis = new RedisContainer("redis:8.2.2").withExposedPorts(6379);
  @MockitoSpyBean
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
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
  }

  @BeforeAll
  static void beforeAll() {
    postgres.start();
    redis.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
    redis.stop();
  }

  @BeforeEach
  public void beforeEach() {
    testUtils.deleteAllPosts();
    testUtils.deleteAllAccounts();
  }

  @AfterEach
  public void afterEach() {
    testUtils.deleteAllPosts();
    testUtils.deleteAllAccounts();
  }

  @Test
  public void createPost() throws Exception {

    Long postAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = postAccountId;
      public String content = "<div>HEll</div><img src=\'123-456\'/><img src=\'456-789\' /> <img src=\'123-789\' />";
    };
    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);

    mockMvc.perform(
        multipart("/")
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .part(new MockPart("postDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isOk());
  }

  @Test
  public void createPostWithEmptyContent() throws Exception {

    Long postAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = postAccountId;
      public String content = "";
    };
    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);

    mockMvc.perform(
        multipart("/")
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .part(new MockPart("postDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createSharedPost() throws Exception {
    List<Long> randomPostList = List.of(testUtils.createRandomPost().getId(), testUtils.createRandomPost().getId(),
        testUtils.createRandomPost().getId());
    AtomicInteger idx = new AtomicInteger();
    Long postAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = postAccountId;
      public String content = "<div>Hee</div><img src=\'11-33\'/>";
      public List<Long> sharedFromPostList = List.copyOf(randomPostList);
    };

    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    MvcResult mvcResult = mockMvc.perform(
        multipart("/")
            .file("imageArr", new byte[] { 1, 2, 3 })
            .part(new MockPart("postDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isOk())
        .andReturn();
    Long resPostId = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), Long.class);
    var savedSharedPostIdList = postRepository.findById(resPostId).get().getSharedFromPostList();
    assertEquals(savedSharedPostIdList.size(), randomPostList.size());
    savedSharedPostIdList.forEach(id -> {
      assertEquals(id, randomPostList.get(idx.getAndIncrement()));
    });
  }

  @Test
  public void createPostWithMaximuxImageCount() throws Exception {
    Long postAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = postAccountId;
      public String content = "<div>HEll</div><img src=\'123-456\'/><img src=\'456-789\' /><img src=\'456-789\' /><img src=\'456-789\' /><img src=\'123-789\' />";
    };
    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);

    mockMvc.perform(
        multipart("/")
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .file("imageArr", new byte[] { 1, 2, 3 })
            .part(new MockPart("postDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isBadRequest());

  }

  @Test
  public void createPostWithMaximuxImageSize() throws Exception {
    Long postAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = postAccountId;
      public String content = "<div>HEll</div><img src=\'123-456\'/>";
    };
    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    int randomBytesSize = 5 * 1024 * 1024;
    randomBytesSize++;
    byte[] randomBytes = new byte[randomBytesSize];
    new Random().nextBytes(randomBytes);

    mockMvc.perform(
        multipart("/")
            .file("imageArr", randomBytes)
            .part(new MockPart("postDto", "", jsonString.getBytes(), MediaType.APPLICATION_JSON)))
        .andExpect(status().isBadRequest());

  }

  @Test
  public void updatePost() throws Exception {
    Post randomPost = testUtils.createPost(testUtils.createRandomAccount(),
        "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>");
    var jsonObject = new Object() {
      public Long id = randomPost.getId();
      public String content = "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/>";
      public Date updatedAt = new Date();
    };

    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    mockMvc.perform(
        put("/")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void updatePostWithMaximumUpdateLimit() throws Exception {
    Post randomPost = testUtils.createPost(testUtils.createRandomAccount(),
        "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>");
    for (int i = 0; i < 5; i++) {
      var iStr = Long.toString(i);
      var jsonObject = new Object() {
        public Long id = randomPost.getId();
        public String content = iStr
            + "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/>";
        public Date updatedAt = new Date();
      };

      String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
      mockMvc.perform(
          put("/")
              .content(jsonString)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
    var jsonObject = new Object() {
      public Long id = randomPost.getId();
      public String content = "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/>";
      public Date updatedAt = new Date();
    };

    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    mockMvc.perform(
        put("/")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updatePostWithSameContent() throws Exception {
    Post randomPost = testUtils.createPost(testUtils.createRandomAccount(),
        "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>");
    var jsonObject = new Object() {
      public Long id = randomPost.getId();
      public String content = "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>";
      public Date updatedAt = new Date();
    };

    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    mockMvc.perform(
        put("/")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updatePostWithNotExactImagesCount() throws Exception {
    Post randomPost = testUtils.createPost(testUtils.createRandomAccount(),
        "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>");
    var jsonObject1 = new Object() {
      public Long id = randomPost.getId();
      public String content = "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00004\'/>";
      public Date updatedAt = new Date();
    };

    String jsonString1 = new ObjectMapper().writeValueAsString(jsonObject1);
    mockMvc.perform(
        put("/")
            .content(jsonString1)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    var jsonObject2 = new Object() {
      public Long id = randomPost.getId();
      public String content = "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/>";
      public Date updatedAt = new Date();
    };

    String jsonString2 = new ObjectMapper().writeValueAsString(jsonObject2);
    mockMvc.perform(
        put("/")
            .content(jsonString2)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

  }

  @Test
  public void updatePostAfterOneHour() throws Exception {
    Post randomPost = testUtils.createPost(testUtils.createRandomAccount(),
        "<div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00003\'/>");
    var jsonObject = new Object() {
      public Long id = randomPost.getId();
      public String content = "<img src=\'post/00003\'/><div>HEll</div><img src=\'post/00001\'/><img src=\'post/00002\'/><img src=\'post/00004\'/>";
      public Date updatedAt = DateUtils.addHours(new Date(), 1);
    };

    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    mockMvc.perform(
        put("/")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

  }

  @Test
  public void addLike() throws Exception {
    Post randomPost = testUtils.createRandomPost();
    Long randomAccountId = testUtils.createRandomAccount();
    var jsonObject = new Object() {
      public Long accountId = randomAccountId;
      public Long postAccountId = randomPost.getAccountId();
      public Long postId = randomPost.getId();
      public Boolean isLike = true;
    };
    String jsonString = new ObjectMapper().writeValueAsString(jsonObject);
    mockMvc.perform(
        post("/likes")
            .content(jsonString)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MvcResult mvcResult = mockMvc.perform(
        get("/" + randomPost.getId()))
        .andReturn();
    Post resPost = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
    });
    assertEquals(1, resPost.getLikesCount());
  }

  @Test
  public void removeLike() throws Exception {
    Post randomPost = testUtils.createRandomPost();
    Long randomAccountId = testUtils.createRandomAccount();
    var jsonObject1 = new Object() {
      public Long accountId = randomAccountId;
      public Long postAccountId = randomPost.getAccountId();
      public Long postId = randomPost.getId();
      public Boolean isLike = true;
    };
    var jsonObject2 = new Object() {
      public Long accountId = randomAccountId;
      public Long postAccountId = randomPost.getAccountId();
      public Long postId = randomPost.getId();
      public Boolean isLike = false;
    };

    String jsonString1 = new ObjectMapper().writeValueAsString(jsonObject1);
    String jsonString2 = new ObjectMapper().writeValueAsString(jsonObject2);
    mockMvc.perform(
        post("/likes")
            .content(jsonString1)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc.perform(
        post("/likes")
            .content(jsonString2)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MvcResult mvcResult = mockMvc.perform(
        get("/" + randomPost.getId()))
        .andReturn();
    Post resPost = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
    });
    assertEquals(0, resPost.getLikesCount());
  }

  @Test
  public void findByAccountId() throws Exception {
    var accountId = testUtils.createRandomAccount();
    List<Post> postList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      postList.add(testUtils.createPost(accountId));
      Thread.sleep(10);
    }
    postList = postList.reversed();
    MvcResult mvcResult = mockMvc.perform(
        get("/account/" + accountId)
            .header("preTime", postList.get(5).getCreatedDate().getTime()))
        .andExpect(status().isOk())
        .andReturn();
    List<Post> resList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(),
        new TypeReference<>() {
        });
    assertEquals(4, resList.size());
  }

  @Test
  public void findFeed() throws Exception {
    var accountId = testUtils.createRandomAccount();
    List<Post> postList = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      var followeeId = testUtils.createRandomAccount();
      postList.add(testUtils.createPost(followeeId));
      postList.add(testUtils.createPost(followeeId));
      testUtils.addFollower(accountId, followeeId);
    }
    postList.add(testUtils.createPost(accountId));
    postList.add(testUtils.createPost(accountId));
    var dummyAccountId = testUtils.createRandomAccount();
    testUtils.createPost(dummyAccountId);
    testUtils.createPost(dummyAccountId);
    MvcResult mvcResult = mockMvc.perform(
        get("/feed/" + accountId)
            .header("preTime", new Date().getTime()))
        .andExpect(status().isOk())
        .andReturn();
    List<Post> resList = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(),
        new TypeReference<>() {
        });
    assertEquals(postList.size(), resList.size());
  }
}
