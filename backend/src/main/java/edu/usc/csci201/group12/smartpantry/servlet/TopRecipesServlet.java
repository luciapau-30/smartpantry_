package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.RecipeLikeDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeDao;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// GET /api/recipes/top — all-time most liked recipes.
// Two-step: (1) get ranked IDs from RECIPE_LIKES, (2) fetch each recipe row by ID.
@WebServlet("/api/recipes/top")
public final class TopRecipesServlet extends AbstractJsonServlet {

    private static final int LIMIT = 20;

    private final RecipeLikeDao likeDao = new RecipeLikeDao();
    private final RecipeDao recipeDao = new RecipeDao();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> ids = likeDao.getTopLikedRecipeIds(LIMIT);
        List<RecipeRow> recipes = new ArrayList<>();
        for (String id : ids) {
            RecipeRow row = recipeDao.getById(id);
            if (row != null) recipes.add(row);
        }
        writeOk(resp, recipes);
    }
}
