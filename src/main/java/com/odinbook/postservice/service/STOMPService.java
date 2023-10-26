package com.odinbook.postservice.service;

import com.odinbook.postservice.model.Comment;
import com.odinbook.postservice.record.LikeRecord;

public interface STOMPService {

    public void sendNewCommentToAccounts(Comment comment);
    public void sendNewLikeToAccounts(LikeRecord likeRecord);
    public void sendRemovedCommentToAccounts(Comment comment);
    public void sendRemovedLikeToAccounts(LikeRecord likeRecord);

}
