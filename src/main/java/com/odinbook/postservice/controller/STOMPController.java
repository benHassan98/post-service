package com.odinbook.postservice.controller;

import com.odinbook.postservice.record.LikeRecord;
import com.odinbook.postservice.service.PostService;
import com.odinbook.postservice.service.STOMPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.TreeMap;

@Controller
public class STOMPController {

    private final PostService postService;
    private final STOMPService stompService;

    @Autowired
    public STOMPController(PostService postService, STOMPService stompService) {
        this.postService = postService;
        this.stompService = stompService;
    }

    @MessageMapping("/like")
    public void postLike(@Payload LikeRecord likeRecord){

        if(likeRecord.isLike()){
            postService.addLike(likeRecord.accountId(), likeRecord.postId());
            stompService.sendNewLikeToAccounts(likeRecord);
        }
        else{
            postService.removeLike(likeRecord.accountId(), likeRecord.postId());
            stompService.sendRemovedLikeToAccounts(likeRecord);
        }



    }


}
