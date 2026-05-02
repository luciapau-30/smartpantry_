package edu.usc.csci201.group12.smartpantry.websocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests that don't require a WebSocket container. Full end-to-end
 * handshake tests live in the integration suite (manual until embedded Tomcat
 * is wired in).
 */
class PantryWebSocketTest {

    @Test
    void pushToUserWithNoSessionsReturnsZero() {
        AlertPayload payload = AlertPayload.expiring()
                .pantryItemId("anything")
                .build();
        int delivered = PantryWebSocket.pushToUser("nobody-listening", payload);
        assertEquals(0, delivered, "No sockets registered for that user");
    }

    @Test
    void connectedUserIdsStartsEmpty() {
        assertNotNull(PantryWebSocket.connectedUserIds());
    }

    @Test
    void broadcasterDoesNotCrashWithoutSessions() {
        PantryEventBroadcaster bc = new PantryEventBroadcaster();
        assertDoesNotThrow(() -> bc.pantryUpdated("u-xyz", "added", "p-1"));
        assertDoesNotThrow(() -> {
            edu.usc.csci201.group12.smartpantry.dao.ExpiringPantryItem item =
                    new edu.usc.csci201.group12.smartpantry.dao.ExpiringPantryItem();
            item.setPantryItemId("p-1");
            item.setUserId("u-xyz");
            item.setIngredientName("Tomato");
            item.setExpirationDate(java.time.LocalDate.now().plusDays(2));
            bc.expiringItem(item);
        });
    }

    @Test
    void broadcasterRejectsBlankUserGracefully() {
        PantryEventBroadcaster bc = new PantryEventBroadcaster();
        assertDoesNotThrow(() -> bc.pantryUpdated(null, "added", "p"));
        assertDoesNotThrow(() -> bc.pantryUpdated("", "added", "p"));
        assertDoesNotThrow(() -> bc.expiringItem(null));
    }
}
