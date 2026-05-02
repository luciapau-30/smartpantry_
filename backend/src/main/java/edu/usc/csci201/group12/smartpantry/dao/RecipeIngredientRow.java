// PORTED (zeqiang/database): DB row type for the RECIPE_INGREDIENTS table.
// Renamed from Zeqiang's RecipeIngredient to avoid collision with model.content.Recipe.RecipeIngredient (Archit's).
package edu.usc.csci201.group12.smartpantry.dao;

public class RecipeIngredientRow {
    private String id;
    private String recipeId;
    private String ingredientId;
    private double quantity;
    private String unit;
    private String notes;
    private boolean optional;
    private String ingredientName;

    public RecipeIngredientRow() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getIngredientId() { return ingredientId; }
    public void setIngredientId(String ingredientId) { this.ingredientId = ingredientId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
}
