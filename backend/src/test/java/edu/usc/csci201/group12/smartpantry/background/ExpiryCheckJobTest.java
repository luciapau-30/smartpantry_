package edu.usc.csci201.group12.smartpantry.background;

import edu.usc.csci201.group12.smartpantry.dao.ExpiringPantryItem;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExpiryCheckJobTest {

    /** Records every {@link PantryEventBroadcaster#expiringItem(ExpiringPantryItem)} call. */
    private static final class RecordingBroadcaster extends PantryEventBroadcaster {
        final List<ExpiringPantryItem> expiringCalls = new ArrayList<>();
        final List<String> pantryUpdates = new ArrayList<>();

        @Override
        public void expiringItem(ExpiringPantryItem item) {
            expiringCalls.add(item);
        }

        @Override
        public void pantryUpdated(String userId, String action, String pantryItemId) {
            pantryUpdates.add(userId + ":" + action + ":" + pantryItemId);
        }
    }

    /** Returns whatever the test pre-loaded for each user. */
    private static class FakeDao extends PantryItemDao {
        final Map<String, List<ExpiringPantryItem>> byUser = new HashMap<>();

        void put(String userId, ExpiringPantryItem... items) {
            byUser.put(userId, new ArrayList<>(List.of(items)));
        }

        @Override
        public List<ExpiringPantryItem> getExpiringSoonWithName(String userId, int withinDays) {
            return byUser.getOrDefault(userId, List.of());
        }
    }

    private static ExpiringPantryItem item(String pantryItemId,
                                           String userId,
                                           String name,
                                           int daysUntilExpiry) {
        ExpiringPantryItem e = new ExpiringPantryItem();
        e.setPantryItemId(pantryItemId);
        e.setUserId(userId);
        e.setIngredientId("ing-" + pantryItemId);
        e.setIngredientName(name);
        e.setQuantity(1.0);
        e.setUnit("each");
        e.setExpirationDate(LocalDate.now().plusDays(daysUntilExpiry));
        return e;
    }

    @Test
    void shortCircuitsWhenDbNotConfigured() {
        FakeDao dao = new FakeDao();
        dao.put("u1", item("p1", "u1", "Tomato", 1));
        RecordingBroadcaster bc = new RecordingBroadcaster();

        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, () -> Set.of("u1"), 3, false);
        job.run();

        assertTrue(bc.expiringCalls.isEmpty(),
                "No alerts should fire when dbConfigured is false (dev mode)");
    }

    @Test
    void noOpWhenNoUsersConnected() {
        FakeDao dao = new FakeDao();
        dao.put("u1", item("p1", "u1", "Tomato", 1));
        RecordingBroadcaster bc = new RecordingBroadcaster();

        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, Set::of, 3, true);
        job.run();

        assertTrue(bc.expiringCalls.isEmpty(),
                "No alerts should fire when no users have an open WebSocket");
    }

    @Test
    void alertsFireOnceThenDedupe() {
        FakeDao dao = new FakeDao();
        ExpiringPantryItem tomato = item("p1", "u1", "Tomato", 1);
        ExpiringPantryItem milk   = item("p2", "u1", "Milk",   2);
        dao.put("u1", tomato, milk);

        RecordingBroadcaster bc = new RecordingBroadcaster();
        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, () -> Set.of("u1"), 3, true);

        job.run();
        assertEquals(2, bc.expiringCalls.size(), "First tick should alert on both items");

        job.run();
        assertEquals(2, bc.expiringCalls.size(),
                "Second tick must not re-alert on the same items");
    }

    @Test
    void itemReentersWindowAfterDropOut() {
        FakeDao dao = new FakeDao();
        ExpiringPantryItem tomato = item("p1", "u1", "Tomato", 1);
        dao.put("u1", tomato);

        RecordingBroadcaster bc = new RecordingBroadcaster();
        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, () -> Set.of("u1"), 3, true);

        job.run();
        assertEquals(1, bc.expiringCalls.size());

        // User used the tomato up — DAO no longer returns it.
        dao.byUser.put("u1", new ArrayList<>());
        job.run();
        assertEquals(1, bc.expiringCalls.size(), "No new alert when there's nothing to alert on");

        // User puts a fresh tomato in with the same id later (simulated).
        dao.put("u1", tomato);
        job.run();
        assertEquals(2, bc.expiringCalls.size(),
                "Once an item drops out and comes back, it should alert again");
    }

    @Test
    void multipleUsersAreAlertedIndependently() {
        FakeDao dao = new FakeDao();
        dao.put("u1", item("p1", "u1", "Tomato", 1));
        dao.put("u2", item("p2", "u2", "Yogurt", 2));

        RecordingBroadcaster bc = new RecordingBroadcaster();
        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, () -> Set.of("u1", "u2"), 3, true);

        job.run();

        assertEquals(2, bc.expiringCalls.size());
        assertTrue(bc.expiringCalls.stream().anyMatch(i -> i.getUserId().equals("u1")));
        assertTrue(bc.expiringCalls.stream().anyMatch(i -> i.getUserId().equals("u2")));
    }

    @Test
    void daoExceptionDoesNotFailEntireRun() {
        FakeDao dao = new FakeDao() {
            @Override
            public List<ExpiringPantryItem> getExpiringSoonWithName(String userId, int withinDays) {
                if ("u1".equals(userId)) throw new RuntimeException("DB down");
                return super.getExpiringSoonWithName(userId, withinDays);
            }
        };
        dao.put("u2", item("p2", "u2", "Yogurt", 2));

        RecordingBroadcaster bc = new RecordingBroadcaster();
        ExpiryCheckJob job = new ExpiryCheckJob(dao, bc, () -> Set.of("u1", "u2"), 3, true);

        assertDoesNotThrow(job::run);
        assertEquals(1, bc.expiringCalls.size(), "u2 should still be alerted");
        assertEquals("u2", bc.expiringCalls.get(0).getUserId());
    }
}
