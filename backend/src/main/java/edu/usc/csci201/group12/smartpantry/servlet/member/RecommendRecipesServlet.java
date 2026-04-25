package edu.usc.csci201.group12.smartpantry.servlet.member;

import edu.usc.csci201.group12.smartpantry.dao.UserStore;
import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.recommendation.PantryItem;
import edu.usc.csci201.group12.smartpantry.recommendation.RecipeRecommender;
import edu.usc.csci201.group12.smartpantry.recommendation.ScoredRecipe;
import edu.usc.csci201.group12.smartpantry.security.RequestUsers;
import edu.usc.csci201.group12.smartpantry.servlet.AbstractJsonServlet;
import edu.usc.csci201.group12.smartpantry.web.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GET /api/member/recipes/recommend
 *
 * <p>Returns a ranked list of recipes based on the logged-in member's pantry,
 * dietary preferences, and recipe popularity.
 *
 * <p>Requires a valid Member session. Guests receive 403.
 *
 * <p>Response shape:
 * <pre>{@code
 * {
 *   "recommendations": [
 *     {
 *       "recipe": { ... },
 *       "totalScore": 0.84,
 *       "pantryScore": 0.90,
 *       "prefScore": 0.50,
 *       "popularityScore": 0.50,
 *       "missingIngredients": 1
 *     },
 *     ...
 *   ]
 * }
 * }</pre>
 */
@WebServlet("/api/member/recipes/recommend")
public final class RecommendRecipesServlet extends AbstractJsonServlet {

    private static final String FETCH_PANTRY_SQL = """
            SELECT id, ingredient_id, quantity, unit, expiration_date
            FROM PANTRY_ITEMS
            WHERE user_id = ?
            """;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserStore userStore = (UserStore) req.getServletContext().getAttribute(ContextKeys.USER_STORE);
        Optional<User> maybeUser = RequestUsers.currentUser(req, userStore);

        if (maybeUser.isEmpty()) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    edu.usc.csci201.group12.smartpantry.json.JsonApiResponse.fail("Login required"));
            return;
        }
        if (!(maybeUser.get() instanceof Member member)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN,
                    edu.usc.csci201.group12.smartpantry.json.JsonApiResponse.fail("Member account required"));
            return;
        }

        RecipeRecommender recommender =
                (RecipeRecommender) req.getServletContext().getAttribute(ContextKeys.RECOMMENDER);

        List<PantryItem> pantry;
        try {
            pantry = fetchPantry(member.getId());
        } catch (SQLException e) {
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    edu.usc.csci201.group12.smartpantry.json.JsonApiResponse.fail("Failed to load pantry"));
            return;
        }

        List<ScoredRecipe> recommendations = recommender.recommend(member, pantry);
        writeOk(resp, Map.of("recommendations", recommendations));
    }

    private List<PantryItem> fetchPantry(String userId) throws SQLException {
        List<PantryItem> items = new ArrayList<>();
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement ps = conn.prepareStatement(FETCH_PANTRY_SQL)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate exp = rs.getDate("expiration_date") != null
                            ? rs.getDate("expiration_date").toLocalDate()
                            : null;
                    items.add(new PantryItem(
                            rs.getString("id"),
                            rs.getString("ingredient_id"),
                            null,   // ingredient name not stored in PANTRY_ITEMS; synonym matching falls back to ID
                            rs.getBigDecimal("quantity"),
                            rs.getString("unit"),
                            exp
                    ));
                }
            }
        }
        return items;
    }
}
