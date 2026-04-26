package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.CommentDao;
import edu.usc.csci201.group12.smartpantry.dao.CommentRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeLikeDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeSaveDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeStep;
import edu.usc.csci201.group12.smartpantry.dao.RecipeStepDao;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeDao;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// GET /api/recipes/{id}           — full recipe detail (ingredients, steps, like counts)
// GET /api/recipes/{id}/comments  — threaded comments for a recipe
// Exact-pattern servlets (/api/recipes/trending, /api/recipes/top) take priority over this wildcard.
@WebServlet("/api/recipes/*")
public final class RecipeDetailServlet extends AbstractJsonServlet {

    private final RecipeDao recipeDao = new RecipeDao();
    private final RecipeIngredientDao ingredientDao = new RecipeIngredientDao();
    private final RecipeStepDao stepDao = new RecipeStepDao();
    private final RecipeLikeDao likeDao = new RecipeLikeDao();
    private final RecipeSaveDao saveDao = new RecipeSaveDao();
    private final CommentDao commentDao = new CommentDao();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // pathInfo: "/{id}" or "/{id}/comments"
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("Recipe id required"));
            return;
        }

        String[] segments = pathInfo.split("/");
        // segments[0] = "" (before leading slash), segments[1] = recipeId
        String recipeId = segments[1];
        boolean wantsComments = segments.length >= 3 && "comments".equals(segments[2]);

        RecipeRow recipe = recipeDao.getById(recipeId);
        if (recipe == null) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, JsonApiResponse.fail("Recipe not found"));
            return;
        }

        if (wantsComments) {
            List<CommentRow> comments = commentDao.getTopLevelByRecipe(recipeId);
            writeOk(resp, comments);
            return;
        }

        // Full recipe detail response
        List<RecipeIngredientRow> ingredients = ingredientDao.getByRecipe(recipeId);
        List<String> steps = stepDao.getByRecipe(recipeId).stream()
                .map(RecipeStep::getInstruction)
                .collect(Collectors.toList());
        int likes = likeDao.countLikes(recipeId);
        int dislikes = likeDao.countDislikes(recipeId);
        int saves = saveDao.countSaves(recipeId);

        // Check if the current user has reacted (optional, null if not logged in)
        Boolean myReaction = null;
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        var userOpt = RequestUsers.currentUser(req, store);
        if (userOpt.isPresent()) {
            myReaction = likeDao.getUserReaction(recipeId, userOpt.get().getId());
        }

        RecipeDetailResponse detail = new RecipeDetailResponse(
                recipe, ingredients, steps, likes, dislikes, saves, myReaction);
        writeOk(resp, detail);
    }

    private record RecipeDetailResponse(
            RecipeRow recipe,
            List<RecipeIngredientRow> ingredients,
            List<String> steps,
            int likes,
            int dislikes,
            int saves,
            Boolean myReaction
    ) {}
}
