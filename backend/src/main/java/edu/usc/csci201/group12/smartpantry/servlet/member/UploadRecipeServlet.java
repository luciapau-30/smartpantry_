package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.api.member.UploadRecipeRequest;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "uploadRecipe", urlPatterns = "/api/member/recipes/upload")
public final class UploadRecipeServlet extends AbstractJsonServlet {

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

        UploadRecipeRequest in = GsonProvider.get().fromJson(jsonBody, UploadRecipeRequest.class);
        if (in == null || in.title == null || in.cuisineType == null || in.ingredients == null || in.ingredients.isEmpty()
                || in.instructions == null || in.instructions.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("title, cuisineType, ingredients[], instructions[] required"));
            return;
        }

        List<Recipe.RecipeIngredient> lines = new ArrayList<>();
        for (UploadRecipeRequest.IngredientLine line : in.ingredients) {
            if (line == null || line.ingredientId == null || line.quantity == null) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("Each ingredient needs ingredientId and quantity"));
                return;
            }
            boolean optional = line.optional != null && line.optional;
            lines.add(new Recipe.RecipeIngredient(
                    UUID.randomUUID().toString(),
                    line.ingredientId,
                    null,
                    line.quantity,
                    line.unit == null ? "" : line.unit,
                    line.notes == null ? "" : line.notes,
                    optional));
        }

        Recipe recipe = new Recipe(member.getId(), in.title, lines, in.instructions, in.cuisineType);
        recipe.setId(UUID.randomUUID().toString());
        if (in.description != null) {
            recipe.setDescription(in.description);
        }
        if (in.imageUrl != null) {
            recipe.setImageUrl(in.imageUrl);
        }
        if (in.prepTimeMin != null) {
            recipe.setPrepTimeMinutes(in.prepTimeMin);
        }
        if (in.cookTimeMin != null) {
            recipe.setCookTimeMinutes(in.cookTimeMin);
        }
        if (in.servings != null) {
            recipe.setServings(in.servings);
        }
        if (in.isPublic != null) {
            recipe.setPublic(in.isPublic);
        }

        try {
            String recipeId = member.uploadRecipe(recipe);
            writeJson(resp, HttpServletResponse.SC_CREATED, JsonApiResponse.ok(Map.of("recipeId", recipeId)));
        } catch (SQLException ex) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonApiResponse.fail("Database error"));
        }
    }
}
