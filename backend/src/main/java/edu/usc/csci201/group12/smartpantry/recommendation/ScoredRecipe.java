package edu.usc.csci201.group12.smartpantry.recommendation;

import edu.usc.csci201.group12.smartpantry.model.content.Recipe;

/**
 * A recipe bundled with its recommendation scores.
 * Returned by {@link RecipeRecommender} and serialised directly to the API response.
 *
 * @param recipe               the full recipe
 * @param totalScore           final weighted score in [0.0, 1.0]
 * @param pantryScore          pantry-coverage sub-score in [0.0, 1.0]
 * @param prefScore            preference-match sub-score in [0.0, 1.0]
 * @param popularityScore      popularity sub-score in [0.0, 1.0]
 * @param missingIngredients   number of required ingredients not in the pantry
 */
public record ScoredRecipe(
        Recipe recipe,
        double totalScore,
        double pantryScore,
        double prefScore,
        double popularityScore,
        int missingIngredients
) {}
