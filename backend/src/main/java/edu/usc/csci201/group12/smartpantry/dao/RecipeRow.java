// PORTED (zeqiang/database): DB row type for the RECIPES table.
// Renamed from Zeqiang's Recipe to avoid collision with model.content.Recipe (Archit's domain object).
package edu.usc.csci201.group12.smartpantry.dao;

import java.time.LocalDateTime;

public class RecipeRow {
    private String id;
    private String authorId;
    private String title;
    private String description;
    private String imageUrl;
    private String difficulty;
    private int prepTimeMin;
    private int cookTimeMin;
    private int servings;
    private boolean isPublic;
    private String categoryTags;
    private LocalDateTime createdAt;

    public RecipeRow() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getPrepTimeMin() { return prepTimeMin; }
    public void setPrepTimeMin(int prepTimeMin) { this.prepTimeMin = prepTimeMin; }

    public int getCookTimeMin() { return cookTimeMin; }
    public void setCookTimeMin(int cookTimeMin) { this.cookTimeMin = cookTimeMin; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getCategoryTags() { return categoryTags; }
    public void setCategoryTags(String categoryTags) { this.categoryTags = categoryTags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
