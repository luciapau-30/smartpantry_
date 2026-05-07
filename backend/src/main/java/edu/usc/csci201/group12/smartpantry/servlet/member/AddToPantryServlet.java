package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.api.member.AddToPantryRequest;
import edu.usc.csci201.group12.smartpantry.dao.Ingredient;
import edu.usc.csci201.group12.smartpantry.dao.IngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.recommendation.IngredientSynonymResolver;
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
        if (in == null || in.quantity() == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("quantity required"));
            return;
        }

        // Resolve ingredient: prefer explicit ID, otherwise look up by name (or create if new)
        String ingredientId = in.ingredientId();
        if (ingredientId == null || ingredientId.isBlank()) {
            String name = in.itemName();
            if (name == null || name.isBlank()) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("ingredientId or itemName required"));
                return;
            }
            IngredientDao dao = new IngredientDao();
            // Run synonym resolver first: "steak" → "beef", "roma tomatoes" → "tomatoes"
            String canonical = IngredientSynonymResolver.loadFromClasspath().canonical(name.trim().toLowerCase());
            Ingredient ing = dao.getByName(canonical);
            if (ing == null && !canonical.equalsIgnoreCase(name.trim())) {
                ing = dao.getByName(name.trim()); // try original if canonical not found
            }
            if (ing == null) {
                // Truly unknown — create a new ingredient so the item can still be saved
                String storeName = canonical.isEmpty() ? name.trim() : canonical;
                dao.createIngredient(storeName, "other", in.unit() != null ? in.unit() : "each", null);
                ing = dao.getByName(storeName);
            }
            if (ing == null) {
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Could not resolve ingredient"));
                return;
            }
            ingredientId = ing.getId();
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
            String pantryItemId = member.addToPantry(ingredientId, in.quantity(), in.unit(), expiration);
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
