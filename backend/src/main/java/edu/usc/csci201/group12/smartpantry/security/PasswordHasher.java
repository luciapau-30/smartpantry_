package edu.usc.csci201.group12.smartpantry.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password hashing for secure login storage. Uses bcrypt (adaptive one-way hash), not reversible encryption.
 */
public final class PasswordHasher {
    private static final int COST = 12;

    public PasswordHasher() {
    }

    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    public boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash);
        return result.verified;
    }
}
