package com.odinbook.postservice.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.odinbook.postservice.DTO.ImageDTO;
import com.odinbook.postservice.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CreatedOrSharedPost
public class PostForm {

    private Long id;
    private Long accountId;
    private String content;
    private String sharedFromPostJson;
    private Boolean visibleToFollowers;
    private Boolean friendsVisibilityType;
    private Boolean edited = false;
    private Boolean deleted = false;
    private List<Long> visibleToFriendList = new ArrayList<>();



    public Post getPost() throws JsonProcessingException {

        Post sharedFromPost = Objects.nonNull(sharedFromPostJson)?
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .readValue(this.sharedFromPostJson, Post.class) : null;

        Post post = new Post();
        post.setId(this.id);
        post.setAccountId(this.accountId);
        post.setContent(this.content);
        post.setSharedFromPost(sharedFromPost);
        post.setFriendsVisibilityType(this.friendsVisibilityType);
        post.setVisibleToFollowers(this.visibleToFollowers);
        post.setEdited(this.edited);
        post.setDeleted(this.deleted);
        post.setVisibleToFriendList(this.visibleToFriendList);

        return post;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent(){
        return this.content;
    }
    public void setSharedFromPostJson(String sharedFromPostJson){
        this.sharedFromPostJson = sharedFromPostJson;
    }

    public String getSharedFromPostJson(){
        return this.sharedFromPostJson;
    }

    public Boolean getFriendsVisibilityType() {
        return friendsVisibilityType;
    }

    public void setFriendsVisibilityType(Boolean friendsVisibilityType) {
        this.friendsVisibilityType = friendsVisibilityType;
    }

    public Boolean getVisibleToFollowers() {
        return visibleToFollowers;
    }

    public void setVisibleToFollowers(Boolean visibleToFollowers) {
       this.visibleToFollowers = visibleToFollowers;
    }

    public List<Long> getVisibleToFriendList() {
        return visibleToFriendList;
    }

    public void setVisibleToFriendList(List<Long> visibleToFriendList) {
        this.visibleToFriendList = visibleToFriendList;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
