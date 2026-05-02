package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.dao.PantryItemDao;
import edu.usc.csci201.group12.smartpantry.dao.PantryItemRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientRow;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /api/member/shopping-list
 * Returns ingredients needed to make all saved recipes that aren't already in the pantry.
 * Aggregates required quantities across all saved recipes, subtracts pantry stock.
 */
@WebServlet("/api/member/shopping-list")
public final class ShoppingListServlet extends AbstractJsonServlet {

    private final RecipeSaveDao saveDao = new RecipeSaveDao();
    private final RecipeIngredientDao ingredientDao = new RecipeIngredientDao();
    private final PantryItemDao pantryItemDao = new PantryItemDao();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        List<String> savedIds = saveDao.getSavedRecipeIdsByUser(member.getId());

        // Sum required quantities per ingredient across all saved recipes
        // key = ingredientId, value = {name, totalRequired, unit}
        Map<String, double[]> required = new HashMap<>();   // [totalRequired]
        Map<String, String>   names    = new HashMap<>();
        Map<String, String>   units    = new HashMap<>();

        for (String recipeId : savedIds) {
            for (RecipeIngredientRow ri : ingredientDao.getByRecipe(recipeId)) {
                if (ri.isOptional()) continue;
                required.merge(ri.getIngredientId(), new double[]{ri.getQuantity()},
                        (a, b) -> new double[]{a[0] + b[0]});
                names.putIfAbsent(ri.getIngredientId(),
                        ri.getIngredientName() != null ? ri.getIngredientName() : ri.getIngredientId());
                units.putIfAbsent(ri.getIngredientId(),
                        ri.getUnit() != null ? ri.getUnit() : "");
            }
        }

        // Subtract what's already in pantry
        Map<String, Double> pantryStock = new HashMap<>();
        for (PantryItemRow p : pantryItemDao.getByUser(member.getId())) {
            pantryStock.merge(p.getIngredientId(), p.getQuantity(), Double::sum);
        }

        List<Map<String, Object>> shoppingList = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : required.entrySet()) {
            String ingId = entry.getKey();
            double needed = entry.getValue()[0];
            double have   = pantryStock.getOrDefault(ingId, 0.0);
            double toBuy  = needed - have;
            if (toBuy > 0.001) {
                shoppingList.add(Map.of(
                        "ingredientId",   ingId,
                        "ingredientName", names.get(ingId),
                        "quantity",       Math.round(toBuy * 100.0) / 100.0,
                        "unit",           units.get(ingId)
                ));
            }
        }

        // Sort alphabetically by name for readability
        shoppingList.sort((a, b) -> String.valueOf(a.get("ingredientName"))
                .compareToIgnoreCase(String.valueOf(b.get("ingredientName"))));

        writeOk(resp, shoppingList);
    }
}
