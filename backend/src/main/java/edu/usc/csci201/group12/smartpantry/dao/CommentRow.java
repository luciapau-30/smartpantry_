// PORTED (zeqiang/database): DB row type for the COMMENTS table.
// Renamed from Zeqiang's Comment to avoid collision with model.content.Comment (Archit's).
package edu.usc.csci201.group12.smartpantry.dao;

import java.sql.Timestamp;

public class CommentRow {
    private String id;
    private String recipeId;
    private String userId;
    private String parentCommentId;
    private String body;
    private Timestamp createdAt;
    private boolean deleted;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
