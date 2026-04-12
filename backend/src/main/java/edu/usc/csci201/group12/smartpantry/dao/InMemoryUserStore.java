package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryUserStore implements UserStore {
    private final Map<String, User> byId = new ConcurrentHashMap<>();
    private final Map<String, String> usernameKeyToId = new ConcurrentHashMap<>();
    private final Map<String, String> emailKeyToId = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        if (username == null) {
            return Optional.empty();
        }
        String id = usernameKeyToId.get(username.trim().toLowerCase());
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String id = emailKeyToId.get(email.trim().toLowerCase());
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByLoginIgnoreCase(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }
        String key = usernameOrEmail.trim().toLowerCase();
        Optional<User> byName = findByUsernameIgnoreCase(key);
        if (byName.isPresent()) {
            return byName;
        }
        return findByEmailIgnoreCase(key);
    }

    @Override
    public synchronized void save(User user) throws DuplicateUserException {
        String uKey = user.getUsername().toLowerCase();
        String eKey = user.getEmail().toLowerCase();
        if (usernameKeyToId.containsKey(uKey) && !usernameKeyToId.get(uKey).equals(user.getId())) {
            throw new DuplicateUserException("Username already taken");
        }
        if (emailKeyToId.containsKey(eKey) && !emailKeyToId.get(eKey).equals(user.getId())) {
            throw new DuplicateUserException("Email already registered");
        }
        byId.put(user.getId(), user);
        usernameKeyToId.put(uKey, user.getId());
        emailKeyToId.put(eKey, user.getId());
    }
}
