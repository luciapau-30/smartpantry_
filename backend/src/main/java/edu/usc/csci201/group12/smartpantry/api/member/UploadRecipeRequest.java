package edu.usc.csci201.group12.smartpantry.api.member;

import java.math.BigDecimal;
import java.util.List;

/** Gson-friendly DTO for {@code POST /api/member/recipes/upload}. */
public final class UploadRecipeRequest {
    public String title;
    public String cuisineType;
    public String description;
    public String imageUrl;
    public Integer prepTimeMin;
    public Integer cookTimeMin;
    public Integer servings;
    public Boolean isPublic;
    public List<IngredientLine> ingredients;
    public List<String> instructions;

    public static final class IngredientLine {
        public String ingredientId;
        public BigDecimal quantity;
        public String unit;
        public String notes;
        public Boolean optional;
    }
}
