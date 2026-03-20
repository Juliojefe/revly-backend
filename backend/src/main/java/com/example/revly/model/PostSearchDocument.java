package com.example.revly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "post_search_document")
public class PostSearchDocument {

    @Id
    @Column(name = "post_id")
    private Integer postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    @Column(name = "description_version", nullable = false)
    private Integer descriptionVersion = 1;

    // pgvector column – use List<Float> (common pattern with Hibernate + pgvector).
    // You will need a custom Type or Hibernate 6.2+ array mapping + pgvector JDBC driver
    // to persist/query this natively. The columnDefinition tells PostgreSQL the type.
    @Column(name = "description_embedding", columnDefinition = "vector(1536)")
    private List<Float> descriptionEmbedding;

    @Column(name = "embedding_status", nullable = false, length = 16)
    private String embeddingStatus = "pending";

    @Column(name = "embedding_model_key", nullable = false, length = 64)
    private String embeddingModelKey = "post_description_embedding_v1";

    @Column(name = "embedding_updated_at")
    private Instant embeddingUpdatedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private Instant updatedAt;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Integer getDescriptionVersion() {
        return descriptionVersion;
    }

    public void setDescriptionVersion(Integer descriptionVersion) {
        this.descriptionVersion = descriptionVersion;
    }

    public List<Float> getDescriptionEmbedding() {
        return descriptionEmbedding;
    }

    public void setDescriptionEmbedding(List<Float> descriptionEmbedding) {
        this.descriptionEmbedding = descriptionEmbedding;
    }

    public String getEmbeddingStatus() {
        return embeddingStatus;
    }

    public void setEmbeddingStatus(String embeddingStatus) {
        this.embeddingStatus = embeddingStatus;
    }

    public String getEmbeddingModelKey() {
        return embeddingModelKey;
    }

    public void setEmbeddingModelKey(String embeddingModelKey) {
        this.embeddingModelKey = embeddingModelKey;
    }

    public Instant getEmbeddingUpdatedAt() {
        return embeddingUpdatedAt;
    }

    public void setEmbeddingUpdatedAt(Instant embeddingUpdatedAt) {
        this.embeddingUpdatedAt = embeddingUpdatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
