package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.api.auth.LoginRequest;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.security.PasswordHasher;
import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "authLogin", urlPatterns = "/api/auth/login")
public final class AuthLoginServlet extends AbstractJsonServlet {

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody) throws IOException {
        LoginRequest in = GsonProvider.get().fromJson(jsonBody, LoginRequest.class);
        if (in == null || in.login() == null || in.password() == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("login and password required"));
            return;
        }

        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        PasswordHasher hasher = (PasswordHasher) req.getServletContext().getAttribute(ContextKeys.PASSWORD_HASHER);

        var userOpt = store.findByLoginIgnoreCase(in.login());
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Invalid credentials"));
            return;
        }
        User user = userOpt.get();
        if (!user.verifyPassword(in.password(), hasher)) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Invalid credentials"));
            return;
        }

        req.getSession(true).setAttribute(SessionAttributes.CURRENT_USER_ID, user.getId());
        writeOk(resp, Map.of("user", user.toPublicView()));
    }
}
