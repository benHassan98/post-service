package com.odinbook.postservice.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.model.Comment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WebPubSubServiceImpl implements WebPubSubService{

//    @Value("${spring.cloud.azure.pubsub.connection-string}")
//    private String webPubSubConnectStr;
    @Override
    public void sendNewCommentToUsers(Comment comment) throws JsonProcessingException {
        String jsonString = new ObjectMapper().writeValueAsString(comment);

//        new WebPubSubServiceClientBuilder()
//                .connectionString(webPubSubConnectStr)
//                .hub("posts")
//                .buildClient()
//                .sendToGroup(
//                        comment.getPost().getId()+".newComment",
//                        jsonString,
//                        WebPubSubContentType.APPLICATION_JSON);

    }

    @Override
    public void sendRemovedCommentIdToUsers(Comment comment){

//        new WebPubSubServiceClientBuilder()
//                .connectionString(webPubSubConnectStr)
//                .hub("posts")
//                .buildClient()
//                .sendToGroup(
//                        comment.getPost().getId()+".removeComment",
//                        comment.getId().toString(),
//                        WebPubSubContentType.TEXT_PLAIN);


    }
}
