// PORTED (zeqiang/database): CRUD for the PANTRY_ITEMS table.
// Returns PantryItemRow (renamed from Zeqiang's PantryItem to avoid collision with recommendation.PantryItem).
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PantryItemDao {

    public boolean addPantryItem(String userId, String ingredientId,
                                 double quantity, String unit, LocalDate expirationDate) {
        String sql = """
                INSERT INTO PANTRY_ITEMS (id, user_id, ingredient_id, quantity, unit, expiration_date, added_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, userId);
            stmt.setString(3, ingredientId);
            stmt.setDouble(4, quantity);
            stmt.setString(5, unit);
            stmt.setDate(6, expirationDate != null ? Date.valueOf(expirationDate) : null);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PantryItemRow getById(String id) {
        String sql = "SELECT * FROM PANTRY_ITEMS WHERE id = ?";
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

    public List<PantryItemRow> getByUser(String userId) {
        List<PantryItemRow> list = new ArrayList<>();
        String sql = "SELECT * FROM PANTRY_ITEMS WHERE user_id = ? ORDER BY expiration_date ASC";
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

    public List<PantryItemRow> getExpiringSoon(String userId, int withinDays) {
        List<PantryItemRow> list = new ArrayList<>();
        String sql = """
                SELECT * FROM PANTRY_ITEMS
                WHERE user_id = ?
                  AND expiration_date IS NOT NULL
                  AND expiration_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
                ORDER BY expiration_date ASC
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setInt(2, withinDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateQuantity(String id, double quantity) {
        String sql = "UPDATE PANTRY_ITEMS SET quantity = ? WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantity);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItem(String id) {
        String sql = "DELETE FROM PANTRY_ITEMS WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean itemExistsForUser(String userId, String ingredientId) {
        String sql = "SELECT 1 FROM PANTRY_ITEMS WHERE user_id = ? AND ingredient_id = ? LIMIT 1";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private PantryItemRow mapRow(ResultSet rs) throws SQLException {
        PantryItemRow item = new PantryItemRow();
        item.setId(rs.getString("id"));
        item.setUserId(rs.getString("user_id"));
        item.setIngredientId(rs.getString("ingredient_id"));
        item.setQuantity(rs.getDouble("quantity"));
        item.setUnit(rs.getString("unit"));
        Date expDate = rs.getDate("expiration_date");
        if (expDate != null) item.setExpirationDate(expDate.toLocalDate());
        Timestamp ts = rs.getTimestamp("added_at");
        if (ts != null) item.setAddedAt(ts.toLocalDateTime());
        return item;
    }
}
