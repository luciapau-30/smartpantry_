// T4/F14: JSON DTO pushed to clients over the pantry WebSocket.
//
// Two event types are used today:
//   - "EXPIRING_ITEM"  — a pantry item is within the expiry window (T3 background job)
//   - "PANTRY_UPDATED" — a pantry item was added or removed (live sync from servlets)
//
// Frontend is expected to read `type` first, then the relevant fields.
package edu.usc.csci201.group12.smartpantry.websocket;

import java.time.LocalDate;

public final class AlertPayload {

    public static final String TYPE_EXPIRING_ITEM  = "EXPIRING_ITEM";
    public static final String TYPE_PANTRY_UPDATED = "PANTRY_UPDATED";

    private final String type;
    private final String pantryItemId;
    private final String ingredientId;
    private final String ingredientName;
    private final Double quantity;
    private final String unit;
    private final String expiresOn;
    private final Long daysLeft;
    private final String action;

    private AlertPayload(Builder b) {
        this.type = b.type;
        this.pantryItemId = b.pantryItemId;
        this.ingredientId = b.ingredientId;
        this.ingredientName = b.ingredientName;
        this.quantity = b.quantity;
        this.unit = b.unit;
        this.expiresOn = b.expiresOn == null ? null : b.expiresOn.toString();
        this.daysLeft = b.daysLeft;
        this.action = b.action;
    }

    public String getType() { return type; }
    public String getPantryItemId() { return pantryItemId; }
    public String getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public Double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiresOn() { return expiresOn; }
    public Long getDaysLeft() { return daysLeft; }
    public String getAction() { return action; }

    public static Builder expiring() {
        return new Builder().type(TYPE_EXPIRING_ITEM);
    }

    public static Builder pantryUpdated(String action) {
        return new Builder().type(TYPE_PANTRY_UPDATED).action(action);
    }

    public static final class Builder {
        private String type;
        private String pantryItemId;
        private String ingredientId;
        private String ingredientName;
        private Double quantity;
        private String unit;
        private LocalDate expiresOn;
        private Long daysLeft;
        private String action;

        public Builder type(String type) { this.type = type; return this; }
        public Builder pantryItemId(String id) { this.pantryItemId = id; return this; }
        public Builder ingredientId(String id) { this.ingredientId = id; return this; }
        public Builder ingredientName(String n) { this.ingredientName = n; return this; }
        public Builder quantity(Double q) { this.quantity = q; return this; }
        public Builder unit(String u) { this.unit = u; return this; }
        public Builder expiresOn(LocalDate d) { this.expiresOn = d; return this; }
        public Builder daysLeft(Long d) { this.daysLeft = d; return this; }
        public Builder action(String a) { this.action = a; return this; }

        public AlertPayload build() { return new AlertPayload(this); }
    }
}
