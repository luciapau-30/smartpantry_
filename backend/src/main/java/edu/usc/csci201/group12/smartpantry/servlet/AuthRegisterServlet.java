package edu.usc.csci201.group12.smartpantry.servlet;

import edu.usc.csci201.group12.smartpantry.api.auth.RegisterRequest;
import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.json.GsonProvider;
import edu.usc.csci201.group12.smartpantry.json.JsonApiResponse;
import edu.usc.csci201.group12.smartpantry.model.Guest;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
import edu.usc.csci201.group12.smartpantry.security.PasswordHasher;
import edu.usc.csci201.group12.smartpantry.security.SessionAttributes;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "authRegister", urlPatterns = "/api/auth/register")
public final class AuthRegisterServlet extends AbstractJsonServlet {

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, String jsonBody) throws IOException {
        RegisterRequest in = GsonProvider.get().fromJson(jsonBody, RegisterRequest.class);
        if (in == null) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail("Expected JSON body"));
            return;
        }

        UserStore store = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        PasswordHasher hasher = (PasswordHasher) req.getServletContext().getAttribute(ContextKeys.PASSWORD_HASHER);
        RecipeRepository recipes = (RecipeRepository) req.getServletContext().getAttribute(ContextKeys.RECIPE_REPOSITORY);

        final User account;
        try {
            String hash = hasher.hash(in.password());
            if ("member".equalsIgnoreCase(in.accountType())) {
                account = new Member(in.username(), in.email(), hash);
            } else {
                account = new Guest(in.username(), in.email(), hash, recipes);
            }
            store.save(account);
        } catch (IllegalArgumentException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, JsonApiResponse.fail(ex.getMessage()));
            return;
        } catch (UserStore.DuplicateUserException ex) {
            writeJson(resp, HttpServletResponse.SC_CONFLICT, JsonApiResponse.fail(ex.getMessage()));
            return;
        }

        req.getSession(true).setAttribute(SessionAttributes.CURRENT_USER_ID, account.getId());
        writeOk(resp, Map.of("user", account.toPublicView()));
    }
}
