package edu.usc.csci201.group12.smartpantry.recommendation;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full recommendation pipeline.
 *
 * <ol>
 *   <li>Fetches all public recipes from the repository.</li>
 *   <li>Scores each recipe via {@link RecipeScorer}.</li>
 *   <li>Returns the list sorted by {@code totalScore} descending.</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>{@code
 *   List<PantryItem> pantry = ...; // loaded from DB for the current user
 *   List<ScoredRecipe> recommendations = recommender.recommend(user, pantry);
 * }</pre>
 */
public final class RecipeRecommender {

    private final RecipeRepository recipeRepository;
    private final RecipeScorer scorer;
    private final ScoreWeights weights;

    public RecipeRecommender(RecipeRepository recipeRepository,
                             RecipeScorer scorer,
                             ScoreWeights weights) {
        this.recipeRepository = recipeRepository;
        this.scorer = scorer;
        this.weights = weights;
    }

    /**
     * Generates a ranked recommendation list for the given user and pantry.
     *
     * @param user   the logged-in user (used for preference scoring)
     * @param pantry the user's current pantry items
     * @return recipes ranked by total score, highest first
     */
    public List<ScoredRecipe> recommend(User user, List<PantryItem> pantry) {
        List<Recipe> candidates = recipeRepository.listAllPublishedFull(user);

        // Pre-compute max comment count for popularity normalisation
        int maxComments = candidates.stream()
                .mapToInt(r -> getCommentCount(r.getId()))
                .max()
                .orElse(0);

        return candidates.stream()
                .map(recipe -> score(recipe, user, pantry, maxComments))
                .sorted(Comparator.<ScoredRecipe>comparingDouble(r -> r.totalScore()).reversed())
                .toList();
    }

    private ScoredRecipe score(Recipe recipe, User user, List<PantryItem> pantry, int maxComments) {
        double pantryS    = scorer.pantryScore(recipe, pantry);
        double prefS      = scorer.prefScore(user, recipe);
        double popularS   = scorer.popularityScore(getCommentCount(recipe.getId()), maxComments);
        double total      = scorer.total(pantryS, prefS, popularS, weights);
        int missing       = scorer.missingRequiredCount(recipe, pantry);

        return new ScoredRecipe(recipe, total, pantryS, prefS, popularS, missing);
    }

    private static final String COUNT_COMMENTS_SQL =
            "SELECT COUNT(*) FROM COMMENTS WHERE recipe_id = ? AND is_deleted = FALSE";

    private int getCommentCount(String recipeId) {
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_COMMENTS_SQL)) {
            ps.setString(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }
}
