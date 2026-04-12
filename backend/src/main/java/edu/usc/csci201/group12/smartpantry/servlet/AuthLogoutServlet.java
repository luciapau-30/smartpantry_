package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "authLogout", urlPatterns = "/api/auth/logout")
public final class AuthLogoutServlet extends AbstractJsonServlet {

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody) throws IOException {
        var session = req.getSession(false);
        if (session != null) {
            session.removeAttribute(SessionAttributes.CURRENT_USER_ID);
            session.invalidate();
        }
        writeOk(resp);
    }
}
