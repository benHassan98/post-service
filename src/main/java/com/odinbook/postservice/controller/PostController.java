package com.odinbook.postservice.controller;

import com.odinbook.postservice.model.Post;
import com.odinbook.postservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {
    private final PostService postService;
    private final MessageChannel notificationRequest;

    @Autowired
    public PostController(PostService postService,
                          @Qualifier("notificationRequest") MessageChannel notificationRequest) {
        this.postService = postService;
        this.notificationRequest = notificationRequest;
    }
    @GetMapping("/try")
    public String ty(){
        Message<String> postMessage = MessageBuilder
                .withPayload("It WORKS !!!!!!!")
                .setHeader("notificationType","postNotification")
                .build();

        notificationRequest.send(postMessage);

        return "Hello";

    }
}
