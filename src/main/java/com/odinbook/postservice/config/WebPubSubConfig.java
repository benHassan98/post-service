package com.odinbook.postservice.config;


import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.postservice.record.LikeRecord;
import com.odinbook.postservice.service.PostService;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class WebPubSubConfig {
//    @Value("${spring.cloud.azure.pubsub.connection-string}")
//    private String webPubSubConnectStr;
//    @Autowired
//    private PostService postService;
//
//    @PostConstruct
//    public void init() throws URISyntaxException {
//        WebPubSubServiceClient service = new WebPubSubServiceClientBuilder()
//                .connectionString(webPubSubConnectStr)
//                .hub("posts")
//                .buildClient();
//
//        WebPubSubClientAccessToken token = service.getClientAccessToken(
//                new GetClientAccessTokenOptions()
//                        .setUserId("0")
//        );
//        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
//
//            @Override
//            public void onMessage(String jsonString) {
//                try {
//                    LikeRecord likeRecord = new ObjectMapper().readValue(jsonString, LikeRecord.class);
//                    if(likeRecord.isLike()){
//                        postService.addLike(likeRecord.accountId(), likeRecord.postId());
//                    }
//                    else{
//                        postService.removeLike(likeRecord.accountId(), likeRecord.postId());
//                    }
//
//                    service.sendToGroup(
//                            likeRecord.postId()+".like",
//                            jsonString,
//                            WebPubSubContentType.APPLICATION_JSON);
//
//                } catch (JsonProcessingException exception) {
//                    exception.printStackTrace();
//                }
//
//
//            }
//
//            @Override
//            public void onOpen(ServerHandshake serverHandshake) {
//
//            }
//
//            @Override
//            public void onClose(int i, String s, boolean b) {
//
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }
//        };
//        webSocketClient.connect();
//
//    }


}
