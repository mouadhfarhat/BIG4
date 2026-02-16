package Entities;

import Utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDAO {

    public static boolean createDelivery(Delivery delivery) {
        String sql = "INSERT INTO delivery (order_id, delivery_man_id, recipient_name, recipient_phone, " +
                "delivery_address, pickup_location, status, scheduled_date, estimated_time, delivery_notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, delivery.getOrderId());
            stmt.setObject(2, delivery.getDeliveryManId());
            stmt.setString(3, delivery.getRecipientName());
            stmt.setString(4, delivery.getRecipientPhone());
            stmt.setString(5, delivery.getDeliveryAddress());
            stmt.setString(6, delivery.getPickupLocation());
            stmt.setString(7, delivery.getStatus());
            stmt.setObject(8, delivery.getScheduledDate());
            stmt.setObject(9, delivery.getEstimatedTime());
            stmt.setString(10, delivery.getDeliveryNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        delivery.setDeliveryId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Delivery> getAllDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM delivery ORDER BY created_at DESC";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                deliveries.add(mapResultSetToDelivery(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveries;
    }

    public static Delivery getDeliveryById(Long deliveryId) {
        String sql = "SELECT * FROM delivery WHERE delivery_id = ?";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deliveryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDelivery(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateDeliveryStatus(Long deliveryId, String newStatus) {
        String sql = "UPDATE delivery SET status = ?, updated_at = NOW() WHERE delivery_id = ?";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setLong(2, deliveryId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateDelivery(Delivery delivery) {
        String sql = "UPDATE delivery SET recipient_name = ?, recipient_phone = ?, delivery_address = ?, " +
                "pickup_location = ?, status = ?, scheduled_date = ?, estimated_time = ?, " +
                "delivery_notes = ?, delivery_man_id = ?, updated_at = NOW() WHERE delivery_id = ?";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, delivery.getRecipientName());
            stmt.setString(2, delivery.getRecipientPhone());
            stmt.setString(3, delivery.getDeliveryAddress());
            stmt.setString(4, delivery.getPickupLocation());
            stmt.setString(5, delivery.getStatus());
            stmt.setObject(6, delivery.getScheduledDate());
            stmt.setObject(7, delivery.getEstimatedTime());
            stmt.setString(8, delivery.getDeliveryNotes());
            stmt.setObject(9, delivery.getDeliveryManId());
            stmt.setLong(10, delivery.getDeliveryId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteDelivery(Long deliveryId) {
        String sql = "DELETE FROM delivery WHERE delivery_id = ?";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deliveryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Delivery> searchDeliveries(String searchTerm) {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM delivery WHERE recipient_name LIKE ? OR recipient_phone LIKE ? " +
                "OR delivery_id LIKE ? ORDER BY created_at DESC";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deliveries.add(mapResultSetToDelivery(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveries;
    }

    public static List<Delivery> getDeliveriesByStatus(String status) {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "SELECT * FROM delivery WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = Mydatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deliveries.add(mapResultSetToDelivery(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deliveries;
    }

    private static Delivery mapResultSetToDelivery(ResultSet rs) throws SQLException {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(rs.getLong("delivery_id"));
        delivery.setOrderId(rs.getLong("order_id"));
        delivery.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
        delivery.setRecipientName(rs.getString("recipient_name"));
        delivery.setRecipientPhone(rs.getString("recipient_phone"));
        delivery.setDeliveryAddress(rs.getString("delivery_address"));
        delivery.setPickupLocation(rs.getString("pickup_location"));
        delivery.setStatus(rs.getString("status"));
        delivery.setScheduledDate(rs.getObject("scheduled_date") != null ? rs.getTimestamp("scheduled_date").toLocalDateTime() : null);
        delivery.setActualDeliveryDate(rs.getObject("actual_delivery_date") != null ? rs.getTimestamp("actual_delivery_date").toLocalDateTime() : null);
        delivery.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
        delivery.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getBigDecimal("current_latitude") : null);
        delivery.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getBigDecimal("current_longitude") : null);
        delivery.setDeliveryNotes(rs.getString("delivery_notes"));
        delivery.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
        delivery.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        delivery.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return delivery;
    }
}