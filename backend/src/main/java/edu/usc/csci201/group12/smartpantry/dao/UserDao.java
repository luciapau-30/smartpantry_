// PORTED (zeqiang/database): CRUD for the USERS table.
// Returns UserRow (package-private DTO) instead of Zeqiang's User POJO.
// JdbcUserStore maps UserRow → Archit's Member/Guest.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDao {

    private static final String SELECT_COLS = """
            SELECT id, username, email, password_hash, profile_picture_url, bio,
                   created_at, last_login, is_active, is_guest
            FROM USERS
            """;

    public boolean createUser(String username, String email, String passwordHash,
                              String profilePictureUrl, String bio) {
        String sql = """
                INSERT INTO USERS
                (id, username, email, password_hash, profile_picture_url, bio,
                 created_at, last_login, is_active, is_guest)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), NULL, 1, 0)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, passwordHash);
            stmt.setString(5, profilePictureUrl);
            stmt.setString(6, bio);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertUser(String id, String username, String email,
                              String passwordHash, boolean isGuest) {
        String sql = """
                INSERT INTO USERS (id, username, email, password_hash, created_at, is_active, is_guest)
                VALUES (?, ?, ?, ?, NOW(), 1, ?)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, passwordHash);
            stmt.setBoolean(5, isGuest);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserRow getUserById(String id) {
        String sql = SELECT_COLS + "WHERE id = ?";
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

    public UserRow getUserByUsername(String username) {
        String sql = SELECT_COLS + "WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserRow getUserByEmail(String email) {
        String sql = SELECT_COLS + "WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateLastLogin(String userId) {
        String sql = "UPDATE USERS SET last_login = NOW() WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM USERS WHERE LOWER(username) = LOWER(?) LIMIT 1";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM USERS WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserRow> getAllActiveUsers() {
        String sql = SELECT_COLS + "WHERE is_active = 1 ORDER BY created_at DESC";
        List<UserRow> users = new ArrayList<>();
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private UserRow mapRow(ResultSet rs) throws SQLException {
        UserRow row = new UserRow();
        row.id = rs.getString("id");
        row.username = rs.getString("username");
        row.email = rs.getString("email");
        row.passwordHash = rs.getString("password_hash");
        row.profilePictureUrl = rs.getString("profile_picture_url");
        row.bio = rs.getString("bio");
        row.createdAt = rs.getTimestamp("created_at");
        row.lastLogin = rs.getTimestamp("last_login");
        row.active = rs.getBoolean("is_active");
        row.guest = rs.getBoolean("is_guest");
        return row;
    }
}
