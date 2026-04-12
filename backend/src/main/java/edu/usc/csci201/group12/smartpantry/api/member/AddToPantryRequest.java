package edu.usc.csci201.group12.smartpantry.api.member;

import java.math.BigDecimal;

public record AddToPantryRequest(String ingredientId, BigDecimal quantity, String unit, String expirationDate) {
}
