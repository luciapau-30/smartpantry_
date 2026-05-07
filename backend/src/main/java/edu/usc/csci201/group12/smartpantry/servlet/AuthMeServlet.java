package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "authMe", urlPatterns = "/api/auth/me")
public final class AuthMeServlet extends AbstractJsonServlet {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);
        if (session == null) {
            writeOk(resp, Map.of("user", null));
            return;
        }
        Object raw = session.getAttribute(SessionAttributes.CURRENT_USER_ID);
        if (!(raw instanceof String userId) || userId.isBlank()) {
            writeOk(resp, Map.of("user", null));
            return;
        }

        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var user = store.findById(userId);
        if (user.isEmpty()) {
            session.removeAttribute(SessionAttributes.CURRENT_USER_ID);
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Session user not found"));
            return;
        }
        writeOk(resp, Map.of("user", user.get().toPublicView()));
    }
}
