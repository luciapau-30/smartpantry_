// PORTED (zeqiang/database): CRUD for the INGREDIENTS table.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngredientDao {

    public boolean createIngredient(String name, String category,
                                    String defaultUnit, String imageUrl) {
        String sql = """
                INSERT INTO INGREDIENTS (id, name, category, default_unit, image_url)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, defaultUnit);
            stmt.setString(5, imageUrl);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Ingredient getById(String id) {
        String sql = "SELECT * FROM INGREDIENTS WHERE id = ?";
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

    public Ingredient getByName(String name) {
        String sql = "SELECT * FROM INGREDIENTS WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Ingredient> searchByName(String keyword) {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM INGREDIENTS WHERE LOWER(name) LIKE LOWER(?) ORDER BY name ASC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Ingredient> getAll() {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM INGREDIENTS ORDER BY name ASC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Ingredient mapRow(ResultSet rs) throws SQLException {
        return new Ingredient(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("default_unit"),
                rs.getString("image_url"));
    }
}
