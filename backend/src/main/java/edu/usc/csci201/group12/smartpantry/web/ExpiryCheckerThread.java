package edu.usc.csci201.group12.smartpantry.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemRow;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.websocket.AlertWebSocketEndpoint;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Background thread (T3): checks PANTRY_ITEMS for expiring items every hour.
// Sends a WebSocket alert to each connected user whose items expire within 3 days.
public class ExpiryCheckerThread {

    private static final int CHECK_INTERVAL_MINUTES = 60;
    private static final int EXPIRY_WINDOW_DAYS = 3;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "expiry-checker");
                t.setDaemon(true);
                return t;
            });

    private final PantryItemDao pantryItemDao = new PantryItemDao();

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::checkAndNotify,
                0,
                CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void checkAndNotify() {
        try {
            List<PantryItemRow> expiring = pantryItemDao.getAllExpiringSoon(EXPIRY_WINDOW_DAYS);

            // Group items by userId
            Map<String, List<PantryItemRow>> byUser = new HashMap<>();
            for (PantryItemRow item : expiring) {
                byUser.computeIfAbsent(item.getUserId(), k -> new ArrayList<>()).add(item);
            }

            // Push to connected users only
            for (String userId : AlertWebSocketEndpoint.getConnectedUserIds()) {
                List<PantryItemRow> items = byUser.get(userId);
                if (items != null && !items.isEmpty()) {
                    AlertWebSocketEndpoint.sendToUser(userId, buildAlertJson(items));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildAlertJson(List<PantryItemRow> items) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "expiry_alert");
        JsonArray arr = new JsonArray();
        for (PantryItemRow item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", item.getId());
            obj.addProperty("ingredientId", item.getIngredientId());
            if (item.getExpirationDate() != null) {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpirationDate());
                obj.addProperty("daysLeft", daysLeft);
                obj.addProperty("expirationDate", item.getExpirationDate().toString());
            }
            arr.add(obj);
        }
        root.add("items", arr);
        return GsonProvider.get().toJson(root);
    }
}
