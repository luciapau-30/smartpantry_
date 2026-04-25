package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.model.Guest;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeSummary;
import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Exposes guest recipe browse/search. Logged-in {@link Guest} uses domain methods; anonymous callers use the repository directly with the same visibility rules for now.
 */
@WebServlet(name = "guestRecipes", urlPatterns = "/api/recipes")
public final class GuestRecipeServlet extends AbstractJsonServlet {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ingredient = first(req.getParameter("ingredient"), req.getParameter("q"));
        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        RecipeRepository recipes = (RecipeRepository) req.getServletContext().getAttribute(ContextKeys.RECIPE_REPOSITORY);

        User viewer = resolveViewer(req, store);
        List<RecipeSummary> list;
        if (viewer instanceof Guest guest) {
            list = (ingredient == null || ingredient.isBlank())
                    ? guest.browseRecipes()
                    : guest.searchByIngredient(ingredient);
        } else {
            String needle = ingredient == null ? "" : ingredient.trim().toLowerCase(Locale.ROOT);
            list = needle.isBlank()
                    ? recipes.listPublishedForGuest(viewer)
                    : recipes.findByIngredient(viewer, needle);
        }

        writeOk(resp, Map.of("recipes", list));
    }

    private static User resolveViewer(HttpServletRequest req, UserStore store) {
        var session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object raw = session.getAttribute(SessionAttributes.CURRENT_USER_ID);
        if (!(raw instanceof String userId) || userId.isBlank()) {
            return null;
        }
        return store.findById(userId).orElse(null);
    }

    private static String first(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
