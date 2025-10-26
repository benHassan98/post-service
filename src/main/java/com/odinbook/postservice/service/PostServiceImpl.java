package com.odinbook.postservice.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.dto.CreatePostDto;
import com.odinbook.postservice.dto.UpdatePostDto;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.NewLikeRecord;
import com.odinbook.postservice.record.NewPostRecord;
import com.odinbook.postservice.repository.PostRepository;
import com.odinbook.service.ImageService;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class PostServiceImpl implements PostService {
  private final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
  @Value("${minio.post}")
  private String BUCKET_NAME;
  @PersistenceContext
  private EntityManager entityManager;
  private final PostRepository postRepository;
  private final StringRedisTemplate stringRedisTemplate;
  private final ImageService imageService;
  private final MinioClient minioClient;

  @Autowired
  public PostServiceImpl(PostRepository postRepository,
      StringRedisTemplate stringRedisTemplate, ImageService imageService, MinioClient minioClient) {
    this.postRepository = postRepository;
    this.stringRedisTemplate = stringRedisTemplate;
    this.imageService = imageService;
    this.minioClient = minioClient;

  }

  @Override
  public Long create(CreatePostDto postDto, MultipartFile[] imageArr) throws JsonProcessingException {
    if (imageArr.length > 4) {
      throw new IllegalStateException("Image count should not exceed 4");
    }
    for (var imageFile : imageArr) {
      if (imageFile.getSize() > MAX_IMAGE_SIZE) {
        throw new IllegalStateException("Maximum Image size is 5 MB");
      }
    }

    Post newPost = postDto.getPost();

    String newContent = this.imageService
        .assignImageIdsToContent("post", newPost.getContent(), imageArr, (id, file) -> {
          try {
            this.minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(id)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
          } catch (Exception exception) {
            exception.printStackTrace();
          }
        });
    newPost.setContent(newContent);
    var savedPost = postRepository.saveAndFlush(newPost);

    NewPostRecord postRecord = new NewPostRecord(savedPost.getId(), savedPost.getAccountId());
    String postJson = new ObjectMapper().writeValueAsString(postRecord);
    stringRedisTemplate.convertAndSend("newPostChannel", postJson);

    return savedPost.getId();
  }

  @Override
  public List<Post> findAll() {
    return postRepository.findAll()
        .stream()
        .peek(this::populateLikesAndCommentsCount)
        .peek(post -> findFirstImages(post, 1))
        .collect(Collectors.toList());
  }

  @Override
  public Post findById(Long id) throws NoSuchElementException {
    return postRepository.findById(id)
        .map(this::populateLikesAndCommentsCount)
        .orElseThrow();
  }

  @Override
  public List<Post> findByAccountId(Long accountId, String preTimeStr) {
    return postRepository
        .findByAccountId(accountId, new Timestamp(Long.valueOf(preTimeStr)))
        .stream()
        .peek(this::populateLikesAndCommentsCount)
        .peek(post -> findFirstImages(post, 1))
        .collect(Collectors.toList());
  }

  @Override
  public List<Post> findFeed(Long accountId, String preTimeStr) {
    return postRepository
        .findFeed(accountId, new Timestamp(Long.valueOf(preTimeStr)))
        .stream()
        .peek(this::populateLikesAndCommentsCount)
        .peek(post -> findFirstImages(post, 1))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> findImagesIds(Long id) {
    String content = postRepository.findById(id).orElseThrow().getContent();
    return Jsoup
        .parse(content)
        .body()
        .getElementsByTag("img")
        .stream()
        .map(element -> element.attr("src").split("/")[1]).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void update(UpdatePostDto postDto) {

    var hashPutResult = this.stringRedisTemplate
        .opsForHash()
        .putIfAbsent("postUpdateCnt:" + postDto.getId().toString(), postDto.getId().toString(), Long.toString(5));
    if (hashPutResult) {
      this.stringRedisTemplate
          .opsForHash()
          .expire("postUpdateCnt:" + postDto.getId().toString(),
              Duration.ofMinutes(65),
              List.of(postDto.getId().toString()));
    }
    var hashGetResult = (String) this.stringRedisTemplate
        .opsForHash()
        .get("postUpdateCnt:" + postDto.getId().toString(),
            postDto.getId().toString());
    if (Long.valueOf(hashGetResult) == 0) {
      throw new IllegalStateException("Maximum update limit is reached");
    }

    Post newPost = postDto.getPost();
    String newContent = newPost.getContent();

    Object[] result = (Object[]) entityManager
        .createNativeQuery("SELECT content, created_date FROM posts WHERE id = :postId")
        .setParameter("postId", newPost.getId())
        .getSingleResult();

    String content = (String) result[0];
    Timestamp createdAt = (Timestamp) result[1];
    Date updatedAt = postDto.getUpdatedAt();
    if (content.equals(newContent)) {
      throw new IllegalStateException("Same content");
    }

    var oldImageCnt = Jsoup.parse((String) content).body().getElementsByTag("img").size();
    var newImageCnt = Jsoup.parse(newContent).body().getElementsByTag("img").size();
    if (oldImageCnt != newImageCnt) {
      throw new IllegalStateException("Images should not be deleted or added");
    }

    if (updatedAt.getTime() - createdAt.getTime() > 60 * 60 * 1000) {
      throw new IllegalStateException("Cannot update post after 1 hour");
    }
    this.stringRedisTemplate
        .opsForHash()
        .put("postUpdateCnt:" + postDto.getId().toString(), postDto.getId().toString(),
            Long.toString(Long.valueOf(hashGetResult) - 1));

    entityManager
        .createNativeQuery(
            "UPDATE posts SET content = :newContent, content_history = array_append(content_history, :oldContent), update_time_history = array_append(update_time_history, :newUpdateTime)  WHERE id = :postId")
        .setParameter("oldContent", content)
        .setParameter("newContent", newContent)
        .setParameter("newUpdateTime", updatedAt)
        .setParameter("postId", newPost.getId())
        .executeUpdate();
  }

  @Override
  @Transactional
  public void deleteById(Long id) {

    entityManager
        .createNativeQuery("UPDATE posts SET is_deleted = true, content= 'This post is deleted' WHERE id = :postId")
        .setParameter("postId", id)
        .executeUpdate();
  }

  @Override
  @Transactional
  public void addLike(Long accountId, Long postAccountId, Long postId) throws JsonProcessingException {
    entityManager
        .createNativeQuery("INSERT INTO likes VALUES (:accountId,:postId)")
        .setParameter("accountId", accountId)
        .setParameter("postId", postId)
        .executeUpdate();

    NewLikeRecord newLikeRecord = new NewLikeRecord(postId, postAccountId, accountId);

    String likeJson = new ObjectMapper().writeValueAsString(newLikeRecord);

    stringRedisTemplate.convertAndSend("newLikeChannel", likeJson);

  }

  @Override
  @Transactional
  public void removeLike(Long accountId, Long postId) {

    entityManager
        .createNativeQuery("DELETE FROM likes WHERE account_id = :accountId AND post_id = :postId")
        .setParameter("accountId", accountId)
        .setParameter("postId", postId)
        .executeUpdate();

  }

  private Post populateLikesAndCommentsCount(Post post) {
    Long likesResult = ((Long) entityManager
        .createNativeQuery("SELECT COUNT(*) FROM likes WHERE post_id = :postId")
        .setParameter("postId", post.getId())
        .getSingleResult());

    Long commentsResult = ((Long) entityManager
        .createNativeQuery("SELECT COUNT(*) FROM comments WHERE post_id = :postId")
        .setParameter("postId", post.getId())
        .getSingleResult());

    post.setLikesCount(likesResult);
    post.setCommentsCount(commentsResult);
    return post;
  }

  private Post findFirstImages(Post post, int prfxSize) {

    var imgList = Jsoup
        .parse(post.getContent())
        .body()
        .getElementsByTag("img");
    for (int i = 0; i < prfxSize && i < imgList.size(); i++) {
      String src = imgList.get(i).attr("src");
      var bytes = this.imageService.getImage(BUCKET_NAME, src);
      post.addImageByte(bytes);
    }

    return post;
  }

}
