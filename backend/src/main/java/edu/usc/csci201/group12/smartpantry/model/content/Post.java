package edu.usc.csci201.group12.smartpantry.model.content;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class Post {
    private String id;
    private String authorId;
    private Instant createdAt;
    private Instant updatedAt;

    protected Post(String authorId) {
        this(UUID.randomUUID().toString(), authorId, Instant.now(), Instant.now());
    }

    protected Post(String id, String authorId, Instant createdAt, Instant updatedAt) {
        setId(id);
        setAuthorId(authorId);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt == null ? createdAt : updatedAt);
    }

    public String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = requireNonBlank(id, "id");
    }

    public String getAuthorId() {
        return authorId;
    }

    public final void setAuthorId(String authorId) {
        this.authorId = requireNonBlank(authorId, "authorId");
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public final void setCreatedAt(Instant createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public final void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    protected final String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
