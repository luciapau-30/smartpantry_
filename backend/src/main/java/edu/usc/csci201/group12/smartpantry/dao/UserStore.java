package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.model.User;

import java.util.Optional;

/**
 * Account persistence. Swap this implementation for JDBC without changing servlets.
 */
public interface UserStore {
    Optional<User> findById(String id);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    /** Resolves login by username or email (case-insensitive). */
    Optional<User> findByLoginIgnoreCase(String usernameOrEmail);

    void save(User user) throws DuplicateUserException;

    final class DuplicateUserException extends Exception {
        public DuplicateUserException(String message) {
            super(message);
        }
    }
}
