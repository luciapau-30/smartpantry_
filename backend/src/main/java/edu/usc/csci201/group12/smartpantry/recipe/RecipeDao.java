// PORTED (zeqiang/database): CRUD for the RECIPES table.
// Returns RecipeRow (renamed from Zeqiang's Recipe to avoid collision with model.content.Recipe).
package edu.usc.csci201.group12.smartpantry.recipe;

import edu.usc.csci201.group12.smartpantry.dao.RecipeRow;
import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeDao {

    public boolean createRecipe(String authorId, String title, String description,
                                String imageUrl, String difficulty,
                                int prepTime, int cookTime, int servings,
                                boolean isPublic, String categoryTags) {
        String sql = """
                INSERT INTO RECIPES (id, author_id, title, description, image_url, difficulty,
                    prep_time_min, cook_time_min, servings, is_public, category_tags, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, authorId);
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setString(5, imageUrl);
            stmt.setString(6, difficulty);
            stmt.setInt(7, prepTime);
            stmt.setInt(8, cookTime);
            stmt.setInt(9, servings);
            stmt.setBoolean(10, isPublic);
            stmt.setString(11, categoryTags);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RecipeRow getById(String id) {
        String sql = "SELECT * FROM RECIPES WHERE id = ?";
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

    public List<RecipeRow> getByAuthor(String authorId) {
        List<RecipeRow> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPES WHERE author_id = ? ORDER BY created_at DESC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecipeRow> getPublicRecipes() {
        List<RecipeRow> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPES WHERE is_public = 1 ORDER BY created_at DESC";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecipeRow> searchByIngredientName(String keyword) {
        List<RecipeRow> list = new ArrayList<>();
        String sql = """
                SELECT DISTINCT r.* FROM RECIPES r
                JOIN RECIPE_INGREDIENTS ri ON r.id = ri.recipe_id
                JOIN INGREDIENTS i ON ri.ingredient_id = i.id
                WHERE r.is_public = 1 AND LOWER(i.name) LIKE LOWER(?)
                ORDER BY r.created_at DESC
                """;
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

    public boolean deleteRecipe(String id) {
        String sql = "DELETE FROM RECIPES WHERE id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RecipeRow mapRow(ResultSet rs) throws SQLException {
        RecipeRow r = new RecipeRow();
        r.setId(rs.getString("id"));
        r.setAuthorId(rs.getString("author_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setImageUrl(rs.getString("image_url"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setPrepTimeMin(rs.getInt("prep_time_min"));
        r.setCookTimeMin(rs.getInt("cook_time_min"));
        r.setServings(rs.getInt("servings"));
        r.setPublic(rs.getBoolean("is_public"));
        r.setCategoryTags(rs.getString("category_tags"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
