package edu.usc.csci201.group12.smartpantry.websocket;

import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// WebSocket endpoint for server-pushed alerts (expiry notifications).
// Clients connect after login; the background thread calls sendToUser() to push messages.
@ServerEndpoint(value = "/ws/alerts", configurator = HttpSessionConfigurator.class)
public class AlertWebSocketEndpoint {

    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        HttpSession httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());
        if (httpSession == null) {
            closeQuietly(session);
            return;
        }
        Object raw = httpSession.getAttribute(SessionAttributes.CURRENT_USER_ID);
        if (!(raw instanceof String userId) || userId.isBlank()) {
            closeQuietly(session);
            return;
        }
        session.getUserProperties().put("userId", userId);
        sessions.put(userId, session);
    }

    @OnClose
    public void onClose(Session session) {
        removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        removeSession(session);
    }

    private void removeSession(Session session) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId != null) sessions.remove(userId, session);
    }

    private void closeQuietly(Session session) {
        try { session.close(); } catch (IOException ignored) {}
    }

    // Called by ExpiryCheckerThread to push an alert to a specific user.
    public static void sendToUser(String userId, String jsonMessage) {
        Session s = sessions.get(userId);
        if (s != null && s.isOpen()) {
            try {
                s.getBasicRemote().sendText(jsonMessage);
            } catch (IOException e) {
                sessions.remove(userId, s);
            }
        }
    }

    public static Set<String> getConnectedUserIds() {
        return sessions.keySet();
    }
}
