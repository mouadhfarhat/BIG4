package Services;

import Entities.Menu;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuService {

    // ✅ used by admin: getAllMenu()
    public List<Menu> getAllMenu() {
        String sql = "SELECT id, title, description, isActive, created_at, updated_at FROM menu ORDER BY id DESC";
        List<Menu> menus = new ArrayList<>();

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Menu m = new Menu();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setDescription(rs.getString("description"));
                m.setActive(rs.getBoolean("isActive"));
                m.setCreatedAt(rs.getTimestamp("created_at"));
                m.setUpdatedAt(rs.getTimestamp("updated_at"));
                menus.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menus;
    }

    // ✅ used by admin: addMenu2(...)
    public void addMenu2(Menu m) {
        String sql = "INSERT INTO menu(title, description, isActive) VALUES(?,?,?)";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setBoolean(3, m.isActive());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ used by admin: updateMenu(...)
    public void updateMenu(Menu m) {
        String sql = "UPDATE menu SET title=?, description=?, isActive=? WHERE id=?";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setBoolean(3, m.isActive());
            ps.setInt(4, m.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ used by admin: delete(...)
    public void delete(int id) {
        String sql = "DELETE FROM menu WHERE id=?";

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ used by client page (optional)
    public List<Menu> getActiveMenus() {
        String sql = "SELECT id, title, description, isActive FROM menu WHERE isActive=1 ORDER BY title";
        List<Menu> menus = new ArrayList<>();

        try (Connection cnx = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Menu m = new Menu();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setDescription(rs.getString("description"));
                m.setActive(rs.getBoolean("isActive"));
                menus.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menus;
    }
}
