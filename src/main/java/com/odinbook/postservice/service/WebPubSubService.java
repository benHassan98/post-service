package com.odinbook.postservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.postservice.model.Comment;


public interface WebPubSubService {
    public void sendNewCommentToUsers(Comment comment) throws JsonProcessingException;
    public void sendRemovedCommentIdToUsers(Comment comment);
}
