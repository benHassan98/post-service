package com.odinbook.postservice.controller;

import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import com.odinbook.postservice.validation.PostForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
public class PostController {
    private final PostService postService;
    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostForm postForm,
                                        BindingResult bindingResult){

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
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/account/{accountId}")
    public List<Post> findPostsByAccountId(@PathVariable Long accountId){
        return postService.findPostsByAccountId(accountId);
    }

    @DeleteMapping("/{deletingAccountId}/")
    public void deleteById(@PathVariable Long id){
        try{
            postService.deletePostById(id);
        }
        catch (IOException ioException){
            throw new RuntimeException(ioException);
        }
    }



}
