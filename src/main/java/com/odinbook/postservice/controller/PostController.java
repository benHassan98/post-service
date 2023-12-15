package com.odinbook.postservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import com.odinbook.postservice.validation.PostForm;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class PostController {
    private final PostService postService;
    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@Valid @ModelAttribute PostForm postForm,
                                        BindingResult bindingResult) throws JsonProcessingException {

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        return  ResponseEntity.ok(postService.createPost(postForm.getPost()));

    }

    @GetMapping("/all")
    public List<Post> findAll(){
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findPostById(@PathVariable Long id){

        return postService.findPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public List<Post> findPostsByAccountId(@PathVariable Long accountId){
        return postService.findPostsByAccountId(accountId);
    }
    @GetMapping("/profile/{accountId}")
    public List<Post> findPublicPostsByAccountId(@PathVariable Long accountId){
        return postService.findPublicPostsByAccountId(accountId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updatePost(@Valid @ModelAttribute PostForm postForm,
                                        BindingResult bindingResult) throws JsonProcessingException {

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        return postService.updatePost(postForm.getPost())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{postId}")
    public void deleteById(@PathVariable Long postId) throws IOException,NoSuchElementException {
        postService.deletePostById(postId);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> noSuchElementExceptionHandler(){
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<?> ioExceptionHandler(){
        return ResponseEntity.status(HttpResponseStatus.BAD_GATEWAY.code()).build();
    }
    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity<?> jsonProcessingExceptionHandler(){
        return ResponseEntity.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).build();
    }


}
