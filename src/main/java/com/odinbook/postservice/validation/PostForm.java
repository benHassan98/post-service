package com.odinbook.postservice.validation;

import com.odinbook.postservice.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CreatedOrSharedPost
public class PostForm {

    private Long accountId;
    private String content;
    private Post sharedFromPost;
    private Boolean isVisibleToFollowers;
    private Boolean friendsVisibilityType;
    private List<Long> visibleToFriendList = new ArrayList<>();
    private MultipartFile[] imageList = new MultipartFile[0];


    public Post getPost(){
        Post post = new Post();
        post.setAccountId(this.accountId);
        post.setContent(this.content);
        post.setSharedFromPost(this.sharedFromPost);
        post.setFriendsVisibilityType(this.friendsVisibilityType);
        post.setVisibleToFollowers(this.isVisibleToFollowers);
        post.setVisibleToFriendList(this.visibleToFriendList);
        post.setImageList(this.imageList);
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
    public void setSharedFromPost(Post sharedFromPost) {
        this.sharedFromPost = sharedFromPost;
    }

    public Post getSharedFromPost(){
        return this.sharedFromPost;
    }

    public MultipartFile[] getImageList() {
        return imageList;
    }

    public void setImageList(MultipartFile[] imageList) {
        this.imageList = imageList;
    }


    public Boolean getFriendsVisibilityType() {
        return friendsVisibilityType;
    }

    public void setFriendsVisibilityType(Boolean friendsVisibilityType) {
        this.friendsVisibilityType = friendsVisibilityType;
    }

    public Boolean getVisibleToFollowers() {
        return isVisibleToFollowers;
    }

    public void setVisibleToFollowers(Boolean visibleToFollowers) {
        isVisibleToFollowers = visibleToFollowers;
    }

    public List<Long> getVisibleToFriendList() {
        return visibleToFriendList;
    }

    public void setVisibleToFriendList(List<Long> visibleToFriendList) {
        this.visibleToFriendList = visibleToFriendList;
    }
}
