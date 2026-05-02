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

    // Returns items enriched with ingredient name via JOIN — used by GetPantryServlet.
    public List<PantryItemRow> getByUserWithNames(String userId) {
        List<PantryItemRow> list = new ArrayList<>();
        String sql = """
                SELECT pi.*, i.name AS ingredient_name
                FROM PANTRY_ITEMS pi
                LEFT JOIN INGREDIENTS i ON pi.ingredient_id = i.id
                WHERE pi.user_id = ?
                ORDER BY pi.expiration_date ASC
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PantryItemRow row = mapRow(rs);
                    row.setIngredientName(rs.getString("ingredient_name"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
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

    // Used by background expiry thread — all users, not just one.
    public List<PantryItemRow> getAllExpiringSoon(int withinDays) {
        List<PantryItemRow> list = new ArrayList<>();
        String sql = """
                SELECT * FROM PANTRY_ITEMS
                WHERE expiration_date IS NOT NULL
                  AND expiration_date >= CURDATE()
                  AND expiration_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
                ORDER BY expiration_date ASC
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, withinDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // T3 background-thread query: JOINs with INGREDIENTS for human-readable alert payloads.
    public List<ExpiringPantryItem> getExpiringSoonWithName(String userId, int withinDays) {
        List<ExpiringPantryItem> list = new ArrayList<>();
        String sql = """
                SELECT p.id           AS pantry_item_id,
                       p.user_id      AS user_id,
                       p.ingredient_id AS ingredient_id,
                       i.name         AS ingredient_name,
                       p.quantity     AS quantity,
                       p.unit         AS unit,
                       p.expiration_date AS expiration_date
                FROM PANTRY_ITEMS p
                LEFT JOIN INGREDIENTS i ON i.id = p.ingredient_id
                WHERE p.user_id = ?
                  AND p.expiration_date IS NOT NULL
                  AND p.expiration_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
                ORDER BY p.expiration_date ASC
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setInt(2, withinDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExpiringPantryItem item = new ExpiringPantryItem();
                    item.setPantryItemId(rs.getString("pantry_item_id"));
                    item.setUserId(rs.getString("user_id"));
                    item.setIngredientId(rs.getString("ingredient_id"));
                    item.setIngredientName(rs.getString("ingredient_name"));
                    item.setQuantity(rs.getDouble("quantity"));
                    item.setUnit(rs.getString("unit"));
                    Date expDate = rs.getDate("expiration_date");
                    if (expDate != null) item.setExpirationDate(expDate.toLocalDate());
                    list.add(item);
                }
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
