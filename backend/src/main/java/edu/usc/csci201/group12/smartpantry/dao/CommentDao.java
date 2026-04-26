// PORTED (zeqiang/database): Threaded comment operations for the COMMENTS table.
// Returns CommentRow (renamed from Zeqiang's Comment to avoid collision with model.content.Comment).
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentDao {

    public boolean addComment(CommentRow comment) {
        String sql = """
                INSERT INTO COMMENTS (id, recipe_id, user_id, parent_comment_id, body, created_at, is_deleted)
                VALUES (?, ?, ?, ?, ?, NOW(), 0)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (comment.getId() == null || comment.getId().isBlank()) {
                comment.setId(UUID.randomUUID().toString());
            }
            stmt.setString(1, comment.getId());
            stmt.setString(2, comment.getRecipeId());
            stmt.setString(3, comment.getUserId());
            if (comment.getParentCommentId() == null || comment.getParentCommentId().isBlank()) {
                stmt.setNull(4, Types.CHAR);
            } else {
                stmt.setString(4, comment.getParentCommentId());
            }
            stmt.setString(5, comment.getBody());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CommentRow getById(String id) {
        String sql = "SELECT * FROM COMMENTS WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CommentRow> getTopLevelByRecipe(String recipeId) {
        List<CommentRow> list = new ArrayList<>();
        String sql = """
                SELECT * FROM COMMENTS
                WHERE recipe_id = ? AND parent_comment_id IS NULL AND is_deleted = 0
                ORDER BY created_at ASC
                """;
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

    public List<CommentRow> getReplies(String parentCommentId) {
        List<CommentRow> list = new ArrayList<>();
        String sql = "SELECT * FROM COMMENTS WHERE parent_comment_id = ? AND is_deleted = 0 ORDER BY created_at ASC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, parentCommentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean softDelete(String id) {
        String sql = "UPDATE COMMENTS SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countByRecipe(String recipeId) {
        String sql = "SELECT COUNT(*) FROM COMMENTS WHERE recipe_id = ? AND is_deleted = 0";
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

    private CommentRow mapRow(ResultSet rs) throws SQLException {
        CommentRow c = new CommentRow();
        c.setId(rs.getString("id"));
        c.setRecipeId(rs.getString("recipe_id"));
        c.setUserId(rs.getString("user_id"));
        c.setParentCommentId(rs.getString("parent_comment_id"));
        c.setBody(rs.getString("body"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setDeleted(rs.getBoolean("is_deleted"));
        return c;
    }
}
