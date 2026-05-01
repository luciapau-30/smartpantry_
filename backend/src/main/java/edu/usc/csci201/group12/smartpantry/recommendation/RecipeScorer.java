package edu.usc.csci201.group12.smartpantry.recommendation;

import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe.RecipeIngredient;

import java.util.List;

/**
 * Computes the three sub-scores and the final weighted score for a recipe.
 *
 * <pre>
 *   RecScore(p1, p2, ...) = w1 * pantryScore(pantry, recipe)
 *                         + w2 * prefScore(user, recipe)
 *                         + w3 * popularityScore(recipe, commentCount)
 * </pre>
 *
 * Each sub-score is normalised to [0.0, 1.0].
 */
public final class RecipeScorer {

    private final PantryMatcher matcher;

    public RecipeScorer(PantryMatcher matcher) {
        this.matcher = matcher;
    }

    // ── pantryScore ──────────────────────────────────────────────────────────

    /**
     * Fraction of required ingredients covered by the pantry, weighted by
     * quantity availability.
     *
     * <p>Optional ingredients contribute a smaller bonus so they don't unfairly
     * boost recipes where the user only has the garnish.
     *
     * @return value in [0.0, 1.0]
     */
    public double pantryScore(Recipe recipe, List<PantryItem> pantry) {
        List<RecipeIngredient> all = recipe.getIngredientsList();
        if (all.isEmpty()) return 0.0;

        double requiredTotal = 0.0;
        double requiredCovered = 0.0;
        double optionalBonus = 0.0;
        int optionalCount = 0;

        for (RecipeIngredient ri : all) {
            PantryMatcher.CoverageResult coverage = matcher.check(ri, pantry);
            if (ri.isOptional()) {
                if (coverage.ingredientFound()) optionalBonus += coverage.quantityRatio();
                optionalCount++;
            } else {
                requiredTotal += 1.0;
                requiredCovered += coverage.quantityRatio();
            }
        }

        double base = (requiredTotal == 0) ? 1.0 : (requiredCovered / requiredTotal);

        // Optional ingredients contribute up to 10 % bonus
        double bonus = 0.0;
        if (optionalCount > 0) {
            bonus = 0.1 * (optionalBonus / optionalCount);
        }

        return Math.min(base + bonus, 1.0);
    }

    /**
     * Counts how many required ingredients are completely missing from the pantry.
     */
    public int missingRequiredCount(Recipe recipe, List<PantryItem> pantry) {
        return (int) recipe.getIngredientsList().stream()
                .filter(ri -> !ri.isOptional())
                .filter(ri -> !matcher.check(ri, pantry).ingredientFound())
                .count();
    }

    // ── prefScore ────────────────────────────────────────────────────────────

    /**
     * How well the recipe matches the user's cuisine preferences.
     *
     * <p>Currently a stub returning 0.5 (neutral).  Wire in user preference data
     * when the preference model is available (e.g. store preferred cuisines in the
     * Member row and pass them here).
     *
     * @return value in [0.0, 1.0]
     */
    public double prefScore(User user, Recipe recipe) {
        if (user == null) return 0.5;
        List<String> prefs = user.getPreferredCuisines();
        if (prefs == null || prefs.isEmpty()) return 0.5;
        String cuisine = recipe.getCuisineType();
        if (cuisine == null) return 0.2;
        boolean match = prefs.stream().anyMatch(p -> p.equalsIgnoreCase(cuisine.trim()));
        return match ? 1.0 : 0.2;
    }

    // ── popularityScore ──────────────────────────────────────────────────────

    /**
     * Normalised popularity based on the recipe's comment count relative to the
     * most-commented recipe in the candidate pool.
     *
     * @param commentCount     number of comments on this recipe
     * @param maxCommentCount  highest comment count across all candidate recipes
     *                         (pass 0 to get a neutral 0.5 for all recipes)
     * @return value in [0.0, 1.0]
     */
    public double popularityScore(int commentCount, int maxCommentCount) {
        if (maxCommentCount <= 0) return 0.5;
        return (double) commentCount / maxCommentCount;
    }

    // ── weighted total ───────────────────────────────────────────────────────

    /**
     * Applies the scoring formula:
     * {@code w1*pantryScore + w2*prefScore + w3*popularityScore}
     */
    public double total(double pantryScore, double prefScore, double popularityScore, ScoreWeights w) {
        return w.w1Pantry() * pantryScore
             + w.w2Preference() * prefScore
             + w.w3Popularity() * popularityScore;
    }
}
