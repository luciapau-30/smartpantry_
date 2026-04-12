package edu.usc.csci201.group12.smartpantry.model;

/**
 * Serializable user summary for JSON APIs (no secrets).
 */
public record UserPublicView(String id, String username, String email, String userType) {
}
