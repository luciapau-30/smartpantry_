// PORTED (zeqiang/database): Save/unsave operations for the RECIPE_SAVES table.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeSaveDao {

    public boolean saveRecipe(String recipeId, String userId) {
        if (isRecipeSaved(recipeId, userId)) return true;
        String sql = "INSERT INTO RECIPE_SAVES (id, recipe_id, user_id, saved_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, recipeId);
            stmt.setString(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unsaveRecipe(String recipeId, String userId) {
        String sql = "DELETE FROM RECIPE_SAVES WHERE recipe_id = ? AND user_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            stmt.setString(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRecipeSaved(String recipeId, String userId) {
        String sql = "SELECT 1 FROM RECIPE_SAVES WHERE recipe_id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            stmt.setString(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getSavedRecipeIdsByUser(String userId) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT recipe_id FROM RECIPE_SAVES WHERE user_id = ? ORDER BY saved_at DESC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("recipe_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public int countSaves(String recipeId) {
        String sql = "SELECT COUNT(*) FROM RECIPE_SAVES WHERE recipe_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
