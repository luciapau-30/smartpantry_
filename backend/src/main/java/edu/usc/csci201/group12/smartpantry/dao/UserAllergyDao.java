// PORTED (zeqiang/database): CRUD for the USER_ALLERGIES table.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAllergyDao {

    public boolean addAllergy(String userId, String allergenName, String severity) {
        String sql = """
                INSERT INTO USER_ALLERGIES (id, user_id, allergen_name, severity, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, userId);
            stmt.setString(3, allergenName);
            stmt.setString(4, severity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserAllergy> getByUser(String userId) {
        List<UserAllergy> list = new ArrayList<>();
        String sql = "SELECT * FROM USER_ALLERGIES WHERE user_id = ? ORDER BY allergen_name ASC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteAllergy(String id) {
        String sql = "DELETE FROM USER_ALLERGIES WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private UserAllergy mapRow(ResultSet rs) throws SQLException {
        UserAllergy a = new UserAllergy();
        a.setId(rs.getString("id"));
        a.setUserId(rs.getString("user_id"));
        a.setAllergenName(rs.getString("allergen_name"));
        a.setSeverity(rs.getString("severity"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
