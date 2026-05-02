package edu.usc.csci201.group12.smartpantry.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

/**
 * Converts ingredient quantities to a common base unit so they can be compared numerically.
 *
 * <ul>
 *   <li>Volume family → base unit: <strong>millilitres (ml)</strong></li>
 *   <li>Weight family → base unit: <strong>grams (g)</strong></li>
 *   <li>Count family  → base unit: <strong>each (1.0 per item)</strong></li>
 * </ul>
 */
public final class UnitNormalizer {

    public enum UnitFamily { VOLUME, WEIGHT, COUNT, UNKNOWN }

    // ── volume conversions → ml ────────────────────────────────────────────
    private static final Map<String, BigDecimal> TO_ML = Map.ofEntries(
            Map.entry("ml",              BigDecimal.ONE),
            Map.entry("milliliter",      BigDecimal.ONE),
            Map.entry("milliliters",     BigDecimal.ONE),
            Map.entry("millilitre",      BigDecimal.ONE),
            Map.entry("millilitres",     BigDecimal.ONE),
            Map.entry("l",               new BigDecimal("1000")),
            Map.entry("liter",           new BigDecimal("1000")),
            Map.entry("liters",          new BigDecimal("1000")),
            Map.entry("litre",           new BigDecimal("1000")),
            Map.entry("litres",          new BigDecimal("1000")),
            Map.entry("cup",             new BigDecimal("240")),
            Map.entry("cups",            new BigDecimal("240")),
            Map.entry("c",               new BigDecimal("240")),
            Map.entry("tbsp",            new BigDecimal("15")),
            Map.entry("tablespoon",      new BigDecimal("15")),
            Map.entry("tablespoons",     new BigDecimal("15")),
            Map.entry("tsp",             new BigDecimal("5")),
            Map.entry("teaspoon",        new BigDecimal("5")),
            Map.entry("teaspoons",       new BigDecimal("5")),
            Map.entry("fl oz",           new BigDecimal("30")),
            Map.entry("fluid ounce",     new BigDecimal("30")),
            Map.entry("fluid ounces",    new BigDecimal("30"))
    );

    // ── weight conversions → grams ─────────────────────────────────────────
    private static final Map<String, BigDecimal> TO_GRAMS = Map.ofEntries(
            Map.entry("g",        BigDecimal.ONE),
            Map.entry("gram",     BigDecimal.ONE),
            Map.entry("grams",    BigDecimal.ONE),
            Map.entry("kg",       new BigDecimal("1000")),
            Map.entry("kilogram", new BigDecimal("1000")),
            Map.entry("kilograms",new BigDecimal("1000")),
            Map.entry("oz",       new BigDecimal("28.3495")),
            Map.entry("ounce",    new BigDecimal("28.3495")),
            Map.entry("ounces",   new BigDecimal("28.3495")),
            Map.entry("lb",       new BigDecimal("453.592")),
            Map.entry("lbs",      new BigDecimal("453.592")),
            Map.entry("pound",    new BigDecimal("453.592")),
            Map.entry("pounds",   new BigDecimal("453.592"))
    );

    // ── count units (all convert 1:1 to "each") ────────────────────────────
    private static final java.util.Set<String> COUNT_UNITS = java.util.Set.of(
            "", "each", "whole", "item", "items", "piece", "pieces",
            "clove", "cloves", "head", "heads", "bunch", "bunches",
            "can", "cans", "package", "packages", "pkg", "slice", "slices"
    );

    /**
     * Converts {@code quantity} in the given {@code unit} to the base unit for its family.
     * Returns {@code null} if the unit is unknown (UNKNOWN family).
     */
    public BigDecimal toBase(BigDecimal quantity, String unit) {
        if (quantity == null) return null;
        String key = normalizeUnit(unit);
        BigDecimal factor = TO_ML.get(key);
        if (factor != null) return quantity.multiply(factor).setScale(4, RoundingMode.HALF_UP);
        factor = TO_GRAMS.get(key);
        if (factor != null) return quantity.multiply(factor).setScale(4, RoundingMode.HALF_UP);
        if (COUNT_UNITS.contains(key)) return quantity.setScale(4, RoundingMode.HALF_UP);
        return null;
    }

    /** Returns the measurement family for the given unit string. */
    public UnitFamily familyOf(String unit) {
        String key = normalizeUnit(unit);
        if (TO_ML.containsKey(key))    return UnitFamily.VOLUME;
        if (TO_GRAMS.containsKey(key)) return UnitFamily.WEIGHT;
        if (COUNT_UNITS.contains(key)) return UnitFamily.COUNT;
        return UnitFamily.UNKNOWN;
    }

    /**
     * Returns true if unitA and unitB belong to the same measurable family,
     * meaning quantities can be compared after conversion.
     */
    public boolean compatible(String unitA, String unitB) {
        UnitFamily a = familyOf(unitA);
        UnitFamily b = familyOf(unitB);
        return a != UnitFamily.UNKNOWN && a == b;
    }

    private static String normalizeUnit(String unit) {
        if (unit == null) return "";
        return unit.trim().toLowerCase(Locale.ROOT);
    }
}
