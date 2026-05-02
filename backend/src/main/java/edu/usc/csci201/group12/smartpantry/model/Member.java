package edu.usc.csci201.group12.smartpantry.model;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;
import edu.usc.csci201.group12.smartpantry.model.content.Comment;
import edu.usc.csci201.group12.smartpantry.model.content.Recipe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Member account: pantry and content mutations via JDBC (schema from DB team).
 * Extends Archit's {@link User} so {@link edu.usc.csci201.group12.smartpantry.dao.UserStore} and auth stay unified.
 */
public class Member extends User {

    private static final String INSERT_PANTRY_SQL = """
            INSERT INTO PANTRY_ITEMS (id, user_id, ingredient_id, quantity, unit, expiration_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERT_RECIPE_SQL = """
            INSERT INTO RECIPES (id, author_id, title, description, image_url, prep_time_min, cook_time_min, servings, is_public, category_tags, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERT_RECIPE_INGREDIENT_SQL = """
            INSERT INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String INSERT_RECIPE_STEP_SQL = """
            INSERT INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String INSERT_COMMENT_SQL = """
            INSERT INTO COMMENTS (id, recipe_id, user_id, parent_comment_id, body, created_at, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public Member(String id, String username, String email, String passwordHash) {
        super(id, username, email, passwordHash);
    }

    public Member(String username, String email, String passwordHash) {
        super(username, email, passwordHash);
    }

    public String addToPantry(String ingredientId, BigDecimal quantity, String unit, LocalDate expirationDate) throws SQLException {
        if (ingredientId == null || ingredientId.isBlank()) {
            throw new IllegalArgumentException("ingredientId cannot be blank");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("quantity must be non-negative");
        }

        String pantryItemId = UUID.randomUUID().toString();
        try (Connection connection = JdbcConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_PANTRY_SQL)) {
            statement.setString(1, pantryItemId);
            statement.setString(2, getId());
            statement.setString(3, ingredientId.trim());
            statement.setBigDecimal(4, quantity);
            statement.setString(5, unit == null ? "" : unit.trim());
            if (expirationDate == null) {
                statement.setDate(6, null);
            } else {
                statement.setDate(6, Date.valueOf(expirationDate));
            }
            statement.executeUpdate();
            return pantryItemId;
        }
    }

    public String uploadRecipe(Recipe recipe) throws SQLException {
        if (recipe == null) {
            throw new IllegalArgumentException("recipe cannot be null");
        }

        recipe.setAuthorId(getId());
        recipe.touch();
        if (recipe.getId() == null || recipe.getId().isBlank()) {
            recipe.setId(UUID.randomUUID().toString());
        }

        try (Connection connection = JdbcConnectionFactory.openConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                insertRecipe(connection, recipe);
                insertRecipeIngredients(connection, recipe);
                insertRecipeSteps(connection, recipe);
                connection.commit();
                return recipe.getId();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public String postComment(Comment comment) throws SQLException {
        if (comment == null) {
            throw new IllegalArgumentException("comment cannot be null");
        }

        comment.setAuthorId(getId());
        comment.touch();
        if (comment.getId() == null || comment.getId().isBlank()) {
            comment.setId(UUID.randomUUID().toString());
        }

        try (Connection connection = JdbcConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_COMMENT_SQL)) {
            statement.setString(1, comment.getId());
            statement.setString(2, comment.getRecipeId());
            statement.setString(3, getId());
            statement.setString(4, comment.getParentCommentId());
            statement.setString(5, comment.getTextBody());
            statement.setTimestamp(6, Timestamp.from(comment.getCreatedAt()));
            statement.setBoolean(7, comment.isDeleted());
            statement.executeUpdate();
            return comment.getId();
        }
    }

    private void insertRecipe(Connection connection, Recipe recipe) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RECIPE_SQL)) {
            statement.setString(1, recipe.getId());
            statement.setString(2, getId());
            statement.setString(3, recipe.getTitle());
            statement.setString(4, recipe.getDescription());
            statement.setString(5, recipe.getImageUrl());
            statement.setInt(6, recipe.getPrepTimeMinutes());
            statement.setInt(7, recipe.getCookTimeMinutes());
            statement.setInt(8, recipe.getServings());
            statement.setBoolean(9, recipe.isPublic());
            statement.setString(10, toCategoryTagsJson(recipe.getCuisineType()));
            statement.setTimestamp(11, Timestamp.from(recipe.getCreatedAt()));
            statement.executeUpdate();
        }
    }

    private void insertRecipeIngredients(Connection connection, Recipe recipe) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RECIPE_INGREDIENT_SQL)) {
            for (Recipe.RecipeIngredient ingredient : recipe.getIngredientsList()) {
                statement.setString(1, ingredient.getId());
                statement.setString(2, recipe.getId());
                statement.setString(3, ingredient.getIngredientId());
                statement.setBigDecimal(4, ingredient.getQuantity());
                statement.setString(5, ingredient.getUnit());
                statement.setString(6, ingredient.getNotes());
                statement.setBoolean(7, ingredient.isOptional());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertRecipeSteps(Connection connection, Recipe recipe) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_RECIPE_STEP_SQL)) {
            int stepNumber = 1;
            for (String instruction : recipe.getInstructions()) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, recipe.getId());
                statement.setInt(3, stepNumber);
                statement.setString(4, instruction);
                statement.setInt(5, 0);
                statement.addBatch();
                stepNumber++;
            }
            statement.executeBatch();
        }
    }

    private String toCategoryTagsJson(String cuisineType) {
        String escapedValue = cuisineType.replace("\\", "\\\\").replace("\"", "\\\"");
        return "[\"" + escapedValue + "\"]";
    }
}
