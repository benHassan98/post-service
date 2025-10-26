package com.odinbook.postservice.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.dto.CreatePostDto;
import com.odinbook.postservice.dto.UpdatePostDto;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.record.DeleteRecord;
import com.odinbook.postservice.record.LikeRecord;
import com.odinbook.postservice.service.PostService;

import jakarta.validation.Valid;

@RestController
public class PostController {
  private final PostService postService;

  @Autowired
  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping("/")
  public ResponseEntity<?> create(@Valid @RequestPart("postDto") CreatePostDto postDto,
      @RequestPart("imageArr") MultipartFile[] imageArr,
      BindingResult bindingResult) throws JsonProcessingException {

    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }

    return ResponseEntity.ok(postService.create(postDto, imageArr));
  }

  @GetMapping("/")
  public List<Post> findAll() {
    return postService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) throws NoSuchElementException {
    return ResponseEntity.ok(postService.findById(id));
  }

  @GetMapping("/feed/{accountId}")
  public ResponseEntity<?> findFeed(@PathVariable Long accountId, @RequestHeader("preTime") String preTimeStr) {
    return ResponseEntity.ok(postService.findFeed(accountId, preTimeStr));
  }

  @GetMapping("/{id}/imagesId")
  public ResponseEntity<?> findImagesIds(@PathVariable Long id) {
    return ResponseEntity.ok(postService.findImagesIds(id));
  }

  @GetMapping("/account/{accountId}")
  public List<Post> findByAccountId(@PathVariable Long accountId, @RequestHeader("preTime") String preTimeStr) {
    return postService.findByAccountId(accountId, preTimeStr);
  }

  @PostMapping("/likes")
  public ResponseEntity<?> updateLikes(@RequestBody LikeRecord likeRecord) throws JsonProcessingException {
    if (likeRecord.isLike()) {
      postService.addLike(likeRecord.accountId(),
          likeRecord.postAccountId(),
          likeRecord.postId());
    } else {
      postService.removeLike(likeRecord.accountId(), likeRecord.postId());
    }
    return ResponseEntity.ok().build();
  }

  @PutMapping("/")
  public ResponseEntity<?> update(@Valid @RequestBody UpdatePostDto postDto,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }
    postService.update(postDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/")
  public void deleteById(@RequestBody DeleteRecord dRecord) {
    postService.deleteById(dRecord.id());
  }

  @ExceptionHandler(value = IllegalStateException.class)
  public ResponseEntity<?> illegalStateExceptionHandler(Exception exception) {
    return ResponseEntity.badRequest().body(exception.getMessage());
  }

  @ExceptionHandler(value = NoSuchElementException.class)
  public ResponseEntity<?> noSuchElementExceptionHandler() {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(value = JsonProcessingException.class)
  public ResponseEntity<?> jsonProcessingExceptionHandler() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

}
