package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.model.User;

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
}
