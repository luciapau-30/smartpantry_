package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.api.member.AddToPantryRequest;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@WebServlet(name = "addToPantry", urlPatterns = "/api/member/pantry/add")
public final class AddToPantryServlet extends AbstractJsonServlet {

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody) throws IOException {
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Login required"));
            return;
        }
        User user = userOpt.get();
        if (!(user instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Member account required"));
            return;
        }

        AddToPantryRequest in = GsonProvider.get().fromJson(jsonBody, AddToPantryRequest.class);
        if (in == null || in.ingredientId() == null || in.quantity() == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("ingredientId and quantity required"));
            return;
        }

        LocalDate expiration = null;
        if (in.expirationDate() != null && !in.expirationDate().isBlank()) {
            try {
                expiration = LocalDate.parse(in.expirationDate().trim());
            } catch (DateTimeParseException ex) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("expirationDate must be YYYY-MM-DD"));
                return;
            }
        }

        try {
            String pantryItemId = member.addToPantry(in.ingredientId(), in.quantity(), in.unit(), expiration);
            PantryEventBroadcaster broadcaster = (PantryEventBroadcaster)
                    req.getServletContext().getAttribute(ContextKeys.EVENT_BROADCASTER);
            if (broadcaster != null) {
                broadcaster.pantryUpdated(member.getId(), "added", pantryItemId);
            }
            writeJson(resp, HttpServletResponse.SC_CREATED, JsonApiResponse.ok(Map.of("pantryItemId", pantryItemId)));
        } catch (SQLException ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Database error"));
        }
    }
}
