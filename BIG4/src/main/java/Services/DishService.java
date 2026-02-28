package Services;

import Entities.Dish;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishService {

    public List<Dish> getAll() {
        String sql = "SELECT id, menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at " +
                "FROM dish ORDER BY id DESC";

        List<Dish> dishes = new ArrayList<>();

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
        String sql = "INSERT INTO dish(menu_id, name, description, base_price, available, stock_quantity, image_url) " +
                "VALUES(?,?,?,?,?,?,?)";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, d.getMenu_id());
            ps.setString(2, d.getName());
            ps.setString(3, d.getDescription());
            ps.setFloat(4, d.getBase_price());
            ps.setBoolean(5, d.getAvailable() != null && d.getAvailable());
            ps.setInt(6, d.getStock_quantity());
            ps.setString(7, d.getImage_url());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
