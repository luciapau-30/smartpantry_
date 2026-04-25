package edu.usc.csci201.group12.smartpantry.model;

import edu.usc.csci201.group12.smartpantry.security.PasswordHasher;

import java.util.Objects;
import java.util.UUID;

/**
 * Core account record: identity plus bcrypt hash. Subclasses add role-specific behavior.
 */
public abstract class User {
    private final String id;
    private String username;
    private String email;
    private String passwordHash;

    protected User(String id, String username, String email, String passwordHash) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    }

    protected User(String username, String email, String passwordHash) {
        this(UUID.randomUUID().toString(), username, email, passwordHash);
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    /** bcrypt hash only — never send to clients */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setUsername(String username) {
        this.username = requireText(username, "username");
    }

    public void setEmail(String email) {
        this.email = requireText(email, "email");
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    }

    public boolean verifyPassword(String plainPassword, PasswordHasher hasher) {
        return hasher.verify(plainPassword, passwordHash);
    }

    /** Safe DTO for JSON responses */
    public UserPublicView toPublicView() {
        return new UserPublicView(id, username, email, getClass().getSimpleName());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
