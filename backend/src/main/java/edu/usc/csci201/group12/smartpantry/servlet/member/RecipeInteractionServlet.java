package edu.usc.csci201.group12.smartpantry.servlet.member;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeLikeDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeSaveDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import edu.usc.csci201.group12.smartpantry.websocket.PantryEventBroadcaster;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// POST /api/member/recipes/{id}/like  — body: {"isLike": true|false}
// POST /api/member/recipes/{id}/save  — body: {"save": true|false}
// POST /api/member/recipes/{id}/make  — no body; deducts matching pantry items
// Exact-pattern servlets (recommend, upload, mine, saved) take priority over this wildcard.
@WebServlet("/api/member/recipes/*")
public final class RecipeInteractionServlet extends AbstractJsonServlet {

    private final RecipeLikeDao likeDao = new RecipeLikeDao();
    private final RecipeSaveDao saveDao = new RecipeSaveDao();
    private final RecipeIngredientDao ingredientDao = new RecipeIngredientDao();
    private final PantryItemDao pantryItemDao = new PantryItemDao();

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody)
            throws IOException {
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, JsonApiResponse.fail("Login required"));
            return;
        }
        if (!(userOpt.get() instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, JsonApiResponse.fail("Member account required"));
            return;
        }

        // pathInfo = "/{recipeId}/like" or "/{recipeId}/save"
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Unknown action"));
            return;
        }
        String[] segments = pathInfo.split("/");
        // segments[0] = "" (before leading slash), segments[1] = recipeId, segments[2] = action
        if (segments.length < 3) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Unknown action"));
            return;
        }
        String recipeId = segments[1];
        String action = segments[2];

        JsonObject body = jsonBody != null && !jsonBody.isBlank()
                ? JsonParser.parseString(jsonBody).getAsJsonObject()
                : new JsonObject();

        switch (action) {
            case "like" -> handleLike(resp, member.getId(), recipeId, body);
            case "save" -> handleSave(resp, member.getId(), recipeId, body);
            case "make" -> handleMake(req, resp, member.getId(), recipeId);
            default -> writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Unknown action: " + action));
        }
    }

    private void handleLike(HttpServletResponse resp, String userId, String recipeId, JsonObject body)
            throws IOException {
        if (!body.has("isLike") || body.get("isLike").isJsonNull()) {
            // Remove reaction
            likeDao.deleteReaction(recipeId, userId);
            writeOk(resp);
            return;
        }
        boolean isLike = body.get("isLike").getAsBoolean();
        likeDao.upsertReaction(recipeId, userId, isLike);
        writeOk(resp);
    }

    private void handleSave(HttpServletResponse resp, String userId, String recipeId, JsonObject body)
            throws IOException {
        boolean save = !body.has("save") || body.get("save").getAsBoolean();
        if (save) {
            saveDao.saveRecipe(recipeId, userId);
        } else {
            saveDao.unsaveRecipe(recipeId, userId);
        }
        writeOk(resp);
    }

    private void handleMake(HttpServletRequest req, HttpServletResponse resp, String userId, String recipeId)
            throws IOException {
        List<RecipeIngredientRow> recipeIngredients = ingredientDao.getByRecipe(recipeId);
        if (recipeIngredients.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Recipe not found or has no ingredients"));
            return;
        }

        List<PantryItemRow> pantry = pantryItemDao.getByUser(userId);

        List<String> used = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (RecipeIngredientRow ri : recipeIngredients) {
            if (ri.isOptional()) continue;

            double needed = ri.getQuantity();
            String ingName = ri.getIngredientName() != null ? ri.getIngredientName() : ri.getIngredientId();

            // consume from matching pantry items (earliest-expiring first, already ordered by DAO)
            for (PantryItemRow p : pantry) {
                if (needed <= 0) break;
                if (!ri.getIngredientId().equals(p.getIngredientId())) continue;

                if (p.getQuantity() <= needed) {
                    needed -= p.getQuantity();
                    pantryItemDao.deleteItem(p.getId());
                    p.setQuantity(0);
                } else {
                    double remaining = p.getQuantity() - needed;
                    pantryItemDao.updateQuantity(p.getId(), remaining);
                    p.setQuantity(remaining);
                    needed = 0;
                }
            }

            if (needed > 0) {
                missing.add(ingName);
            } else {
                used.add(String.format("%.4g %s %s", ri.getQuantity(),
                        ri.getUnit() != null && !ri.getUnit().isBlank() ? ri.getUnit() : "", ingName).trim());
            }
        }

        PantryEventBroadcaster broadcaster = (PantryEventBroadcaster)
                req.getServletContext().getAttribute(ContextKeys.EVENT_BROADCASTER);
        if (broadcaster != null && !used.isEmpty()) {
            broadcaster.pantryUpdated(userId, "make_recipe", recipeId);
        }

        writeOk(resp, Map.of("used", used, "missing", missing));
    }
}
