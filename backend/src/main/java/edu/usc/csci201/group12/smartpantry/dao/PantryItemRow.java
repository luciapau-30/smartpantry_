// PORTED (zeqiang/database): DB row type for the PANTRY_ITEMS table.
// Renamed from Zeqiang's PantryItem to avoid collision with recommendation.PantryItem (Lucia's).
package edu.usc.csci201.group12.smartpantry.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PantryItemRow {
    private String id;
    private String userId;
    private String ingredientId;
    private double quantity;
    private String unit;
    private LocalDate expirationDate;
    private LocalDateTime addedAt;

    public PantryItemRow() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIngredientId() { return ingredientId; }
    public void setIngredientId(String ingredientId) { this.ingredientId = ingredientId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
