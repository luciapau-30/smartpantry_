package edu.usc.csci201.group12.smartpantry.recipe;

import java.util.List;

/** Lightweight recipe row for browse/search responses. */
public record RecipeSummary(String id, String name, String description, List<String> ingredients,
                            int prepTimeMin, int cookTimeMin, String categoryTags) {
}
