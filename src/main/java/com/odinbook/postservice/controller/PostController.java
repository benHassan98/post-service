package com.odinbook.postservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.util.JSONUtils;
import com.odinbook.postservice.DTO.ImageDTO;
import com.odinbook.postservice.DTO.TestOb;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import com.odinbook.postservice.validation.PostForm;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
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
                                        BindingResult bindingResult,
                                        @RequestParam(value = "idList",required = false) String[] idList,
                                        @RequestParam(value = "fileList",required = false) MultipartFile[] fileList) throws JsonProcessingException {

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Post post = postForm.getPost();

        if(Objects.nonNull(idList)){
            List<ImageDTO> imageDTOList = new ArrayList<>();
            for(int i = 0; i< idList.length;i++){
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setId(idList[i]);
                imageDTO.setFile(fileList[i]);
                imageDTOList.add(imageDTO);
            }

            post.setImageList(imageDTOList);
        }


        return  ResponseEntity.ok(postService.createPost(post));

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
                                        BindingResult bindingResult,
                                        @RequestParam(value = "idList",required = false) String[] idList,
                                        @RequestParam(value = "fileList",required = false) MultipartFile[] fileList) throws JsonProcessingException {

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Post post = postForm.getPost();

        if(Objects.nonNull(idList)){
            List<ImageDTO> imageDTOList = new ArrayList<>();
            for(int i = 0; i< idList.length;i++){
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setId(idList[i]);
                imageDTO.setFile(fileList[i]);
                imageDTOList.add(imageDTO);
            }

            post.setImageList(imageDTOList);
        }


        return postService.updatePost(post)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
    @PostMapping(value = "/test/test")
    public void ts(
            @RequestParam("idList") String[] idList,
            @RequestParam("imageList") MultipartFile[] imageList,
            @ModelAttribute TestOb testOb) throws ParseException, JsonProcessingException {

//        TestOb testOb1 =  new ObjectMapper().readValue(testOb, TestOb.class);

        Arrays.stream(idList).toList().forEach(System.out::println);
        Arrays.stream(imageList).toList().forEach(System.out::println);

        System.out.println(testOb.id);
        System.out.println(testOb.file.getOriginalFilename());
//        System.out.println(testOb.id);
//        System.out.println(testOb.file.getOriginalFilename());

//        System.out.println(testOb.file.getOriginalFilename());
//        System.out.println(files[0].getOriginalFilename());
//        System.out.println(imageDTO.getId());
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
