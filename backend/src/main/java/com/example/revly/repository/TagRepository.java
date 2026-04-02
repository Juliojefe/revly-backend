package com.example.revly.repository;

import com.example.revly.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByTagName(String tagName);

    // similar spelling search for tag suggestions (LIKE %query%)
    @Query("SELECT t.tagName FROM Tag t " +
            "WHERE LOWER(t.tagName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY t.tagName ASC")
    Page<String> findSimilarTagNames(@Param("query") String query, Pageable pageable);
}