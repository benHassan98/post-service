package com.odinbook.postservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.DTO.ImageDTO;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.service.CommentService;
import com.odinbook.postservice.validation.CommentForm;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@Valid @ModelAttribute CommentForm commentForm,
                                           BindingResult bindingResult,
                                           @RequestParam(value = "idList",required = false) String[] idList,
                                           @RequestParam(value = "fileList",required = false) MultipartFile[] fileList) throws JsonProcessingException {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Comment comment = commentForm.getComment();

        if(Objects.nonNull(idList)){
            List<ImageDTO> imageDTOList = new ArrayList<>();
            for(int i = 0; i< idList.length;i++){
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setId(idList[i]);
                imageDTO.setFile(fileList[i]);
                imageDTOList.add(imageDTO);
            }
            comment.setImageList(imageDTOList);
        }

        return  ResponseEntity.ok(commentService.createComment(comment));

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findCommentById(@PathVariable Long id){
        return commentService.findCommentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommentById(@PathVariable Long id){
        try{
            commentService.deleteCommentById(id);
            return ResponseEntity.ok().build();
        }
        catch (NoSuchElementException exception){
            exception.printStackTrace();
            return ResponseEntity.notFound().build();
        }

    }
    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity<?> jsonProcessingExceptionHandler(){
        return ResponseEntity.status(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).build();
    }

}
