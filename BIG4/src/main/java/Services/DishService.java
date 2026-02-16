package Services;

import Entities.Dish;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishService {

    private final Connection cnx;

    public DishService() {
        cnx = Mydatabase.getInstance().getConnection();
    }

    // ✅ CREATE (now includes menu_id)
    public void add(Dish dish) throws SQLException {

        String sql = "INSERT INTO dish " +
                "(menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        System.out.println("DEBUG DishService.add menu_id=" + dish.getMenu_id());
        ps.setInt(1, dish.getMenu_id());              // ✅ menu_id
        ps.setString(2, dish.getName());
        ps.setString(3, dish.getDescription());
        ps.setFloat(4, dish.getBase_price());
        ps.setBoolean(5, dish.getAvailable());
        ps.setInt(6, dish.getStock_quantity());
        ps.setString(7, dish.getImage_url());
        ps.setTimestamp(8, dish.getCreated_at());
        ps.setTimestamp(9, dish.getUpdate_at());
        System.out.println("DEBUG menu_id = " + dish.getMenu_id());
        ps.executeUpdate();
    }

    // ✅ READ ALL (now reads menu_id)
    public List<Dish> getAll() throws SQLException {

        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dish";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Dish d = new Dish();

            d.setId(rs.getInt("id"));
            d.setMenu_id(rs.getInt("menu_id"));       // ✅ menu_id
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

        return dishes;
    }

    // ✅ READ BY ID (now reads menu_id)
    public Dish getById(int id) throws SQLException {

        String sql = "SELECT * FROM dish WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Dish d = new Dish();

            d.setId(rs.getInt("id"));
            d.setMenu_id(rs.getInt("menu_id"));       // ✅ menu_id
            d.setName(rs.getString("name"));
            d.setDescription(rs.getString("description"));
            d.setBase_price(rs.getFloat("base_price"));
            d.setAvailable(rs.getBoolean("available"));
            d.setStock_quantity(rs.getInt("stock_quantity"));
            d.setImage_url(rs.getString("image_url"));
            d.setCreated_at(rs.getTimestamp("created_at"));
            d.setUpdate_at(rs.getTimestamp("updated_at"));

            return d;
        }

        return null;
    }

    // ✅ UPDATE (now includes menu_id too)
    public void update(Dish dish) throws SQLException {

        String sql = "UPDATE dish SET " +
                "menu_id = ?, name = ?, description = ?, base_price = ?, available = ?, " +
                "stock_quantity = ?, image_url = ?, updated_at = ? " +
                "WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, dish.getMenu_id());              // ✅ menu_id
        ps.setString(2, dish.getName());
        ps.setString(3, dish.getDescription());
        ps.setFloat(4, dish.getBase_price());
        ps.setBoolean(5, dish.getAvailable());
        ps.setInt(6, dish.getStock_quantity());
        ps.setString(7, dish.getImage_url());
        ps.setTimestamp(8, dish.getUpdate_at());
        ps.setInt(9, dish.getId());

        ps.executeUpdate();
    }

    // ✅ DELETE
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM dish WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
    }

    // ⭐ BONUS: get dishes by menu_id (useful for UI later)
    public List<Dish> getByMenuId(int menuId) throws SQLException {

        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dish WHERE menu_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, menuId);

        ResultSet rs = ps.executeQuery();

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

        return dishes;
    }
}
