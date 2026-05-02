// T3 / F14: Periodic job that pushes "your X expires in N days" alerts to every
// user who currently has an open WebSocket. We scope the query to connected users
// only — there's no point doing a JDBC round-trip for someone who isn't listening.
//
// To avoid spamming the same alert every tick, we keep an in-memory record of
// (userId, pantryItemId) pairs we've already alerted on; a row is forgotten once
// it falls out of the expiry window or is removed from the pantry.
//
// The "currently connected" lookup is injected as a Supplier so the job can be
// unit-tested without booting a WebSocket container.
package edu.usc.csci201.group12.smartpantry.background;

import edu.usc.csci201.group12.smartpantry.dao.ExpiringPantryItem;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class ExpiryCheckJob implements Runnable {

    private static final Logger LOG = Logger.getLogger(ExpiryCheckJob.class.getName());
    private static final int DEFAULT_WINDOW_DAYS = 3;

    private final PantryItemDao pantryItemDao;
    private final PantryEventBroadcaster broadcaster;
    private final Supplier<Set<String>> connectedUsersSupplier;
    private final int windowDays;
    private final boolean dbConfigured;

    private final Map<String, Set<String>> alreadyAlertedByUser = new ConcurrentHashMap<>();

    public ExpiryCheckJob(PantryItemDao pantryItemDao,
                          PantryEventBroadcaster broadcaster,
                          Supplier<Set<String>> connectedUsersSupplier,
                          int windowDays,
                          boolean dbConfigured) {
        this.pantryItemDao = pantryItemDao;
        this.broadcaster = broadcaster;
        this.connectedUsersSupplier = connectedUsersSupplier;
        this.windowDays = windowDays;
        this.dbConfigured = dbConfigured;
    }

    public ExpiryCheckJob(PantryItemDao pantryItemDao,
                          PantryEventBroadcaster broadcaster,
                          Supplier<Set<String>> connectedUsersSupplier,
                          boolean dbConfigured) {
        this(pantryItemDao, broadcaster, connectedUsersSupplier, DEFAULT_WINDOW_DAYS, dbConfigured);
    }

    @Override
    public void run() {
        if (!dbConfigured) {
            LOG.fine("ExpiryCheckJob skipped — no PANTRY_DB_URL configured (dev mode).");
            return;
        }

        Set<String> connected = connectedUsersSupplier.get();
        if (connected == null || connected.isEmpty()) {
            return;
        }

        int alertsSent = 0;
        for (String userId : connected) {
            List<ExpiringPantryItem> expiring;
            try {
                expiring = pantryItemDao.getExpiringSoonWithName(userId, windowDays);
            } catch (Exception ex) {
                LOG.warning("ExpiryCheckJob: lookup failed for user=" + userId
                        + " (" + ex.getMessage() + ")");
                continue;
            }

            Set<String> previouslyAlerted = alreadyAlertedByUser
                    .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
            Set<String> currentIds = new HashSet<>();

            for (ExpiringPantryItem item : expiring) {
                currentIds.add(item.getPantryItemId());
                if (previouslyAlerted.add(item.getPantryItemId())) {
                    broadcaster.expiringItem(item);
                    alertsSent++;
                }
            }
            previouslyAlerted.retainAll(currentIds);
        }

        alreadyAlertedByUser.keySet().retainAll(connected);

        if (alertsSent > 0) {
            final int n = alertsSent;
            LOG.info(() -> "ExpiryCheckJob: pushed " + n + " expiry alert(s)");
        }
    }
}
