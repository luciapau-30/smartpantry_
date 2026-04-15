package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;

import java.util.List;

/**
 * Recipe persistence boundary. Replace with JDBC/JPA implementation when the schema exists.
 * {@code viewer} may be {@code null} for anonymous browse; future rules can use it for personalization.
 */
public interface RecipeRepository {

    List<RecipeSummary> listPublishedForGuest(User viewer);

    List<RecipeSummary> findByIngredient(User viewer, String ingredientNormalizedLowercase);

    /**
     * Returns full {@link Recipe} objects (including ingredient quantities and units)
     * for all public recipes.  Used by the recommendation engine.
     */
    List<Recipe> listAllPublishedFull(User viewer);
}
