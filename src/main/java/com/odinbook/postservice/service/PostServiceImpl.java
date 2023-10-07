package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Post;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    @Override
    public Post createPost(Post post) throws IOException {
        return null;
    }

    @Override
    public List<Post> findAll() {
        return null;
    }

    @Override
    public Post findPostById(Long id) {
        return null;
    }

    @Override
    public List<Post> findPostsByAccountId(Long accountId) {
        return null;
    }

    @Override
    public void deletePostById(Long id) throws IOException {

    }

    @Override
    public Boolean addLike(Long accountId, Long postId) {
        return null;
    }

    @Override
    public Boolean removeLike(Long accountId, Long postId) {
        return null;
    }

    @Override
    public List<Post> searchPostsByContent(String accountId, String searchContent) throws IOException {
        return null;
    }
}
