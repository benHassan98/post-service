package com.odinbook.postservice.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.record.NewCommentRecord;
import com.odinbook.postservice.repository.CommentRepository;
import com.odinbook.service.ImageService;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CommentServiceImpl implements CommentService {

  private final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

  @Value("${minio.comment}")
  private String BUCKET_NAME;
  @PersistenceContext
  private EntityManager entityManager;
  private final CommentRepository commentRepository;
  private final StringRedisTemplate stringRedisTemplate;
  private final ImageService imageService;
  private final MinioClient minioClient;

  @Autowired
  public CommentServiceImpl(CommentRepository commentRepository,
      StringRedisTemplate stringRedisTemplate, ImageService imageService, MinioClient minioClient) {
    this.commentRepository = commentRepository;
    this.stringRedisTemplate = stringRedisTemplate;
    this.imageService = imageService;
    this.minioClient = minioClient;
  }

  @Override
  public Long create(Comment comment, MultipartFile[] imageArr) throws JsonProcessingException {
    if (imageArr.length > 4) {
      throw new IllegalStateException("Image count should not exceed 4");
    }
    for (var imageFile : imageArr) {
      if (imageFile.getSize() > MAX_IMAGE_SIZE) {
        throw new IllegalStateException("Maximum Image size is 5 MB");
      }
    }

    String newContent = this.imageService
        .assignImageIdsToContent("comment", comment.getContent(), imageArr, (id, file) -> {
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
    comment.setContent(newContent);
    var savedComment = commentRepository.saveAndFlush(comment);

    NewCommentRecord commentRecord = new NewCommentRecord(
        comment.getId(),
        comment.getAccountId(),
        comment.getPostId(),
        comment.getAccountId());
    String commentJson = new ObjectMapper().writeValueAsString(commentRecord);
    stringRedisTemplate.convertAndSend("newCommentChannel", commentJson);

    return savedComment.getId();
  }

  @Override
  public Comment findById(Long commentId) throws NoSuchElementException {
    return commentRepository.findById(commentId).orElseThrow();
  }

  @Override
  public List<Comment> findByPostId(Long postId, String preTimeStr) {
    return commentRepository.findByPostId(postId, new Timestamp(Long.valueOf(preTimeStr)));
  }

  @Override
  public void deleteById(Long commentId) {
    commentRepository.deleteById(commentId);
  }

}
