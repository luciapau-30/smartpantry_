package edu.usc.csci201.group12.smartpantry.security;

import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/** Resolves the logged-in {@link User} from the session + {@link UserStore}. */
public final class RequestUsers {

    private RequestUsers() {
    }

    public static Optional<User> currentUser(HttpServletRequest req, UserStore store) {
        var session = req.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object raw = session.getAttribute(SessionAttributes.CURRENT_USER_ID);
        if (!(raw instanceof String userId) || userId.isBlank()) {
            return Optional.empty();
        }
        return store.findById(userId);
    }
}
