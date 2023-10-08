package com.odinbook.postservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.repository.PostRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MessageChannel notificationRequest;
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           @Qualifier("notificationRequest") MessageChannel notificationRequest,
                           ElasticsearchClient elasticsearchClient) {
        this.postRepository = postRepository;
        this.notificationRequest = notificationRequest;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public Post createPost(Post post){

        Post savedPost = postRepository.saveAndFlush(post);

        try{
            elasticsearchClient.index(idx->idx
                    .index("posts-"+savedPost.getAccountId().toString())
                    .id(savedPost.getId().toString())
                    .document(savedPost)
            );
        }
        catch (IOException exception){
            exception.printStackTrace();
        }

        Message<Post> postMessage = MessageBuilder
                .withPayload(savedPost)
                .setHeader("notificationType","newPost")
                .build();

        notificationRequest.send(postMessage);
        return savedPost;
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findPostById(Long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public List<Post> findPostsByAccountId(Long accountId) {
        return postRepository.findPostsByAccountId(accountId);
    }

    @Override
    public void deletePostById(Long postId) throws IOException,NoSuchElementException{

        Post post = findPostById(postId)
                .orElseThrow();

        BooleanResponse indexExists = elasticsearchClient.indices().exists(e->e.index("posts-"+post.getAccountId().toString()));

        if(indexExists.value()){
            elasticsearchClient.delete(r->r
                    .index("posts-"+post.getAccountId().toString())
                    .id(post.getId().toString())
            );
        }

        postRepository.deleteById(postId);

    }

    @Override
    public void banByPostId(Long postId) {
        Post post = findPostById(postId)
                .orElseThrow();
        postRepository.updatePostByIdAndBanned(postId,true);

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","banPost")
                .build();

        notificationRequest.send(postMessage);
    }

    @Override
    public void addLike(Long accountId, Long postId) throws NoSuchElementException {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        postRepository.addLike(accountId,postId);

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","addLike")
                .build();

        notificationRequest.send(postMessage);

    }

    @Override
    public void removeLike(Long accountId, Long postId) throws NoSuchElementException {
        Post post = postRepository.findById(postId)
                .orElseThrow();

        postRepository.removeLike(accountId,postId);

        Message<Post> postMessage = MessageBuilder
                .withPayload(post)
                .setHeader("notificationType","removeLike")
                .build();

        notificationRequest.send(postMessage);

    }

    @Override
    public List<Post> searchPostsByContent(String accountId, String searchContent){
        try{
            return  elasticsearchClient.search(s->s
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
        catch (IOException exception){
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }
}
