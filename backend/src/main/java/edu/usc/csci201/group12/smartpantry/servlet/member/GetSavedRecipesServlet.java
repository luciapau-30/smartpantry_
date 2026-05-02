package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.dao.RecipeSaveDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeDao;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/member/recipes/saved")
public final class GetSavedRecipesServlet extends AbstractJsonServlet {

    private final RecipeSaveDao recipeSaveDao = new RecipeSaveDao();
    private final RecipeDao recipeDao = new RecipeDao();

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

        List<String> ids = recipeSaveDao.getSavedRecipeIdsByUser(member.getId());
        List<RecipeRow> recipes = new ArrayList<>();
        for (String id : ids) {
            RecipeRow row = recipeDao.getById(id);
            if (row != null) recipes.add(row);
        }
        writeOk(resp, recipes);
    }
}
