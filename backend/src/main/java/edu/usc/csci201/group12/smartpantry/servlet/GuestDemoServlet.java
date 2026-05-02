package edu.usc.csci201.group12.smartpantry.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.usc.csci201.group12.smartpantry.dao.Ingredient;
import edu.usc.csci201.group12.smartpantry.dao.IngredientDao;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.recommendation.PantryItem;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeRecommender;
import edu.usc.csci201.group12.smartpantry.recommendation.ScoredRecipe;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * POST /api/guest/demo
 * Body: {"ingredients": ["tomatoes", "garlic", "pasta"]}
 * Returns top 5 recipe recommendations based on the supplied ingredient list.
 * No auth required — one-time guest try-it experience (F15).
 */
@WebServlet("/api/guest/demo")
public final class GuestDemoServlet extends AbstractJsonServlet {

    private static final int MAX_RESULTS = 5;
    private final IngredientDao ingredientDao = new IngredientDao();

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody)
            throws IOException {
        RecipeRecommender recommender =
                (RecipeRecommender) req.getServletContext().getAttribute(ContextKeys.RECOMMENDER);
        if (recommender == null) {
            writeJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    JsonApiResponse.fail("Recommendation engine not available"));
            return;
        }

        List<String> names = parseNames(jsonBody);
        if (names.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    JsonApiResponse.fail("Provide at least one ingredient name in ingredients[]"));
            return;
        }

        // Resolve names → PantryItems; fall back to name-only if not in DB
        List<PantryItem> tempPantry = new ArrayList<>();
        for (String name : names) {
            Ingredient ing = null;
            try { ing = ingredientDao.getByName(name); } catch (Exception ignored) {}
            String id   = ing != null ? ing.getId()   : UUID.randomUUID().toString();
            String canonical = ing != null ? ing.getName() : name;
            tempPantry.add(new PantryItem(
                    UUID.randomUUID().toString(), id, canonical,
                    BigDecimal.valueOf(999), "cup", null));
        }

        List<ScoredRecipe> ranked = recommender.recommend(null, tempPantry);
        List<Map<String, Object>> results = ranked.stream()
                .limit(MAX_RESULTS)
                .map(sr -> Map.<String, Object>of(
                        "id",           sr.recipe().getId(),
                        "title",        sr.recipe().getTitle(),
                        "description",  sr.recipe().getDescription() != null ? sr.recipe().getDescription() : "",
                        "cuisineType",  sr.recipe().getCuisineType(),
                        "pantryScore",  Math.round(sr.pantryScore() * 100),
                        "totalScore",   Math.round(sr.totalScore() * 100),
                        "missing",      sr.missingIngredients()
                ))
                .toList();

        writeOk(resp, results);
    }

    private static List<String> parseNames(String body) {
        List<String> names = new ArrayList<>();
        try {
            JsonArray arr = JsonParser.parseString(body)
                    .getAsJsonObject()
                    .getAsJsonArray("ingredients");
            for (var el : arr) {
                String s = el.getAsString().trim();
                if (!s.isEmpty()) names.add(s);
            }
        } catch (Exception ignored) {}
        return names;
    }
}
