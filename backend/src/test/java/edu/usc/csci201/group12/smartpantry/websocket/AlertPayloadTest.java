package edu.usc.csci201.group12.smartpantry.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AlertPayloadTest {

    @Test
    void expiringPayloadSerialisesAllFields() {
        AlertPayload p = AlertPayload.expiring()
                .pantryItemId("p1")
                .ingredientId("ing-1")
                .ingredientName("Tomato")
                .quantity(2.0)
                .unit("each")
                .expiresOn(LocalDate.of(2026, 5, 1))
                .daysLeft(3L)
                .build();

        String json = GsonProvider.get().toJson(p);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        assertEquals("EXPIRING_ITEM", obj.get("type").getAsString());
        assertEquals("p1", obj.get("pantryItemId").getAsString());
        assertEquals("ing-1", obj.get("ingredientId").getAsString());
        assertEquals("Tomato", obj.get("ingredientName").getAsString());
        assertEquals(2.0, obj.get("quantity").getAsDouble());
        assertEquals("each", obj.get("unit").getAsString());
        assertEquals("2026-05-01", obj.get("expiresOn").getAsString());
        assertEquals(3L, obj.get("daysLeft").getAsLong());
        assertNull(obj.get("action"), "action is for PANTRY_UPDATED only");
    }

    @Test
    void pantryUpdatedPayloadIsTaggedCorrectly() {
        AlertPayload p = AlertPayload.pantryUpdated("added")
                .pantryItemId("p42")
                .build();

        JsonObject obj = JsonParser.parseString(GsonProvider.get().toJson(p)).getAsJsonObject();

        assertEquals("PANTRY_UPDATED", obj.get("type").getAsString());
        assertEquals("added", obj.get("action").getAsString());
        assertEquals("p42", obj.get("pantryItemId").getAsString());
        assertNull(obj.get("ingredientName"));
        assertNull(obj.get("daysLeft"));
    }

    @Test
    void typeConstantsMatchSerialisedValues() {
        assertEquals("EXPIRING_ITEM", AlertPayload.TYPE_EXPIRING_ITEM);
        assertEquals("PANTRY_UPDATED", AlertPayload.TYPE_PANTRY_UPDATED);
    }
}
