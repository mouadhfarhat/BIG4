package Services;

import Entities.Dish;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishService {

    private final IngredientWasteAutomationService ingredientWasteAutomationService = new IngredientWasteAutomationService();

    public List<Dish> getAll() {
        String sql = "SELECT id, menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at " +
                "FROM dish ORDER BY id DESC";

        List<Dish> dishes = new ArrayList<>();

        ingredientWasteAutomationService.recordExpiredIngredientWaste();

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Dish d = new Dish();
                d.setId(rs.getInt("id"));
                d.setMenu_id(rs.getInt("menu_id"));
                d.setName(rs.getString("name"));
                d.setDescription(rs.getString("description"));
                d.setBase_price(rs.getFloat("base_price"));
                d.setAvailable(rs.getBoolean("available"));
                d.setStock_quantity(rs.getInt("stock_quantity"));
                d.setImage_url(rs.getString("image_url"));
                d.setCreated_at(rs.getTimestamp("created_at"));
                d.setUpdate_at(rs.getTimestamp("updated_at"));
                applyIngredientAvailability(cnx, d);
                dishes.add(d);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dishes;
    }

    // ✅ used by admin: getByMenuId(...)
    public List<Dish> getByMenuId(int menuId) {
        String sql = "SELECT id, menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at " +
                "FROM dish WHERE menu_id=? ORDER BY id DESC";

        List<Dish> dishes = new ArrayList<>();

        ingredientWasteAutomationService.recordExpiredIngredientWaste();

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, menuId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dish d = new Dish();
                    d.setId(rs.getInt("id"));
                    d.setMenu_id(rs.getInt("menu_id"));
                    d.setName(rs.getString("name"));
                    d.setDescription(rs.getString("description"));
                    d.setBase_price(rs.getFloat("base_price"));
                    d.setAvailable(rs.getBoolean("available"));
                    d.setStock_quantity(rs.getInt("stock_quantity"));
                    d.setImage_url(rs.getString("image_url"));
                    d.setCreated_at(rs.getTimestamp("created_at"));
                    d.setUpdate_at(rs.getTimestamp("updated_at"));
                    applyIngredientAvailability(cnx, d);
                    dishes.add(d);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dishes;
    }

    // ✅ used by admin: add(...)
    public void add(Dish d) {
        addAndReturnId(d);
    }

    public int addAndReturnId(Dish d) {
        String sql = "INSERT INTO dish(menu_id, name, description, base_price, available, stock_quantity, image_url) " +
                "VALUES(?,?,?,?,?,?,?)";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getMenu_id());
            ps.setString(2, d.getName());
            ps.setString(3, d.getDescription());
            ps.setFloat(4, d.getBase_price());
            ps.setBoolean(5, d.getAvailable() != null && d.getAvailable());
            ps.setInt(6, d.getStock_quantity());
            ps.setString(7, d.getImage_url());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    d.setId(id);
                    return id;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // ✅ used by admin: update(...)
    public void update(Dish d) {
        String sql = "UPDATE dish SET menu_id=?, name=?, description=?, base_price=?, available=?, stock_quantity=?, image_url=? " +
                "WHERE id=?";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, d.getMenu_id());
            ps.setString(2, d.getName());
            ps.setString(3, d.getDescription());
            ps.setFloat(4, d.getBase_price());
            ps.setBoolean(5, d.getAvailable() != null && d.getAvailable());
            ps.setInt(6, d.getStock_quantity());
            ps.setString(7, d.getImage_url());
            ps.setInt(8, d.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ used by admin: delete(...)
    public void delete(int id) {
        String sql = "DELETE FROM dish WHERE id=?";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ used by client page: getDishesByMenu(...)
    public List<Dish> getDishesByMenu(int menuId) {
        return getByMenuId(menuId);
    }

    private void applyIngredientAvailability(Connection cnx, Dish dish) {
        String sql = "SELECT COUNT(*) AS recipe_count, " +
                "COALESCE(SUM(CASE " +
                "WHEN i.id IS NULL THEN 1 " +
                "WHEN i.expiryDate IS NOT NULL AND i.expiryDate < CURDATE() THEN 1 " +
                "WHEN i.quantityInStock < di.quantity_required THEN 1 " +
                "ELSE 0 END), 0) AS blocking_count, " +
                "MIN(CASE WHEN di.quantity_required > 0 AND i.id IS NOT NULL THEN FLOOR(i.quantityInStock / di.quantity_required) END) AS possible_servings " +
                "FROM dish_ingredient di " +
                "LEFT JOIN ingredient i ON i.id = di.ingredient_id " +
                "WHERE di.dish_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, dish.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }

                int recipeCount = rs.getInt("recipe_count");
                int blockingCount = rs.getInt("blocking_count");
                Number possibleServingsNumber = (Number) rs.getObject("possible_servings");
                Integer possibleServings = possibleServingsNumber == null ? null : possibleServingsNumber.intValue();

                boolean manualAvailable = dish.getAvailable() != null && dish.getAvailable();
                if (recipeCount == 0) {
                    dish.setAvailable(manualAvailable);
                    return;
                }

                boolean ingredientsAvailable = blockingCount == 0;
                dish.setAvailable(manualAvailable && ingredientsAvailable);
                if (possibleServings != null && possibleServings >= 0) {
                    dish.setStock_quantity(possibleServings);
                }
            }
        } catch (SQLException ignored) {
            // Keep existing dish values when recipe table is unavailable.
        }
    }
}
