package com.odinbook.postservice.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.odinbook.postservice.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
  @Query(value = "SELECT id, account_id, content, is_deleted, shared_from_posts, content_history, update_time_history, created_date FROM posts WHERE account_id = :accountId AND created_date < :preTime ORDER BY created_date DESC LIMIT 50", nativeQuery = true)
  public List<Post> findByAccountId(@Param("accountId") Long accountId, @Param("preTime") Timestamp preTime);

  @Query(value = "SELECT id, account_id, content, is_deleted, shared_from_posts, content_history, update_time_history, created_date FROM posts WHERE (account_id IN (SELECT followee_id FROM followers WHERE follower_id = :accountId) OR account_id = :accountId) AND created_date < :preTime ORDER BY created_date DESC LIMIT 50", nativeQuery = true)
  public List<Post> findFeed(@Param("accountId") Long accountId, @Param("preTime") Timestamp preTime);

}
