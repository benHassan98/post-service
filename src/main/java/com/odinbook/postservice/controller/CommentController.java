package com.odinbook.postservice.controller;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.service.CommentService;
import com.odinbook.postservice.validation.CommentForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
                                           BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return  ResponseEntity.ok(commentService.createComment(commentForm.getComment()));

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


}
