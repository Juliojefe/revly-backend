package com.example.revly.repository;
import com.example.revly.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findById(int postId);
    Page<Post> findByUserUserIdNotIn(Set<Integer> excludedUserIds, Pageable pageable);
}