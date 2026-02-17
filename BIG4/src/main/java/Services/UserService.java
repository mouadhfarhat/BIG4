package Services;

import Entities.DeliveryMan;
import Entities.User;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile CRUD: read/update own profile; admin can list and delete clients/delivery men.
 */
public class UserService {

    /**
     * Load full user by id (including address). For delivery man, does not load delivery_man row here.
     */
    public User getUserById(Long userId) throws SQLException {
        String sql = "SELECT id, email, role, reference_id, full_name, phone, address FROM user WHERE id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        }
        return null;
    }

    /**
     * For delivery man: load linked delivery_man row.
     */
    public DeliveryMan getDeliveryManByUserId(Long userId) throws SQLException {
        User u = getUserById(userId);
        if (u == null || u.getReferenceId() == null) return null;
        return new DeliverymanService().getDeliveryManById(u.getReferenceId());
    }

    /**
     * Update user row (all roles). Email must be unique per role.
     */
    public void updateUserProfile(Long userId, String fullName, String email, String phone, String address) throws SQLException {
        email = email != null ? email.trim().toLowerCase() : "";
        String sql = "UPDATE user SET full_name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName != null ? fullName : "");
            ps.setString(2, email);
            ps.setString(3, phone != null ? phone : "");
            ps.setString(4, address != null ? address : "");
            ps.setLong(5, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Update delivery_man row (for delivery man profile). Also updates user row for name/email/phone.
     */
    public void updateDeliveryManProfile(Long userId, Long deliveryManId, String name, String email, String phone,
                                         String vehicleType, String vehicleNumber, String address) throws SQLException {
        DeliverymanService dmService = new DeliverymanService();
        DeliveryMan dm = dmService.getDeliveryManById(deliveryManId);
        if (dm == null) return;
        dm.setName(name != null ? name : dm.getName());
        dm.setEmail(email != null ? email.trim() : dm.getEmail());
        dm.setPhone(phone != null ? phone : dm.getPhone());
        dm.setVehicleType(vehicleType != null ? vehicleType : dm.getVehicleType());
        dm.setVehicleNumber(vehicleNumber != null ? vehicleNumber : dm.getVehicleNumber());
        dm.setAddress(address != null ? address : dm.getAddress());
        dmService.updateDeliveryMan2(dm);
        updateUserProfile(userId, name, email, phone, address);
    }

    /**
     * Delete user by id. If role was DELIVERY_MAN, also delete delivery_man row.
     */
    public void deleteUser(Long userId) throws SQLException {
        User u = getUserById(userId);
        if (u == null) return;
        if (u.getReferenceId() != null) {
            try {
                new DeliverymanService().deleteDeliveryMan(u.getReferenceId());
            } catch (SQLException ignored) { }
        }
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Admin only: delete another user (client or delivery man). Admins cannot be deleted this way.
     */
    public void deleteUserByAdmin(Long adminUserId, Long targetUserId) throws SQLException {
        User admin = getUserById(adminUserId);
        if (admin == null || !"ADMIN".equals(admin.getRole())) throw new SecurityException("Not authorized");
        User target = getUserById(targetUserId);
        if (target == null) return;
        if ("ADMIN".equals(target.getRole())) throw new SecurityException("Cannot delete an admin");
        deleteUser(targetUserId);
    }

    /**
     * List all users with role CLIENT (for admin management).
     */
    public List<User> listClients() throws SQLException {
        return listUsersByRole("CLIENT");
    }

    /**
     * List all users with role DELIVERY_MAN (for admin management).
     */
    public List<User> listDeliveryMen() throws SQLException {
        return listUsersByRole("DELIVERY_MAN");
    }

    private List<User> listUsersByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, email, role, reference_id, full_name, phone, address FROM user WHERE role = ? ORDER BY full_name";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUser(rs));
        }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setReferenceId(rs.getObject("reference_id") != null ? rs.getLong("reference_id") : null);
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        try { u.setAddress(rs.getString("address")); } catch (SQLException ignored) { }
        return u;
    }
}
