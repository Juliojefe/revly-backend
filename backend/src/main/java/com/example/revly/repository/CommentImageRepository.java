package com.example.revly.repository;

import com.example.revly.model.Comment;
import com.example.revly.model.CommentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentImageRepository extends JpaRepository<CommentImage, Integer> {
    List<CommentImage> findByComment(Comment comment);
}