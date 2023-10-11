package com.odinbook.postservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.odinbook.postservice.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService{
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticSearchServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public void insertPost(Post post) throws IOException, ElasticsearchException {
        elasticsearchClient.index(idx->idx
                .index("posts-"+post.getAccountId().toString())
                .id(post.getId().toString())
                .document(post)
        );
    }

    @Override
    public void updatePost(Post newPost) throws IOException, ElasticsearchException {
        elasticsearchClient.update(u->u
                        .index("posts-"+ newPost.getAccountId().toString())
                        .id(newPost.getId().toString())
                        .doc(newPost)
                , Post.class);
    }

    @Override
    public void deletePost(Post post) throws IOException, ElasticsearchException {
        BooleanResponse indexExists = elasticsearchClient
                .indices()
                .exists(e->e.index("posts-"+post.getAccountId().toString()));

        if(indexExists.value()){
            elasticsearchClient.delete(r->r
                    .index("posts-"+post.getAccountId().toString())
                    .id(post.getId().toString())
            );
        }
    }

    @Override
    public List<Post> searchPostsByContent(String accountId, String searchContent) throws IOException, ElasticsearchException {
        return elasticsearchClient.search(s->s
                        .index("posts-"+accountId)
                        .query(q->q
                                .match(v->v
                                        .field("content")
                                        .query(searchContent)
                                        .fuzziness("AUTO"))
                        )
                ,
                Post.class
        ).hits().hits().stream().map(Hit::source).toList();
    }
}
