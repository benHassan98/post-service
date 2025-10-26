package com.odinbook.postservice.repository;

import com.odinbook.postservice.model.Comment;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query(value = "SELECT id, account_id, post_id, content, created_date FROM comments WHERE post_id = :postId AND created_date < :preTime ORDER BY created_date DESC LIMIT 50", nativeQuery = true)
  public List<Comment> findByPostId(@Param("postId") Long postId, @Param("preTime") Timestamp preTime);
}
