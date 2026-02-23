package Services;

import Entities.Fooddonationevent;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Fooddonationeventservice {

    private Connection cnx;

    public Fooddonationeventservice() {
        try {
            cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== CREATE ====================

    /**
     * Add new food donation event (WITHOUT calendar_event_id - matches your DB)
     */
    public void addFoodDonationEvent(Fooddonationevent event) throws SQLException {
        String sql = "INSERT INTO food_donation_event(event_date, total_quantity, charity_name, status, delivery_id) " +
                "VALUES(?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setDate(1, event.getEventDate());
        ps.setInt(2, event.getTotalQuantity());
        ps.setString(3, event.getCharityName());
        ps.setString(4, event.getStatus() != null ? event.getStatus() : "PENDING");

        if (event.getDeliveryId() != null) {
            ps.setLong(5, event.getDeliveryId());
        } else {
            ps.setNull(5, Types.BIGINT);
        }

        ps.executeUpdate();

        // Get the generated ID
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            event.setDonationEventId(rs.getInt(1));
        }
    }

    // ==================== READ ====================

    /**
     * Get all food donation events
     */
    public List<Fooddonationevent> getAllFoodDonationEvents() throws SQLException {
        List<Fooddonationevent> events = new ArrayList<>();
        String sql = "SELECT * FROM food_donation_event ORDER BY event_date DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            events.add(mapResultSetToEvent(rs));
        }

        return events;
    }

    /**
     * Get food donation event by ID
     */
    public Fooddonationevent getFoodDonationEventById(Integer id) throws SQLException {
        String sql = "SELECT * FROM food_donation_event WHERE donation_event_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToEvent(rs);
        }

        return null;
    }

    /**
     * Get food donation events by status
     */
    public List<Fooddonationevent> getFoodDonationEventsByStatus(String status) throws SQLException {
        List<Fooddonationevent> events = new ArrayList<>();
        String sql = "SELECT * FROM food_donation_event WHERE status = ? ORDER BY event_date DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            events.add(mapResultSetToEvent(rs));
        }

        return events;
    }

    // ==================== UPDATE ====================

    /**
     * Update food donation event (WITHOUT calendar_event_id - matches your DB)
     */
    public void updateFoodDonationEvent(Fooddonationevent event) throws SQLException {
        String sql = "UPDATE food_donation_event SET event_date=?, total_quantity=?, charity_name=?, " +
                "status=?, delivery_id=? WHERE donation_event_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, event.getEventDate());
        ps.setInt(2, event.getTotalQuantity());
        ps.setString(3, event.getCharityName());
        ps.setString(4, event.getStatus());

        if (event.getDeliveryId() != null) {
            ps.setLong(5, event.getDeliveryId());
        } else {
            ps.setNull(5, Types.BIGINT);
        }

        ps.setInt(6, event.getDonationEventId());

        ps.executeUpdate();
    }

    /**
     * Update event status
     */
    public void updateEventStatus(Integer eventId, String status) throws SQLException {
        String sql = "UPDATE food_donation_event SET status=? WHERE donation_event_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, eventId);
        ps.executeUpdate();
    }

    // ==================== DELETE ====================

    /**
     * Delete food donation event
     */
    public void deleteFoodDonationEvent(Integer eventId) throws SQLException {
        String sql = "DELETE FROM food_donation_event WHERE donation_event_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ps.executeUpdate();
    }

    // ==================== STATISTICS ====================

    /**
     * Count all food donation events
     */
    public int countAllEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM food_donation_event";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("count");
        }

        return 0;
    }

    /**
     * Count events by status
     */
    public int countEventsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM food_donation_event WHERE status=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("count");
        }

        return 0;
    }

    /**
     * Get total quantity donated
     */
    public int getTotalQuantityDonated() throws SQLException {
        String sql = "SELECT SUM(total_quantity) as total FROM food_donation_event";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Map ResultSet to FoodDonationEvent object (WITHOUT calendar_event_id)
     */
    private Fooddonationevent mapResultSetToEvent(ResultSet rs) throws SQLException {
        Fooddonationevent event = new Fooddonationevent();

        event.setDonationEventId(rs.getInt("donation_event_id"));
        event.setEventDate(rs.getDate("event_date"));
        event.setTotalQuantity(rs.getInt("total_quantity"));
        event.setCharityName(rs.getString("charity_name"));
        event.setStatus(rs.getString("status"));

        // Handle nullable delivery_id field
        long deliveryId = rs.getLong("delivery_id");
        if (!rs.wasNull()) {
            event.setDeliveryId(deliveryId);
        }

        event.setCreatedAt(rs.getTimestamp("created_at"));
        event.setUpdatedAt(rs.getTimestamp("updated_at"));

        return event;
    }
}