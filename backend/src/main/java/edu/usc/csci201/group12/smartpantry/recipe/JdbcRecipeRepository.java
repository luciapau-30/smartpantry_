// NEW: Implements RecipeRepository using RecipeDao, RecipeIngredientDao, and RecipeStepDao.
// Replaces InMemoryRecipeRepository when PANTRY_DB_URL is set.
// listAllPublishedFull() builds Archit's Recipe domain objects needed by the recommendation engine.
package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientDao;
import edu.usc.csci201.group12.smartpantry.dao.RecipeIngredientRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.dao.RecipeStep;
import edu.usc.csci201.group12.smartpantry.dao.RecipeStepDao;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe.RecipeIngredient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class JdbcRecipeRepository implements RecipeRepository {

    private final RecipeDao recipeDao;
    private final RecipeIngredientDao ingredientDao;
    private final RecipeStepDao stepDao;

    public JdbcRecipeRepository(RecipeDao recipeDao,
                                RecipeIngredientDao ingredientDao,
                                RecipeStepDao stepDao) {
        this.recipeDao = recipeDao;
        this.ingredientDao = ingredientDao;
        this.stepDao = stepDao;
    }

    @Override
    public List<RecipeSummary> listPublishedForGuest(User viewer) {
        return recipeDao.getPublicRecipes().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummary> findByIngredient(User viewer, String ingredientNormalizedLowercase) {
        if (ingredientNormalizedLowercase == null || ingredientNormalizedLowercase.isBlank()) {
            return List.of();
        }
        return recipeDao.searchByIngredientName(ingredientNormalizedLowercase.trim().toLowerCase(Locale.ROOT))
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> listAllPublishedFull(User viewer) {
        List<Recipe> result = new ArrayList<>();
        for (RecipeRow row : recipeDao.getPublicRecipes()) {
            Recipe recipe = toFullRecipe(row);
            if (recipe != null) result.add(recipe);
        }
        return result;
    }

    // Maps a RecipeRow to the lightweight RecipeSummary used by browse/search responses.
    private RecipeSummary toSummary(RecipeRow row) {
        List<String> ingredientNames = ingredientDao.getByRecipe(row.getId()).stream()
                .map(ri -> ri.getIngredientName() != null ? ri.getIngredientName() : ri.getIngredientId())
                .collect(Collectors.toList());
        return new RecipeSummary(row.getId(), row.getTitle(), row.getDescription(), ingredientNames,
                row.getPrepTimeMin(), row.getCookTimeMin(), row.getCategoryTags());
    }

    // Builds Archit's full Recipe domain object from DB rows.
    // Returns null and skips the recipe if it has no ingredients (Recipe requires at least one).
    private Recipe toFullRecipe(RecipeRow row) {
        List<RecipeIngredientRow> riRows = ingredientDao.getByRecipe(row.getId());
        if (riRows.isEmpty()) return null;

        List<RecipeIngredient> ingredients = riRows.stream()
                .map(ri -> new RecipeIngredient(
                        ri.getId(),
                        ri.getIngredientId(),
                        null,
                        BigDecimal.valueOf(ri.getQuantity()),
                        ri.getUnit(),
                        ri.getNotes() != null ? ri.getNotes() : "",
                        ri.isOptional()))
                .collect(Collectors.toList());

        List<String> steps = stepDao.getByRecipe(row.getId()).stream()
                .map(RecipeStep::getInstruction)
                .collect(Collectors.toList());
        if (steps.isEmpty()) steps = List.of("No instructions provided.");

        String cuisineType = (row.getCategoryTags() != null && !row.getCategoryTags().isBlank())
                ? row.getCategoryTags()
                : "General";

        Instant createdAt = row.getCreatedAt() != null
                ? row.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
                : Instant.now();

        try {
            return new Recipe(
                    row.getId(),
                    row.getAuthorId(),
                    createdAt,
                    createdAt,
                    row.getTitle(),
                    row.getDescription() != null ? row.getDescription() : "",
                    row.getImageUrl() != null ? row.getImageUrl() : "",
                    row.getPrepTimeMin(),
                    row.getCookTimeMin(),
                    Math.max(1, row.getServings()),
                    row.isPublic(),
                    cuisineType,
                    ingredients,
                    steps);
        } catch (IllegalArgumentException e) {
            // Skip malformed rows rather than crashing the whole recommendation run
            return null;
        }
    }
}
