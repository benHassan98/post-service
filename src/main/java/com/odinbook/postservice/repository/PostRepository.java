package com.odinbook.postservice.repository;

import com.odinbook.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    @Query(value = """
            SELECT *

            FROM posts

            WHERE
            account_id = :accountId

            OR

            (
              is_followers_visible = 1 AND EXISTS ( select * from followers where follower_id = :accountId AND followee_id = account_id )
            )

            OR

            (
            EXISTS (SELECT * FROM friends WHERE (adding_id = :accountId AND added_id = account_id) OR (added_id = :accountId AND adding_id = account_id)   )
            AND
            (
            (friends_visibility_type = 1 AND EXISTS (select * from posts_friends_visibility WHERE friend_id = :accountId AND id = post_id) )
            OR
            (friends_visibility_type = 0 AND NOT EXISTS (select * from posts_friends_visibility WHERE friend_id = :accountId AND id = post_id) )
            )
            )""",nativeQuery = true)
    public List<Post> findPostsByAccountId(@Param("accountId") Long accountId);
    @Query(value = "INSERT INTO likes(account_id,post_id) VALUES (:accountId,:postId)",nativeQuery = true)
    public void addLike(@Param("accountId") Long accountId, @Param("postId") Long postId);

    @Query(value = "DELETE FROM likes WHERE account_id=:accountId AND post_id=:postId",nativeQuery = true)
    public void removeLike(@Param("accountId") Long accountId, @Param("postId") Long postId);

    @Query(value = "INSERT INTO followers_visibility VALUES(:postId,:isVisible)",nativeQuery = true)
    public void addFollowersVisibility(@Param("postId") Long postId,@Param("isVisible") Boolean isVisible);
    @Query(value = "INSERT INTO posts_visibility_type VALUES (:postId,:visibilityType)",nativeQuery = true)
    public void addPostVisibilityType(@Param("postId") Long postId,@Param("visibilityType") Boolean visibilityType);

}
