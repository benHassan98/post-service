package com.odinbook.postservice.validation;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

public class CommentForm {
    private Long accountId;
    @PostNotNull
    private Post post;
    @NotEmpty
    private String content;
    private MultipartFile[] imageList = new MultipartFile[0];
    public Comment getComment(){
        Comment comment = new Comment();
        comment.setAccountId(this.accountId);
        comment.setPost(this.post);
        comment.setContent(this.content);
        comment.setImageList(this.imageList);

        return comment;

    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MultipartFile[] getImageList() {
        return imageList;
    }

    public void setImageList(MultipartFile[] imageList) {
        this.imageList = imageList;
    }
}
