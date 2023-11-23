package com.odinbook.postservice.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.odinbook.postservice.DTO.ImageDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = "content")
    private String content;

    @Transient
    private List<ImageDTO> imageList;

    @ManyToOne
    @JoinColumn(name = "shared_from_post_id")
    private Post sharedFromPost;

    @ElementCollection
    @CollectionTable(name = "likes" ,joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "account_id")
    private final List<Long> likesList = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "post")
    private final List<Comment> commentsList = new ArrayList<>();

    @Column(name = "is_edited")
    private Boolean isEdited = false;
    @Column(name = "is_followers_visible")
    private Boolean isVisibleToFollowers;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "friends_visibility_type")
    private Boolean friendsVisibilityType;

    @ElementCollection
    @CollectionTable(name = "posts_friends_visibility" ,joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "friend_id")
    private List<Long> visibleToFriendList = new ArrayList<>();

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdDate;

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getSharedFromPost() {
        return sharedFromPost;
    }

    public void setSharedFromPost(Post sharedFromPost) {
        this.sharedFromPost = sharedFromPost;
    }

    public List<Long> getLikesList() {
        return likesList;
    }

    public List<Comment> getCommentsList() {
        return commentsList;
    }

    public Boolean getVisibleToFollowers() {
        return isVisibleToFollowers;
    }

    public void setVisibleToFollowers(Boolean visibleToFollowers) {
        isVisibleToFollowers = visibleToFollowers;
    }

    public Boolean getFriendsVisibilityType() {
        return friendsVisibilityType;
    }

    public void setFriendsVisibilityType(Boolean friendsVisibilityType) {
        this.friendsVisibilityType = friendsVisibilityType;
    }

    public List<Long> getVisibleToFriendList() {
        return visibleToFriendList;
    }

    public void setVisibleToFriendList(List<Long> visibleToFriendList) {
        this.visibleToFriendList = visibleToFriendList;
    }

    public List<ImageDTO> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageDTO> imageList) {
        this.imageList = imageList;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
