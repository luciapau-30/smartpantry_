// PORTED (zeqiang/database): CRUD for the RECIPE_STEPS table.
package edu.usc.csci201.group12.smartpantry.dao;

import edu.usc.csci201.group12.smartpantry.jdbc.JdbcConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeStepDao {

    public boolean addStep(String recipeId, int stepNumber,
                           String instruction, Integer timerSeconds) {
        String sql = """
                INSERT INTO RECIPE_STEPS (id, recipe_id, step_number, instruction, timer_seconds)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, recipeId);
            stmt.setInt(3, stepNumber);
            stmt.setString(4, instruction);
            if (timerSeconds != null) stmt.setInt(5, timerSeconds);
            else stmt.setNull(5, Types.INTEGER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RecipeStep> getByRecipe(String recipeId) {
        List<RecipeStep> list = new ArrayList<>();
        String sql = "SELECT * FROM RECIPE_STEPS WHERE recipe_id = ? ORDER BY step_number ASC";
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

    public boolean deleteByRecipe(String recipeId) {
        String sql = "DELETE FROM RECIPE_STEPS WHERE recipe_id = ?";
        try (Connection conn = JdbcConnectionFactory.openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RecipeStep mapRow(ResultSet rs) throws SQLException {
        RecipeStep step = new RecipeStep();
        step.setId(rs.getString("id"));
        step.setRecipeId(rs.getString("recipe_id"));
        step.setStepNumber(rs.getInt("step_number"));
        step.setInstruction(rs.getString("instruction"));
        int timer = rs.getInt("timer_seconds");
        step.setTimerSeconds(rs.wasNull() ? null : timer);
        return step;
    }
}
