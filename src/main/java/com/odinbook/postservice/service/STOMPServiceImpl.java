package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.record.LikeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class STOMPServiceImpl implements STOMPService{

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public STOMPServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void sendNewCommentToAccounts(Comment comment) {
        simpMessagingTemplate.convertAndSend(
                "/topic/post."+comment.getPost().getId()+".add",
                comment
        );

    }

    @Override
    public void sendNewLikeToAccounts(LikeRecord likeRecord) {
        simpMessagingTemplate.convertAndSend(
                "/topic/post."+likeRecord.postId()+".add",
                likeRecord
        );
    }

    @Override
    public void sendRemovedCommentToAccounts(Comment comment) {
        simpMessagingTemplate.convertAndSend(
                "/topic/post."+comment.getPost().getId()+".remove",
                comment
        );
    }

    @Override
    public void sendRemovedLikeToAccounts(LikeRecord likeRecord) {
        simpMessagingTemplate.convertAndSend(
                "/topic/post."+likeRecord.postId()+".remove",
                likeRecord
        );
    }
}
