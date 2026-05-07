package edu.usc.csci201.group12.smartpantry.api.member;

import java.math.BigDecimal;

public record AddToPantryRequest(String ingredientId, String itemName, BigDecimal quantity, String unit, String expirationDate) {
}
