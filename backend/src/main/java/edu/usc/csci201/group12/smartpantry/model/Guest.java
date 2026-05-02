package edu.usc.csci201.group12.smartpantry.model;

import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeSummary;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Default free-tier account. Recipe access delegates to {@link RecipeRepository} so persistence can be swapped later.
 */  
public class Guest extends User {

    private final RecipeRepository recipeRepository;

    public Guest(String id, String username, String email, String passwordHash, RecipeRepository recipeRepository) {
        super(id, username, email, passwordHash);
        this.recipeRepository = Objects.requireNonNull(recipeRepository, "recipeRepository");
    }

    public Guest(String username, String email, String passwordHash, RecipeRepository recipeRepository) {
        super(username, email, passwordHash);
        this.recipeRepository = Objects.requireNonNull(recipeRepository, "recipeRepository");
    }

    /** Published recipes visible to guests (ordering defined by repository). */
    public List<RecipeSummary> browseRecipes() {
        return recipeRepository.listPublishedForGuest(this);
    }

    /** Case-insensitive substring match on ingredient names. */
    public List<RecipeSummary> searchByIngredient(String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return List.of();
        }
        String needle = ingredient.trim().toLowerCase(Locale.ROOT);
        return recipeRepository.findByIngredient(this, needle);
    }
}
