package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Post;

import java.io.IOException;
import java.util.List;

public interface PostService {
    public Post createPost(Post post) throws IOException;
    public List<Post> findAll();
    public Post findPostById(Long id);
    public List<Post> findPostsByAccountId(Long accountId);
    public void deletePostById(Long id) throws IOException;
    public Boolean addLike(Long accountId,Long postId);
    public Boolean removeLike(Long accountId,Long postId);
    public List<Post> searchPostsByContent(String accountId,String searchContent) throws IOException;


}
