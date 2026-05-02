package edu.usc.csci201.group12.smartpantry.recommendation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only view of one row from PANTRY_ITEMS, used by the recommendation engine.
 * ingredientName may be null if the DB join does not include it.
 */
public record PantryItem(
        String id,
        String ingredientId,
        String ingredientName,
        BigDecimal quantity,
        String unit,
        LocalDate expirationDate
) {}
