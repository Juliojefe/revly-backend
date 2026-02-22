package com.example.revly.repository;
import com.example.revly.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // Retrieves a Post by its primary key (postId).
    // Returns Optional.empty() if no Post is found.
    Optional<Post> findById(int postId);

    // Returns a paginated list of Posts where the Post's userId
    // is NOT in the provided excludedUserIds set.
    Page<Post> findByUserUserIdNotIn(Set<Integer> excludedUserIds, Pageable pageable);

    // Returns a paginated list of Post IDs for the "Explore" feed.
    // Includes posts where:
    // - The post has no associated user, OR
    // - The post's user is not the current user AND
    //   the post's user is NOT someone the current user is following.
    // Results are ordered by newest first (createdAt DESC, postId DESC).
    @Query("SELECT p.postId FROM Post p WHERE p.user IS NULL OR (p.user.userId <> :userId AND p.user NOT IN (SELECT fol FROM User u JOIN u.following fol WHERE u.userId = :userId)) ORDER BY p.createdAt DESC, p.postId DESC")
    Page<Integer> findExplorePostIds(@Param("userId") Integer userId, Pageable pageable);

    // Fetches full Post entities for the given list of postIds.
    // Uses LEFT JOIN FETCH to eagerly load associated user and images
    // to prevent N+1 query problems.
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.postId IN :postIds")
    List<Post> findByPostIdInWithDetails(@Param("postIds") List<Integer> postIds);

    // Returns a paginated list of all Post IDs,
    // ordered by newest first (createdAt DESC, postId DESC).
    @Query("SELECT p.postId FROM Post p ORDER BY p.createdAt DESC, p.postId DESC")
    Page<Integer> findAllPostIds(Pageable pageable);

    // Returns like counts for each post in the given postIds list.
    // Each result is an Object[]:
    //   [0] = postId
    //   [1] = number of likes (COUNT of likers)
    // Uses LEFT JOIN so posts with zero likes are included.
    @Query("SELECT p.postId, COUNT(l) FROM Post p LEFT JOIN p.likers l WHERE p.postId IN :postIds GROUP BY p.postId")
    List<Object[]> findLikeCountsByPostIds(@Param("postIds") List<Integer> postIds);

    // Returns the IDs of posts (from the provided postIds list)
    // that the specified user has liked.
    @Query("SELECT p.postId FROM Post p JOIN p.likers l WHERE l.userId = :userId AND p.postId IN :postIds")
    List<Integer> findLikedPostIdsForUser(@Param("postIds") List<Integer> postIds, @Param("userId") Integer userId);

    // Returns the IDs of posts (from the provided postIds list)
    // that the specified user has saved.
    @Query("SELECT p.postId FROM Post p JOIN p.savers s WHERE s.userId = :userId AND p.postId IN :postIds")
    List<Integer> findSavedPostIdsForUser(@Param("postIds") List<Integer> postIds, @Param("userId") Integer userId);
}