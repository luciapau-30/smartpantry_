// PORTED (zeqiang/database): CRUD for the RECIPE_INGREDIENTS table.
// Returns RecipeIngredientRow (renamed from Zeqiang's RecipeIngredient to avoid collision with model.content.Recipe.RecipeIngredient).
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeIngredientDao {

    public boolean addIngredientToRecipe(String recipeId, String ingredientId,
                                         double quantity, String unit,
                                         String notes, boolean isOptional) {
        String sql = """
                INSERT INTO RECIPE_INGREDIENTS (id, recipe_id, ingredient_id, quantity, unit, notes, is_optional)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, recipeId);
            stmt.setString(3, ingredientId);
            stmt.setDouble(4, quantity);
            stmt.setString(5, unit);
            stmt.setString(6, notes);
            stmt.setBoolean(7, isOptional);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RecipeIngredientRow> getByRecipe(String recipeId) {
        List<RecipeIngredientRow> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPE_INGREDIENTS WHERE recipe_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteByRecipe(String recipeId) {
        String sql = "DELETE FROM RECIPE_INGREDIENTS WHERE recipe_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RecipeIngredientRow mapRow(ResultSet rs) throws SQLException {
        RecipeIngredientRow ri = new RecipeIngredientRow();
        ri.setId(rs.getString("id"));
        ri.setRecipeId(rs.getString("recipe_id"));
        ri.setIngredientId(rs.getString("ingredient_id"));
        ri.setQuantity(rs.getDouble("quantity"));
        ri.setUnit(rs.getString("unit"));
        ri.setNotes(rs.getString("notes"));
        ri.setOptional(rs.getBoolean("is_optional"));
        return ri;
    }
}
