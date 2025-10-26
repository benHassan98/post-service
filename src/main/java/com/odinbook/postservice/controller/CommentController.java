package com.odinbook.postservice.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.dto.CreateCommentDto;
import com.odinbook.postservice.record.DeleteRecord;
import com.odinbook.postservice.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/comment")
public class CommentController {

  private final CommentService commentService;

  @Autowired
  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/")
  public ResponseEntity<?> create(@Valid @RequestPart("commentDto") CreateCommentDto commentDto,
      @RequestPart("imageArr") MultipartFile[] imageArr,
      BindingResult bindingResult) throws JsonProcessingException {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }

    return ResponseEntity.ok(commentService.create(commentDto.getComment(), imageArr));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) throws NoSuchElementException {
    return ResponseEntity.ok(commentService.findById(id));
  }

  @GetMapping("/post/{id}")
  public ResponseEntity<?> findByPostId(@PathVariable Long id, @RequestHeader("preTime") String preTimeStr) {
    return ResponseEntity.ok(commentService.findByPostId(id, preTimeStr));
  }

  @DeleteMapping("/")
  public void deleteById(@RequestBody DeleteRecord dRecord) {
    commentService.deleteById(dRecord.id());
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
