package Services;

import Entities.DeliveryMan;
import Entities.User;
import Utils.Mydatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

/**
 * Handles user authentication: login and sign up for roles DELIVERY_MAN, ADMIN, CLIENT.
 */
public class AuthService {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Login: find user by email, password hash, and role.
     */
    public User login(String email, String password, String role) throws SQLException {
        ensureUserTable();
        String hash = hashPassword(password);
        String sql = "SELECT id, email, role, reference_id, full_name, phone, address FROM user WHERE email = ? AND password_hash = ? AND role = ?";
        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, hash);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
        return null;
    }

    /**
     * Sign up: create user (and for DELIVERY_MAN also create delivery_man row).
     * Admin cannot sign up; they are created manually or via default admin.
     */
    public User signUp(String email, String password, String role, String fullName, String phone,
                       String vehicleType, String vehicleNumber) throws SQLException {
        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException("Admin accounts cannot self-register. Contact your administrator.");
        }
        ensureUserTable();
        email = email.trim().toLowerCase();
        if (email.isEmpty() || password == null || password.length() < 4) {
            throw new IllegalArgumentException("Email and password (min 4 characters) required.");
        }

        try (Connection conn = Mydatabase.getInstance().getConnection()) {
            // Check email not already used for this role
            if (userExists(conn, email, role)) {
                throw new IllegalArgumentException("An account with this email already exists for this role.");
            }

            Long referenceId = null;
            if ("DELIVERY_MAN".equals(role)) {
                DeliveryMan dm = new DeliveryMan(
                        fullName != null ? fullName : email,
                        phone != null ? phone : "",
                        email,
                        vehicleType != null ? vehicleType : "Motorcycle",
                        vehicleNumber != null ? vehicleNumber : ""
                );
                dm.setAddress("");
                referenceId = new DeliverymanService().addDeliveryManAndGetId(dm);
            }

            String hash = hashPassword(password);
            String sql = "INSERT INTO user (email, password_hash, role, reference_id, full_name, phone) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, email);
                ps.setString(2, hash);
                ps.setString(3, role);
                if (referenceId != null) {
                    ps.setLong(4, referenceId);
                } else {
                    ps.setNull(4, Types.BIGINT);
                }
                ps.setString(5, fullName != null ? fullName : email);
                ps.setString(6, phone != null ? phone : "");
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    User u = new User();
                    u.setId(keys.getLong(1));
                    u.setEmail(email);
                    u.setRole(role);
                    u.setReferenceId(referenceId);
                    u.setFullName(fullName != null ? fullName : email);
                    u.setPhone(phone);
                    return u;
                }
            }
        }
        return null;
    }

    private boolean userExists(Connection conn, String email, String role) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ? AND role = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private void ensureUserTable() throws SQLException {
        try (Connection conn = Mydatabase.getInstance().getConnection();
             Statement st = conn.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS user (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                            "email VARCHAR(255) NOT NULL," +
                            "password_hash VARCHAR(512) NOT NULL," +
                            "role VARCHAR(32) NOT NULL," +
                            "reference_id BIGINT NULL," +
                            "full_name VARCHAR(255) NULL," +
                            "phone VARCHAR(64) NULL," +
                            "address VARCHAR(255) NULL," +
                            "UNIQUE KEY uk_email_role (email, role)" +
                            ")"
            );
            try { st.execute("ALTER TABLE user ADD COLUMN address VARCHAR(255) NULL"); } catch (SQLException ignored) { }
        }
        ensureDefaultAdmin();
    }

    /** Default admin created automatically if no admin exists. Credentials: admin@big4.com / Admin@123 */
    private void ensureDefaultAdmin() throws SQLException {
        try (Connection conn = Mydatabase.getInstance().getConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM user WHERE role = 'ADMIN' LIMIT 1");
                 ResultSet rs = check.executeQuery()) {
                if (rs.next()) return;
            }
            String hash = hashPassword("Admin@123");
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO user (email, password_hash, role, reference_id, full_name, phone) VALUES (?, ?, 'ADMIN', NULL, ?, NULL)")) {
                ins.setString(1, "admin@big4.com");
                ins.setString(2, hash);
                ins.setString(3, "System Admin");
                ins.executeUpdate();
            }
        }
    }
}
