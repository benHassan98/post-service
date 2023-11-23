package com.odinbook.postservice.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.DTO.ImageDTO;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.datatype.jsr310.*;
public class CommentForm {
    private Long accountId;
    @PostNotNull
    private String postJson;
    @NotEmpty
    private String content;
    public Comment getComment() throws JsonProcessingException {
        System.out.println(this.accountId);
        System.out.println(this.postJson);
        System.out.println(this.content);


        Post post = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(this.postJson, Post.class);


        Comment comment = new Comment();
        comment.setAccountId(this.accountId);
        comment.setPost(post);
        comment.setContent(this.content);

        return comment;

    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPostJson() {
        return postJson;
    }

    public void setPostJson(String postJson) throws JsonProcessingException {

        this.postJson = postJson;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
