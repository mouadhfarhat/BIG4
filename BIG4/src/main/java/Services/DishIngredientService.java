package Services;

import Entities.DishIngredient;
import Utils.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DishIngredientService {

    private final Connection cnx;

    public DishIngredientService() {
        try {
            cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain database connection", e);
        }
    }

    public void ensureDishIngredientTableExists() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS dish_ingredient (
                    dish_id INT NOT NULL,
                    ingredient_id INT NOT NULL,
                    quantity_required DOUBLE NOT NULL,
                    PRIMARY KEY (dish_id, ingredient_id),
                    CONSTRAINT fk_dish_ingredient_dish
                        FOREIGN KEY (dish_id) REFERENCES dish(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_dish_ingredient_ingredient
                        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
                        ON DELETE CASCADE
                )
                """;
        try (Statement statement = cnx.createStatement()) {
            statement.execute(sql);
        }
    }

    public void upsertDishIngredient(DishIngredient recipeLine) throws SQLException {
        String sql = """
                INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity_required = VALUES(quantity_required)
                """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recipeLine.getDishId());
            ps.setLong(2, recipeLine.getIngredientId());
            ps.setDouble(3, recipeLine.getQuantityRequired());
            ps.executeUpdate();
        }
    }

    public void replaceDishRecipe(int dishId, List<DishIngredient> recipe) throws SQLException {
        String deleteSql = "DELETE FROM dish_ingredient WHERE dish_id = ?";
        try (PreparedStatement deletePs = cnx.prepareStatement(deleteSql)) {
            deletePs.setInt(1, dishId);
            deletePs.executeUpdate();
        }

        if (recipe == null || recipe.isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required) VALUES (?, ?, ?)";
        try (PreparedStatement insertPs = cnx.prepareStatement(insertSql)) {
            for (DishIngredient line : recipe) {
                insertPs.setInt(1, dishId);
                insertPs.setLong(2, line.getIngredientId());
                insertPs.setDouble(3, line.getQuantityRequired());
                insertPs.addBatch();
            }
            insertPs.executeBatch();
        }
    }

    public List<DishIngredient> getByDishId(int dishId) throws SQLException {
        String sql = "SELECT dish_id, ingredient_id, quantity_required FROM dish_ingredient WHERE dish_id = ?";
        List<DishIngredient> lines = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(map(rs));
                }
            }
        }
        return lines;
    }

    public List<DishIngredient> getByIngredientId(long ingredientId) throws SQLException {
        String sql = "SELECT dish_id, ingredient_id, quantity_required FROM dish_ingredient WHERE ingredient_id = ?";
        List<DishIngredient> lines = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(map(rs));
                }
            }
        }
        return lines;
    }

    public List<DishIngredient> getAll() throws SQLException {
        String sql = "SELECT dish_id, ingredient_id, quantity_required FROM dish_ingredient";
        List<DishIngredient> lines = new ArrayList<>();
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lines.add(map(rs));
            }
        }
        return lines;
    }

    public void delete(int dishId, long ingredientId) throws SQLException {
        String sql = "DELETE FROM dish_ingredient WHERE dish_id = ? AND ingredient_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ps.setLong(2, ingredientId);
            ps.executeUpdate();
        }
    }

    private DishIngredient map(ResultSet rs) throws SQLException {
        return new DishIngredient(
                rs.getInt("dish_id"),
                rs.getLong("ingredient_id"),
                rs.getDouble("quantity_required")
        );
    }
}
