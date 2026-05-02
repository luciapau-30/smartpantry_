package edu.usc.csci201.group12.smartpantry.model.content;

import java.time.Instant;

public class Comment extends Post {
    private String recipeId;
    private String parentCommentId;
    private String textBody;
    private boolean deleted;

    public Comment(String authorId, String recipeId, String textBody) {
        super(authorId);
        setRecipeId(recipeId);
        setParentCommentId(null);
        setTextBody(textBody);
        setDeleted(false);
    }

    public Comment(
            String id,
            String authorId,
            Instant createdAt,
            Instant updatedAt,
            String recipeId,
            String parentCommentId,
            String textBody,
            boolean deleted) {
        super(id, authorId, createdAt, updatedAt);
        setRecipeId(recipeId);
        setParentCommentId(parentCommentId);
        setTextBody(textBody);
        setDeleted(deleted);
    }

    public String getRecipeId() {
        return recipeId;
    }

    public final void setRecipeId(String recipeId) {
        this.recipeId = requireNonBlank(recipeId, "recipeId");
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public final void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId == null || parentCommentId.isBlank() ? null : parentCommentId.trim();
    }

    public String getTextBody() {
        return textBody;
    }

    public final void setTextBody(String textBody) {
        this.textBody = requireNonBlank(textBody, "textBody");
    }

    public boolean isDeleted() {
        return deleted;
    }

    public final void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
