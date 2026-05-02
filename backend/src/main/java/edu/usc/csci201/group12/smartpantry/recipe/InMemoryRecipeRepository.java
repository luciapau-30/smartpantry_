// MERGE (lucia/recommendation-engine → dev): Added publishedFull list with full ingredient
// quantities and listAllPublishedFull() method so the recommendation engine can score recipes.
package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe.RecipeIngredient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Development stub with sample recipes. Safe to delete once a real DAO is wired.
 */
public final class InMemoryRecipeRepository implements RecipeRepository {

    private final List<RecipeSummary> published = List.of(
            new RecipeSummary(
                    "r1",
                    "Lentil soup",
                    "Hearty soup with pantry staples.",
                    List.of("lentils", "carrot", "onion", "vegetable stock")),
            new RecipeSummary(
                    "r2",
                    "Tomato pasta",
                    "Quick weeknight pasta.",
                    List.of("pasta", "tomato", "garlic", "olive oil")),
            new RecipeSummary(
                    "r3",
                    "Garden salad",
                    "Fresh side salad.",
                    List.of("lettuce", "tomato", "cucumber", "olive oil")));

    /**
     * Full Recipe objects with quantities, used by the recommendation engine.
     * Kept in sync with {@code published} by ID.
     */
    private final List<Recipe> publishedFull = List.of(
            new Recipe(
                    "r1", "system", Instant.now(), Instant.now(),
                    "Lentil soup", "Hearty soup with pantry staples.", "", 10, 30, 4, true, "Mediterranean",
                    List.of(
                            new RecipeIngredient("ing-lentils",  "ing-lentils",  "lentils",          new BigDecimal("200"), "g",    "", false),
                            new RecipeIngredient("ing-carrot",   "ing-carrot",   "carrot",            new BigDecimal("2"),   "whole","", false),
                            new RecipeIngredient("ing-onion",    "ing-onion",    "onion",             new BigDecimal("1"),   "whole","", false),
                            new RecipeIngredient("ing-veg-broth","ing-veg-broth","vegetable broth",   new BigDecimal("1"),   "l",    "", false)
                    ),
                    List.of("Dice the vegetables.", "Simmer lentils and vegetables in broth for 30 min.")),
            new Recipe(
                    "r2", "system", Instant.now(), Instant.now(),
                    "Tomato pasta", "Quick weeknight pasta.", "", 5, 20, 2, true, "Italian",
                    List.of(
                            new RecipeIngredient("ing-pasta",    "ing-pasta",    "pasta",             new BigDecimal("200"), "g",    "", false),
                            new RecipeIngredient("ing-tomatoes", "ing-tomatoes", "tomatoes",          new BigDecimal("3"),   "whole","", false),
                            new RecipeIngredient("ing-garlic",   "ing-garlic",   "garlic",            new BigDecimal("3"),   "cloves","", false),
                            new RecipeIngredient("ing-olive-oil","ing-olive-oil","olive oil",         new BigDecimal("2"),   "tbsp", "", false)
                    ),
                    List.of("Cook pasta al dente.", "Sauté garlic in olive oil, add tomatoes, toss with pasta.")),
            new Recipe(
                    "r3", "system", Instant.now(), Instant.now(),
                    "Garden salad", "Fresh side salad.", "", 10, 0, 2, true, "American",
                    List.of(
                            new RecipeIngredient("ing-lettuce",  "ing-lettuce",  "lettuce",           new BigDecimal("1"),   "whole","", false),
                            new RecipeIngredient("ing-tomatoes", "ing-tomatoes", "tomatoes",          new BigDecimal("2"),   "whole","", false),
                            new RecipeIngredient("ing-cucumber", "ing-cucumber", "cucumber",          new BigDecimal("1"),   "whole","", false),
                            new RecipeIngredient("ing-olive-oil","ing-olive-oil","olive oil",         new BigDecimal("2"),   "tbsp", "", true)
                    ),
                    List.of("Chop vegetables.", "Toss with olive oil and serve.")));

    @Override
    public List<RecipeSummary> listPublishedForGuest(User viewer) {
        return new ArrayList<>(published);
    }

    @Override
    public List<RecipeSummary> findByIngredient(User viewer, String ingredientNormalizedLowercase) {
        if (ingredientNormalizedLowercase == null || ingredientNormalizedLowercase.isBlank()) {
            return List.of();
        }
        String needle = ingredientNormalizedLowercase.trim().toLowerCase(Locale.ROOT);
        return published.stream()
                .filter(r -> r.ingredients().stream()
                        .map(i -> i.toLowerCase(Locale.ROOT))
                        .anyMatch(i -> i.contains(needle)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> listAllPublishedFull(User viewer) {
        return new ArrayList<>(publishedFull);
    }
}
