package com.odinbook.postservice.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.odinbook.postservice.model.Post;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchService {
    public void insertPost(Post post) throws IOException, ElasticsearchException;
    public void updatePost(Post newPost) throws IOException, ElasticsearchException;
    public void deletePost(Post post) throws IOException, ElasticsearchException;
    public List<Post> searchPostsByContent(String accountId, String searchContent) throws IOException, ElasticsearchException;
}
