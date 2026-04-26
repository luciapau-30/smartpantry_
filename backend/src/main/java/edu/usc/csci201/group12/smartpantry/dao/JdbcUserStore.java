// NEW: Implements UserStore using UserDao (Zeqiang's ported DAO).
// Maps UserRow → Archit's Member (registered users) or Guest (guest accounts).
// SmartPantryBootstrapListener wires this in when PANTRY_DB_URL is set.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.model.Guest;
import edu.usc.csci201.group12.smartpantry.model.Member;
import edu.usc.csci201.group12.smartpantry.model.User;
import edu.usc.csci201.group12.smartpantry.recipe.RecipeRepository;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public class JdbcUserStore implements UserStore {

    private final UserDao userDao;
    private final RecipeRepository recipeRepository;

    public JdbcUserStore(UserDao userDao, RecipeRepository recipeRepository) {
        this.userDao = userDao;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userDao.getUserById(id)).map(this::toUser);
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(userDao.getUserByUsername(username)).map(this::toUser);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(userDao.getUserByEmail(email)).map(this::toUser);
    }

    @Override
    public Optional<User> findByLoginIgnoreCase(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) return Optional.empty();
        Optional<User> byUsername = findByUsernameIgnoreCase(usernameOrEmail.trim());
        return byUsername.isPresent() ? byUsername : findByEmailIgnoreCase(usernameOrEmail.trim());
    }

    @Override
    public void save(User user) throws DuplicateUserException {
        if (userDao.usernameExists(user.getUsername())) {
            throw new DuplicateUserException("Username already taken");
        }
        if (userDao.emailExists(user.getEmail())) {
            throw new DuplicateUserException("Email already registered");
        }
        boolean ok = userDao.insertUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user instanceof Guest);
        if (!ok) {
            throw new RuntimeException("Failed to persist user " + user.getId());
        }
    }

    // Maps a DB row to Archit's domain type.
    // Guests get a placeholder email if the DB has NULL (guests don't always supply one).
    private User toUser(UserRow row) {
        if (row.guest) {
            String email = (row.email != null && !row.email.isBlank())
                    ? row.email
                    : row.id + "@guest.invalid";
            String hash = (row.passwordHash != null) ? row.passwordHash : "";
            return new Guest(row.id, row.username, email, hash, recipeRepository);
        }
        return new Member(row.id, row.username, row.email, row.passwordHash);
    }
}
