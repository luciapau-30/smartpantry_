package edu.usc.csci201.group12.smartpantry.recommendation;

import edu.usc.csci201.group12.smartpantry.model.content.Recipe.RecipeIngredient;

import java.math.BigDecimal;
import java.util.List;

/**
 * Checks how well a single recipe ingredient requirement is covered by the user's pantry.
 *
 * <p>Matching strategy (in order):
 * <ol>
 *   <li>Exact {@code ingredientId} match (fastest, always consistent).</li>
 *   <li>Synonym/name match via {@link IngredientSynonymResolver}.</li>
 * </ol>
 *
 * <p>Once an ingredient is found, quantities are compared after normalising both to
 * their base unit via {@link UnitNormalizer}.  If units are incompatible (e.g. cups
 * vs grams) the ingredient is considered present but the quantity ratio is 0.5
 * (can't verify, assume partial).
 */
public final class PantryMatcher {

    /**
     * Result of checking one recipe ingredient against the pantry.
     *
     * @param ingredientFound  true when at least one pantry item matches the ingredient
     * @param quantityRatio    how much of the required quantity is available [0.0 … 1.0];
     *                         1.0 means fully covered, 0.0 means nothing found
     */
    public record CoverageResult(boolean ingredientFound, double quantityRatio) {
        public static final CoverageResult NOT_FOUND = new CoverageResult(false, 0.0);
    }

    private final IngredientSynonymResolver synonymResolver;
    private final UnitNormalizer unitNormalizer;

    public PantryMatcher(IngredientSynonymResolver synonymResolver, UnitNormalizer unitNormalizer) {
        this.synonymResolver = synonymResolver;
        this.unitNormalizer = unitNormalizer;
    }

    /**
     * Checks coverage of {@code required} against the given {@code pantry}.
     */
    public CoverageResult check(RecipeIngredient required, List<PantryItem> pantry) {
        if (pantry == null || pantry.isEmpty()) return CoverageResult.NOT_FOUND;

        PantryItem match = findMatch(required, pantry);
        if (match == null) return CoverageResult.NOT_FOUND;

        return computeRatio(required, match);
    }

    private PantryItem findMatch(RecipeIngredient required, List<PantryItem> pantry) {
        // 1. Exact ID match
        for (PantryItem item : pantry) {
            if (required.getIngredientId().equals(item.ingredientId())) {
                return item;
            }
        }
        // 2. Synonym name match
        String reqName = required.getIngredientName();
        if (reqName != null && !reqName.isBlank()) {
            for (PantryItem item : pantry) {
                String pantryName = item.ingredientName() != null
                        ? item.ingredientName()
                        : item.ingredientId(); // fall back to ID as name hint
                if (synonymResolver.matches(reqName, pantryName)) {
                    return item;
                }
            }
        }
        return null;
    }

    private CoverageResult computeRatio(RecipeIngredient required, PantryItem pantryItem) {
        BigDecimal requiredQty = required.getQuantity();
        // If no specific quantity required, presence alone is enough
        if (requiredQty == null || requiredQty.compareTo(BigDecimal.ZERO) == 0) {
            return new CoverageResult(true, 1.0);
        }

        // Check unit compatibility
        if (!unitNormalizer.compatible(required.getUnit(), pantryItem.unit())) {
            // Units in different families — ingredient is present but we can't verify amount
            return new CoverageResult(true, 0.5);
        }

        BigDecimal requiredBase = unitNormalizer.toBase(requiredQty, required.getUnit());
        BigDecimal pantryBase   = unitNormalizer.toBase(pantryItem.quantity(), pantryItem.unit());

        if (requiredBase == null || pantryBase == null || requiredBase.compareTo(BigDecimal.ZERO) == 0) {
            return new CoverageResult(true, 1.0);
        }

        double ratio = pantryBase.doubleValue() / requiredBase.doubleValue();
        return new CoverageResult(true, Math.min(ratio, 1.0));
    }
}
