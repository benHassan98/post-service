package com.odinbook.postservice.repository;

import com.odinbook.postservice.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    @Query(value = "select * from comments where account_id = ?",nativeQuery = true)
    public List<Comment> findCommentsByAccountId(Long accountId);
    @Query(value = "select * from comments where post_id = ?",nativeQuery = true)
    public List<Comment> findCommentsByPostId(Long postId);
    public void deleteCommentsByPostId(Long postId);
}
