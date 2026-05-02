// PORTED (zeqiang/database): Like/dislike operations for the RECIPE_LIKES table.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeLikeDao {

    public boolean upsertReaction(String recipeId, String userId, boolean isLike) {
        String checkSql = "SELECT id FROM RECIPE_LIKES WHERE recipe_id = ? AND user_id = ?";
        String insertSql = """
                INSERT INTO RECIPE_LIKES (id, recipe_id, user_id, is_like, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """;
        String updateSql = "UPDATE RECIPE_LIKES SET is_like = ?, created_at = NOW() WHERE recipe_id = ? AND user_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection()) {
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, recipeId);
                check.setString(2, userId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                            update.setBoolean(1, isLike);
                            update.setString(2, recipeId);
                            update.setString(3, userId);
                            return update.executeUpdate() > 0;
                        }
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setString(1, UUID.randomUUID().toString());
                insert.setString(2, recipeId);
                insert.setString(3, userId);
                insert.setBoolean(4, isLike);
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReaction(String recipeId, String userId) {
        String sql = "DELETE FROM RECIPE_LIKES WHERE recipe_id = ? AND user_id = ?";
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

    /** Returns true = liked, false = disliked, null = no reaction. */
    public Boolean getUserReaction(String recipeId, String userId) {
        String sql = "SELECT is_like FROM RECIPE_LIKES WHERE recipe_id = ? AND user_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            stmt.setString(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("is_like");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countLikes(String recipeId) {
        return countReactions(recipeId, true);
    }

    public int countDislikes(String recipeId) {
        return countReactions(recipeId, false);
    }

    private int countReactions(String recipeId, boolean isLike) {
        String sql = "SELECT COUNT(*) FROM RECIPE_LIKES WHERE recipe_id = ? AND is_like = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            stmt.setBoolean(2, isLike);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Step 1 of two-step trending: returns recipe IDs most liked in the last 24 hours.
    public List<String> getTrendingRecipeIds(int limit) {
        return getTopIds("WHERE is_like = 1 AND created_at > NOW() - INTERVAL 24 HOUR", limit);
    }

    // Step 1 of two-step top: returns recipe IDs with the most all-time likes.
    public List<String> getTopLikedRecipeIds(int limit) {
        return getTopIds("WHERE is_like = 1", limit);
    }

    private List<String> getTopIds(String whereClause, int limit) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT recipe_id FROM RECIPE_LIKES " + whereClause
                + " GROUP BY recipe_id ORDER BY COUNT(*) DESC LIMIT ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("recipe_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
