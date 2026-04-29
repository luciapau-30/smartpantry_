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

    // Inserts canonical ingredients on first run (INSERT IGNORE — safe to call repeatedly).
    public void seedIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM INGREDIENTS";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement count = conn.prepareStatement(countSql);
             ResultSet rs = count.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String[][] rows = {
            // id,              name,               category,  default_unit
            {"ing-001","tomatoes",          "produce","cup"},
            {"ing-002","garlic",            "produce","clove"},
            {"ing-003","onion",             "produce","piece"},
            {"ing-004","carrot",            "produce","piece"},
            {"ing-005","potatoes",          "produce","lb"},
            {"ing-006","spinach",           "produce","cup"},
            {"ing-007","lettuce",           "produce","cup"},
            {"ing-008","cucumber",          "produce","piece"},
            {"ing-009","bell pepper",       "produce","piece"},
            {"ing-010","broccoli",          "produce","cup"},
            {"ing-011","cauliflower",       "produce","cup"},
            {"ing-012","zucchini",          "produce","piece"},
            {"ing-013","mushrooms",         "produce","cup"},
            {"ing-014","corn",              "produce","cup"},
            {"ing-015","peas",              "produce","cup"},
            {"ing-016","green beans",       "produce","cup"},
            {"ing-017","celery",            "produce","stalk"},
            {"ing-018","lemon",             "produce","piece"},
            {"ing-019","lime",              "produce","piece"},
            {"ing-020","tomato paste",      "produce","tbsp"},
            {"ing-021","chicken",           "protein","lb"},
            {"ing-022","beef",              "protein","lb"},
            {"ing-023","pork",              "protein","lb"},
            {"ing-024","shrimp",            "protein","lb"},
            {"ing-025","salmon",            "protein","oz"},
            {"ing-026","tuna",              "protein","oz"},
            {"ing-027","turkey",            "protein","lb"},
            {"ing-028","bacon",             "protein","oz"},
            {"ing-029","sausage",           "protein","oz"},
            {"ing-030","egg",               "protein","piece"},
            {"ing-031","milk",              "dairy","cup"},
            {"ing-032","butter",            "dairy","tbsp"},
            {"ing-033","cheese",            "dairy","oz"},
            {"ing-034","heavy cream",       "dairy","cup"},
            {"ing-035","sour cream",        "dairy","cup"},
            {"ing-036","yogurt",            "dairy","cup"},
            {"ing-037","cream cheese",      "dairy","oz"},
            {"ing-038","rice",              "grains","cup"},
            {"ing-039","pasta",             "grains","oz"},
            {"ing-040","flour",             "grains","cup"},
            {"ing-041","oats",              "grains","cup"},
            {"ing-042","bread",             "grains","slice"},
            {"ing-043","tortilla",          "grains","piece"},
            {"ing-044","lentils",           "grains","cup"},
            {"ing-045","chickpeas",         "grains","cup"},
            {"ing-046","beans",             "grains","cup"},
            {"ing-047","olive oil",         "pantry","tbsp"},
            {"ing-048","vegetable oil",     "pantry","tbsp"},
            {"ing-049","chicken broth",     "pantry","cup"},
            {"ing-050","vegetable broth",   "pantry","cup"},
            {"ing-051","coconut milk",      "pantry","cup"},
            {"ing-052","soy sauce",         "pantry","tbsp"},
            {"ing-053","vinegar",           "pantry","tbsp"},
            {"ing-054","hot sauce",         "pantry","tsp"},
            {"ing-055","honey",             "pantry","tbsp"},
            {"ing-056","sugar",             "pantry","cup"},
            {"ing-057","brown sugar",       "pantry","cup"},
            {"ing-058","baking powder",     "pantry","tsp"},
            {"ing-059","baking soda",       "pantry","tsp"},
            {"ing-060","salt",              "spices","tsp"},
            {"ing-061","pepper",            "spices","tsp"},
            {"ing-062","cumin",             "spices","tsp"},
            {"ing-063","paprika",           "spices","tsp"},
            {"ing-064","chili powder",      "spices","tsp"},
            {"ing-065","oregano",           "spices","tsp"},
            {"ing-066","basil",             "spices","tsp"},
            {"ing-067","thyme",             "spices","tsp"},
            {"ing-068","cinnamon",          "spices","tsp"},
            {"ing-069","coriander",         "spices","tsp"},
            {"ing-070","turmeric",          "spices","tsp"},
            {"ing-071","ginger",            "spices","tsp"},
        };

        String sql = "INSERT IGNORE INTO INGREDIENTS (id, name, category, default_unit, image_url) VALUES (?,?,?,?,NULL)";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String[] row : rows) {
                stmt.setString(1, row[0]);
                stmt.setString(2, row[1]);
                stmt.setString(3, row[2]);
                stmt.setString(4, row[3]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
