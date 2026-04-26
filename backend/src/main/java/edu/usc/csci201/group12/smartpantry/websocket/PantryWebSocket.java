// T4: Jakarta WebSocket endpoint for live pantry updates.
//
// URL:    ws://<host>/smartpantry/ws/pantry
// Auth:   relies on the existing HttpSession (cookie JSESSIONID); the configurator
//         lifts the userId attribute set by AuthLoginServlet into the EndpointConfig
//         so onOpen can register the socket against the right user.
//
// The endpoint maintains a static Map<userId, Set<Session>> so multiple tabs/devices
// per user all receive pushed events. Pushes happen via {@link PantryEventBroadcaster},
// not directly — keeps the call sites (servlets, background jobs) decoupled from the
// WebSocket framework.
package edu.usc.csci201.group12.smartpantry.websocket;

import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(
        value = "/ws/pantry",
        configurator = PantryWebSocket.HttpSessionConfigurator.class
)
public final class PantryWebSocket {

    private static final Logger LOG = Logger.getLogger(PantryWebSocket.class.getName());
    private static final String USER_ID_PROP = SessionAttributes.CURRENT_USER_ID;

    private static final Map<String, Set<Session>> SESSIONS_BY_USER = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String userId = (String) config.getUserProperties().get(USER_ID_PROP);
        if (userId == null || userId.isBlank()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                        "Login required"));
            } catch (IOException ignored) {
            }
            return;
        }
        session.getUserProperties().put(USER_ID_PROP, userId);
        SESSIONS_BY_USER
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        LOG.fine(() -> "WS opened for user=" + userId + " sessionId=" + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        String userId = (String) session.getUserProperties().get(USER_ID_PROP);
        if (userId == null) return;
        Set<Session> sessions = SESSIONS_BY_USER.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                SESSIONS_BY_USER.remove(userId);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.log(Level.WARNING, "WS error on sessionId=" + session.getId(), error);
    }

    /**
     * Send an alert payload to every open socket for the given user. Returns the
     * number of sockets the payload was successfully delivered to. Closed/dead
     * sockets are pruned as a side effect.
     */
    public static int pushToUser(String userId, AlertPayload payload) {
        Set<Session> sessions = SESSIONS_BY_USER.get(userId);
        if (sessions == null || sessions.isEmpty()) return 0;
        String json = GsonProvider.get().toJson(payload);
        int delivered = 0;
        for (Session s : sessions) {
            if (!s.isOpen()) {
                sessions.remove(s);
                continue;
            }
            try {
                s.getBasicRemote().sendText(json);
                delivered++;
            } catch (IOException ex) {
                LOG.log(Level.FINE, "WS send failed; pruning sessionId=" + s.getId(), ex);
                sessions.remove(s);
            }
        }
        if (sessions.isEmpty()) {
            SESSIONS_BY_USER.remove(userId);
        }
        return delivered;
    }

    /** Visible for ExpiryCheckJob — only push to users who actually have a socket open. */
    public static Set<String> connectedUserIds() {
        return Set.copyOf(SESSIONS_BY_USER.keySet());
    }

    /**
     * Configurator that copies the userId from the HttpSession into the
     * per-connection {@link ServerEndpointConfig#getUserProperties()} so that
     * {@link #onOpen(Session, EndpointConfig)} can identify the caller.
     */
    public static final class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec,
                                    HandshakeRequest request,
                                    jakarta.websocket.HandshakeResponse response) {
            Object http = request.getHttpSession();
            if (http instanceof HttpSession httpSession) {
                Object userId = httpSession.getAttribute(SessionAttributes.CURRENT_USER_ID);
                if (userId instanceof String s && !s.isBlank()) {
                    sec.getUserProperties().put(USER_ID_PROP, s);
                }
            }
        }
    }
}
