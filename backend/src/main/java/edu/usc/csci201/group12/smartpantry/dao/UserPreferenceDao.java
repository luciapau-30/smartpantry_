package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores preferred cuisines per user in USER_PREFERENCES table.
 * Table is created on first use (CREATE TABLE IF NOT EXISTS).
 */
public class UserPreferenceDao {

    private static final String CREATE_SQL = """
            CREATE TABLE IF NOT EXISTS USER_PREFERENCES (
                user_id VARCHAR(36) NOT NULL,
                cuisine VARCHAR(100) NOT NULL,
                PRIMARY KEY (user_id, cuisine)
            )
            """;

    public void ensureTable() {
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(CREATE_SQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPreferences(String userId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT cuisine FROM USER_PREFERENCES WHERE user_id = ? ORDER BY cuisine";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(rs.getString("cuisine"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void setPreferences(String userId, List<String> cuisines) {
        String deleteSql = "DELETE FROM USER_PREFERENCES WHERE user_id = ?";
        String insertSql = "INSERT IGNORE INTO USER_PREFERENCES (user_id, cuisine) VALUES (?, ?)";
        try (Connection conn = JdbcConnectionFactory.openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setString(1, userId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                for (String cuisine : cuisines) {
                    if (cuisine != null && !cuisine.isBlank()) {
                        ins.setString(1, userId);
                        ins.setString(2, cuisine.trim());
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
