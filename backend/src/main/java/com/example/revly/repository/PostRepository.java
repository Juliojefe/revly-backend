package com.example.revly.repository;

import com.example.revly.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Integer> {

    Optional<Post> findById(int postId);

    Page<Post> findByUserUserIdNotIn(Set<Integer> excludedUserIds, Pageable pageable);

    @Query("SELECT p.postId FROM Post p WHERE p.user IS NULL OR (p.user.userId <> :userId AND p.user NOT IN (SELECT fol FROM User u JOIN u.following fol WHERE u.userId = :userId)) ORDER BY p.createdAt DESC, p.postId DESC")
    Page<Integer> findExplorePostIds(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.postId IN :postIds")
    List<Post> findByPostIdInWithDetails(@Param("postIds") List<Integer> postIds);

    @Query("SELECT p.postId FROM Post p ORDER BY p.createdAt DESC, p.postId DESC")
    Page<Integer> findAllPostIds(Pageable pageable);

    @Query("SELECT p.postId, COUNT(l) FROM Post p LEFT JOIN p.likers l WHERE p.postId IN :postIds GROUP BY p.postId")
    List<Object[]> findLikeCountsByPostIds(@Param("postIds") List<Integer> postIds);

    @Query("SELECT p.postId FROM Post p JOIN p.likers l WHERE l.userId = :userId AND p.postId IN :postIds")
    List<Integer> findLikedPostIdsForUser(@Param("postIds") List<Integer> postIds, @Param("userId") Integer userId);

    @Query("SELECT p.postId FROM Post p JOIN p.savers s WHERE s.userId = :userId AND p.postId IN :postIds")
    List<Integer> findSavedPostIdsForUser(@Param("postIds") List<Integer> postIds, @Param("userId") Integer userId);

    //  semantic search cosine distance
    @Query(value = """
        SELECT p.post_id 
        FROM post p 
        WHERE p.description_embedding IS NOT NULL
        ORDER BY p.description_embedding <=> CAST(:embedding AS vector)
        """,
            countQuery = "SELECT COUNT(*) FROM post p WHERE p.description_embedding IS NOT NULL",
            nativeQuery = true)
    Page<Integer> findPostIdsBySemanticSimilarity(@Param("embedding") float[] embedding, Pageable pageable);

    // Tag search (pure JPQL)
    @Query("SELECT p.postId FROM Post p JOIN p.tags t WHERE LOWER(t.tagName) = LOWER(:tagName) " +
            "ORDER BY p.createdAt DESC, p.postId DESC")
    Page<Integer> findPostIdsByTag(@Param("tagName") String tagName, Pageable pageable);

    // multi-tag search
    @Query("""
    SELECT p.postId 
    FROM Post p 
    WHERE EXISTS (
        SELECT 1 FROM p.tags t 
        WHERE t.tagName IN :tagNames
    )
    ORDER BY p.createdAt DESC, p.postId DESC
    """)
    Page<Integer> findPostIdsByAnyTag(@Param("tagNames") Set<String> tagNames, Pageable pageable);

    // Hybrid search – semantic similarity + any of the provided tags
    @Query(value = """
    SELECT p.post_id 
    FROM post p 
    WHERE EXISTS (
        SELECT 1 FROM post_tag pt 
        JOIN tag t ON pt.tag_id = t.tag_id 
        WHERE pt.post_id = p.post_id 
          AND t.tag_name IN (:tagNames)
    )
      AND p.description_embedding IS NOT NULL
    ORDER BY p.description_embedding <=> CAST(:embedding AS vector)
    """,
            countQuery = """
    SELECT COUNT(*) FROM post p 
    WHERE EXISTS (
        SELECT 1 FROM post_tag pt 
        JOIN tag t ON pt.tag_id = t.tag_id 
        WHERE pt.post_id = p.post_id 
          AND t.tag_name IN (:tagNames)
    )
      AND p.description_embedding IS NOT NULL
    """,
            nativeQuery = true)
    Page<Integer> findPostIdsByHybridAnyTags(@Param("embedding") float[] embedding,
                                             @Param("tagNames") Set<String> tagNames,
                                             Pageable pageable);
}