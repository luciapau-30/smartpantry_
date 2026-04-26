package edu.usc.csci201.group12.smartpantry.servlet.member;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.usc.csci201.group12.smartpantry.dao.RecipeLikeDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeSaveDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// POST /api/member/recipes/{id}/like  — body: {"isLike": true|false}
// POST /api/member/recipes/{id}/save  — body: {"save": true|false}
// Exact-pattern servlets (recommend, upload, mine, saved) take priority over this wildcard.
@WebServlet("/api/member/recipes/*")
public final class RecipeInteractionServlet extends AbstractJsonServlet {

    private final RecipeLikeDao likeDao = new RecipeLikeDao();
    private final RecipeSaveDao saveDao = new RecipeSaveDao();

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
}
