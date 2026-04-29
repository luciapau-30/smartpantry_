// T3: DTO returned by PantryItemDao.getExpiringSoonWithName — joins PANTRY_ITEMS
// with INGREDIENTS so the background expiry job can build a human-readable alert
// without an extra round-trip per row.
package edu.usc.csci201.group12.smartpantry.dao;

import java.time.LocalDate;

public class ExpiringPantryItem {
    private String pantryItemId;
    private String userId;
    private String ingredientId;
    private String ingredientName;
    private double quantity;
    private String unit;
    private LocalDate expirationDate;

    public ExpiringPantryItem() {}

    public String getPantryItemId() { return pantryItemId; }
    public void setPantryItemId(String pantryItemId) { this.pantryItemId = pantryItemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIngredientId() { return ingredientId; }
    public void setIngredientId(String ingredientId) { this.ingredientId = ingredientId; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
