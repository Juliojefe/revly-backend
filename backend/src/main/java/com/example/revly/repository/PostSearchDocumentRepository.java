package com.example.revly.repository;

import com.example.revly.model.PostSearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostSearchDocumentRepository extends JpaRepository<PostSearchDocument, Integer> {
}