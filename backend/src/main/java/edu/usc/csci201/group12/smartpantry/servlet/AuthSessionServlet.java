package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "authSession", urlPatterns = "/api/auth/session")
public final class AuthSessionServlet extends AbstractJsonServlet {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            writeOk(resp, Map.of(
                "loggedIn", true,
                "userId", u.getId(),
                "username", u.getUsername()
            ));
        } else {
            writeOk(resp, Map.of("loggedIn", false));
        }
    }
}
