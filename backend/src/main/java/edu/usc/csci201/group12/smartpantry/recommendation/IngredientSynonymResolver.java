package edu.usc.csci201.group12.smartpantry.recommendation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Resolves whether two ingredient name strings refer to the same ingredient.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Normalize both strings (lowercase, strip common adjectives like "fresh/dried/chopped").</li>
 *   <li>Map each normalized string to its canonical name via synonyms.json.</li>
 *   <li>If canonicals are equal → match.</li>
 * </ol>
 *
 * <p>synonyms.json format: { "canonical": ["variant1", "variant2", ...] }
 */
public final class IngredientSynonymResolver {

    /** Words that appear before ingredient names but don't change the ingredient identity. */
    private static final Pattern MODIFIER_PATTERN = Pattern.compile(
            "\\b(fresh|dried|frozen|canned|chopped|sliced|diced|minced|grated|peeled|" +
            "crushed|ground|cooked|raw|roasted|smoked|organic|large|small|medium|ripe|" +
            "whole|halved|quartered)\\b",
            Pattern.CASE_INSENSITIVE);

    /** variant (normalized) → canonical name */
    private final Map<String, String> variantToCanonical;

    private IngredientSynonymResolver(Map<String, String> variantToCanonical) {
        this.variantToCanonical = variantToCanonical;
    }

    /**
     * Loads synonym mappings from {@code synonyms.json} on the classpath.
     * Falls back to an empty resolver (only exact-match after normalization) if the file is missing.
     */
    public static IngredientSynonymResolver loadFromClasspath() {
        try (var stream = IngredientSynonymResolver.class
                .getClassLoader()
                .getResourceAsStream("synonyms.json")) {

            if (stream == null) {
                return new IngredientSynonymResolver(Map.of());
            }

            Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> raw = new Gson().fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8), mapType);

            Map<String, String> reverse = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
                String canonical = entry.getKey().trim().toLowerCase(Locale.ROOT);
                reverse.put(canonical, canonical);
                for (String variant : entry.getValue()) {
                    reverse.put(normalize(variant), canonical);
                }
            }
            return new IngredientSynonymResolver(reverse);

        } catch (Exception e) {
            return new IngredientSynonymResolver(Map.of());
        }
    }

    /**
     * Returns true if {@code nameA} and {@code nameB} refer to the same ingredient
     * (same canonical name after normalization and synonym lookup).
     */
    public boolean matches(String nameA, String nameB) {
        if (nameA == null || nameB == null) return false;
        return canonical(nameA).equals(canonical(nameB));
    }

    /**
     * Returns the canonical ingredient name for a given raw name string.
     * If no synonym entry is found, returns the normalized string itself.
     */
    public String canonical(String name) {
        if (name == null) return "";
        String norm = normalize(name);
        return variantToCanonical.getOrDefault(norm, norm);
    }

    private static String normalize(String name) {
        if (name == null) return "";
        String lower = name.trim().toLowerCase(Locale.ROOT);
        // Strip quantity-adjective modifiers
        String stripped = MODIFIER_PATTERN.matcher(lower).replaceAll("").trim();
        // Collapse extra whitespace
        return stripped.replaceAll("\\s+", " ").trim();
    }
}
