package Services;

import Entities.Delivery;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryService {

    private  Connection cnx;

    public DeliveryService() {
        try {
            cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Add new delivery (using PreparedStatement - SAFER)
     */
    public void addDelivery2(Delivery delivery) throws SQLException {
        String sql = "insert into delivery(order_id, delivery_man_id, delivery_address, recipient_name, recipient_phone, pickup_location, status, scheduled_date, estimated_time, delivery_notes)" +
                "values(?,?,?,?,?,?,?,NOW(),?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setLong(1, delivery.getOrderId());
        ps.setObject(2, delivery.getDeliveryManId());
        ps.setString(3, delivery.getDeliveryAddress());
        ps.setString(4, delivery.getRecipientName());
        ps.setString(5, delivery.getRecipientPhone());
        ps.setString(6, delivery.getPickupLocation());
        ps.setString(7, delivery.getStatus());
        ps.setObject(8, delivery.getEstimatedTime());
        ps.setString(9, delivery.getDeliveryNotes());
        ps.executeUpdate();
    }

    /**
     * Get all deliveries
     */
    public List<Delivery> getAllDeliveries() throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "select * from delivery";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Delivery d = new Delivery();
            d.setDeliveryId(rs.getLong("delivery_id"));
            d.setOrderId(rs.getLong("order_id"));
            d.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
            d.setDeliveryAddress(rs.getString("delivery_address"));
            d.setRecipientName(rs.getString("recipient_name"));
            d.setRecipientPhone(rs.getString("recipient_phone"));
            d.setPickupLocation(rs.getString("pickup_location"));
            d.setStatus(rs.getString("status"));
            d.setScheduledDate(rs.getTimestamp("scheduled_date"));
            d.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
            d.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getDouble("current_latitude") : null);
            d.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getDouble("current_longitude") : null);
            d.setDeliveryNotes(rs.getString("delivery_notes"));
            d.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
            deliveries.add(d);
        }
        return deliveries;
    }

    /**
     * Get delivery by ID
     */
    public Delivery getDeliveryById(Long id) throws SQLException {
        String sql = "select * from delivery where delivery_id = " + id;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            Delivery d = new Delivery();
            d.setDeliveryId(rs.getLong("delivery_id"));
            d.setOrderId(rs.getLong("order_id"));
            d.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
            d.setDeliveryAddress(rs.getString("delivery_address"));
            d.setRecipientName(rs.getString("recipient_name"));
            d.setRecipientPhone(rs.getString("recipient_phone"));
            d.setPickupLocation(rs.getString("pickup_location"));
            d.setStatus(rs.getString("status"));
            d.setScheduledDate(rs.getTimestamp("scheduled_date"));
            d.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
            d.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getDouble("current_latitude") : null);
            d.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getDouble("current_longitude") : null);
            d.setDeliveryNotes(rs.getString("delivery_notes"));
            d.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
            return d;
        }
        return null;
    }

    /**
     * Get delivery by order ID
     */
    public Delivery getDeliveryByOrderId(Long orderId) throws SQLException {
        String sql = "select * from delivery where order_id = " + orderId;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            Delivery d = new Delivery();
            d.setDeliveryId(rs.getLong("delivery_id"));
            d.setOrderId(rs.getLong("order_id"));
            d.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
            d.setDeliveryAddress(rs.getString("delivery_address"));
            d.setRecipientName(rs.getString("recipient_name"));
            d.setRecipientPhone(rs.getString("recipient_phone"));
            d.setPickupLocation(rs.getString("pickup_location"));
            d.setStatus(rs.getString("status"));
            d.setScheduledDate(rs.getTimestamp("scheduled_date"));
            d.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
            d.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getDouble("current_latitude") : null);
            d.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getDouble("current_longitude") : null);
            d.setDeliveryNotes(rs.getString("delivery_notes"));
            d.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
            return d;
        }
        return null;
    }

    /**
     * Get deliveries by status
     */
    public List<Delivery> getDeliveriesByStatus(String status) throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "select * from delivery where status = '" + status + "'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Delivery d = new Delivery();
            d.setDeliveryId(rs.getLong("delivery_id"));
            d.setOrderId(rs.getLong("order_id"));
            d.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
            d.setDeliveryAddress(rs.getString("delivery_address"));
            d.setRecipientName(rs.getString("recipient_name"));
            d.setRecipientPhone(rs.getString("recipient_phone"));
            d.setPickupLocation(rs.getString("pickup_location"));
            d.setStatus(rs.getString("status"));
            d.setScheduledDate(rs.getTimestamp("scheduled_date"));
            d.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
            d.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getDouble("current_latitude") : null);
            d.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getDouble("current_longitude") : null);
            d.setDeliveryNotes(rs.getString("delivery_notes"));
            d.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
            deliveries.add(d);
        }
        return deliveries;
    }

    /**
     * Get pending deliveries
     */
    public List<Delivery> getPendingDeliveries() throws SQLException {
        return getDeliveriesByStatus("PENDING");
    }

    /**
     * Get deliveries for specific delivery man
     */
    public List<Delivery> getDeliveriesByDeliveryMan(Long deliveryManId) throws SQLException {
        List<Delivery> deliveries = new ArrayList<>();
        String sql = "select * from delivery where delivery_man_id = " + deliveryManId;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Delivery d = new Delivery();
            d.setDeliveryId(rs.getLong("delivery_id"));
            d.setOrderId(rs.getLong("order_id"));
            d.setDeliveryManId(rs.getObject("delivery_man_id") != null ? rs.getLong("delivery_man_id") : null);
            d.setDeliveryAddress(rs.getString("delivery_address"));
            d.setRecipientName(rs.getString("recipient_name"));
            d.setRecipientPhone(rs.getString("recipient_phone"));
            d.setPickupLocation(rs.getString("pickup_location"));
            d.setStatus(rs.getString("status"));
            d.setScheduledDate(rs.getTimestamp("scheduled_date"));
            d.setEstimatedTime(rs.getObject("estimated_time") != null ? rs.getInt("estimated_time") : null);
            d.setCurrentLatitude(rs.getObject("current_latitude") != null ? rs.getDouble("current_latitude") : null);
            d.setCurrentLongitude(rs.getObject("current_longitude") != null ? rs.getDouble("current_longitude") : null);
            d.setDeliveryNotes(rs.getString("delivery_notes"));
            d.setRating(rs.getObject("rating") != null ? rs.getInt("rating") : null);
            deliveries.add(d);
        }
        return deliveries;
    }


    /**
     * Update delivery (using PreparedStatement - SAFER)
     */
    public void updateDelivery2(Delivery delivery) throws SQLException {
        String sql = "update delivery set delivery_address=?, recipient_name=?, recipient_phone=?, " +
                "pickup_location=?, status=?, estimated_time=?, delivery_notes=? where delivery_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, delivery.getDeliveryAddress());
        ps.setString(2, delivery.getRecipientName());
        ps.setString(3, delivery.getRecipientPhone());
        ps.setString(4, delivery.getPickupLocation());
        ps.setString(5, delivery.getStatus());
        ps.setObject(6, delivery.getEstimatedTime());
        ps.setString(7, delivery.getDeliveryNotes());
        ps.setLong(8, delivery.getDeliveryId());
        ps.executeUpdate();
    }

    /**
     * Update delivery status
     */
    public void updateDeliveryStatus(Long id, String status) throws SQLException {
        String sql = "update delivery set status='" + status + "' where delivery_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Assign delivery to delivery man
     */
    public void assignDeliveryToDeliveryMan(Long deliveryId, Long deliveryManId) throws SQLException {
        String sql = "update delivery set delivery_man_id=" + deliveryManId + ", status='ACCEPTED' where delivery_id=" + deliveryId;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Update delivery location
     */
    public void updateDeliveryLocation(Long id, Double latitude, Double longitude) throws SQLException {
        String sql = "update delivery set current_latitude=" + latitude + ", current_longitude=" + longitude + " where delivery_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Rate delivery
     */
    public void rateDelivery(Long id, Integer rating) throws SQLException {
        String sql = "update delivery set rating=" + rating + " where delivery_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Delete delivery
     */
    public void deleteDelivery(Long id) throws SQLException {
        String sql = "delete from delivery where delivery_id=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    /**
     * Cancel delivery
     */
    public void cancelDelivery(Long id) throws SQLException {
        updateDeliveryStatus(id, "CANCELED");
    }

    /**
     * Count all deliveries
     */
    public int countDeliveries() throws SQLException {
        String sql = "select count(*) as count from delivery";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }

    /**
     * Count pending deliveries for delivery man
     */
    public int countPendingDeliveries(Long deliveryManId) throws SQLException {
        String sql = "select count(*) as count from delivery where delivery_man_id=" + deliveryManId + " and status='PENDING'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }
}
