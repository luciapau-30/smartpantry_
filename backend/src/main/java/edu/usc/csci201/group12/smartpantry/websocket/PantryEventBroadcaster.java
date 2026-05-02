// T4: Thin facade in front of PantryWebSocket so call sites (servlets, background
// jobs) don't depend on the WebSocket API directly. Stored as a context attribute
// at startup; servlets fetch it from ServletContext via ContextKeys.EVENT_BROADCASTER
// and call broadcast methods.
package edu.usc.csci201.group12.smartpantry.websocket;

import edu.usc.csci201.group12.smartpantry.dao.ExpiringPantryItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PantryEventBroadcaster {

    /** Live-sync push: pantry was just modified by an authenticated request. */
    public void pantryUpdated(String userId, String action, String pantryItemId) {
        if (userId == null || userId.isBlank()) return;
        AlertPayload payload = AlertPayload.pantryUpdated(action)
                .pantryItemId(pantryItemId)
                .build();
        PantryWebSocket.pushToUser(userId, payload);
    }

    /** Expiry alert push: one payload per expiring item. */
    public void expiringItem(ExpiringPantryItem item) {
        if (item == null || item.getUserId() == null) return;
        long daysLeft = item.getExpirationDate() == null
                ? 0L
                : ChronoUnit.DAYS.between(LocalDate.now(), item.getExpirationDate());
        AlertPayload payload = AlertPayload.expiring()
                .pantryItemId(item.getPantryItemId())
                .ingredientId(item.getIngredientId())
                .ingredientName(item.getIngredientName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expiresOn(item.getExpirationDate())
                .daysLeft(daysLeft)
                .build();
        PantryWebSocket.pushToUser(item.getUserId(), payload);
    }
}
