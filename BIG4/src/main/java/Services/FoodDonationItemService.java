package Services;

import Entities.FoodDonationItem;
import Utils.Mydatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class FoodDonationItemService {

    private Connection cnx;

    public FoodDonationItemService() {
        try {
            cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== CREATE ====================

    /**
     * Add a new food donation item
     * @param item The FoodDonationItem to add
     * @throws SQLException if database error occurs
     */
    public void addFoodDonationItem(FoodDonationItem item) throws SQLException {
        String sql = "INSERT INTO food_donation_items(donation_event_id, item_id, quantity) VALUES(?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, item.getDonationEventId());
        ps.setInt(2, item.getItemId());
        ps.setInt(3, item.getQuantity());

        ps.executeUpdate();
    }


    public void addMultipleFoodDonationItems(List<FoodDonationItem> items) throws SQLException {
        String sql = "INSERT INTO food_donation_items(donation_event_id, item_id, quantity) VALUES(?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        for (FoodDonationItem item : items) {
            ps.setInt(1, item.getDonationEventId());
            ps.setInt(2, item.getItemId());
            ps.setInt(3, item.getQuantity());
            ps.addBatch();
        }

        ps.executeBatch();
    }

    // ==================== READ ====================


    public List<FoodDonationItem> getAllFoodDonationItems() throws SQLException {
        List<FoodDonationItem> items = new ArrayList<>();
        String sql = "SELECT fdi.donation_event_id, fdi.item_id, fdi.quantity, d.name as item_name " +
                "FROM food_donation_items fdi " +
                "LEFT JOIN dish d ON fdi.item_id = d.id " +
                "ORDER BY fdi.donation_event_id, fdi.item_id";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            items.add(mapResultSetToItem(rs));
        }

        return items;
    }


    public List<FoodDonationItem> getItemsByEventId(Integer eventId) throws SQLException {
        List<FoodDonationItem> items = new ArrayList<>();
        String sql = "SELECT fdi.donation_event_id, fdi.item_id, fdi.quantity, d.name as item_name " +
                "FROM food_donation_items fdi " +
                "LEFT JOIN dish d ON fdi.item_id = d.id " +
                "WHERE fdi.donation_event_id = ? " +
                "ORDER BY fdi.item_id";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            items.add(mapResultSetToItem(rs));
        }

        return items;
    }


    public FoodDonationItem getItemByIds(Integer eventId, Integer itemId) throws SQLException {
        String sql = "SELECT fdi.donation_event_id, fdi.item_id, fdi.quantity, d.name as item_name " +
                "FROM food_donation_items fdi " +
                "LEFT JOIN dish d ON fdi.item_id = d.id " +
                "WHERE fdi.donation_event_id = ? AND fdi.item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ps.setInt(2, itemId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToItem(rs);
        }

        return null;
    }


    public List<FoodDonationItem> getItemsByDishId(Integer itemId) throws SQLException {
        List<FoodDonationItem> items = new ArrayList<>();
        String sql = "SELECT fdi.donation_event_id, fdi.item_id, fdi.quantity, d.name as item_name " +
                "FROM food_donation_items fdi " +
                "LEFT JOIN dish d ON fdi.item_id = d.id " +
                "WHERE fdi.item_id = ? " +
                "ORDER BY fdi.donation_event_id";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, itemId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            items.add(mapResultSetToItem(rs));
        }

        return items;
    }


    public boolean itemExists(Integer eventId, Integer itemId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM food_donation_items " +
                "WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ps.setInt(2, itemId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("count") > 0;
        }

        return false;
    }

    // ==================== UPDATE ====================


    public void updateFoodDonationItem(FoodDonationItem item) throws SQLException {
        String sql = "UPDATE food_donation_items SET quantity = ? " +
                "WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, item.getQuantity());
        ps.setInt(2, item.getDonationEventId());
        ps.setInt(3, item.getItemId());

        ps.executeUpdate();
    }


    public void updateItemQuantity(Integer eventId, Integer itemId, Integer newQuantity) throws SQLException {
        String sql = "UPDATE food_donation_items SET quantity = ? " +
                "WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, newQuantity);
        ps.setInt(2, eventId);
        ps.setInt(3, itemId);

        ps.executeUpdate();
    }


    public void incrementItemQuantity(Integer eventId, Integer itemId, Integer increment) throws SQLException {
        String sql = "UPDATE food_donation_items SET quantity = quantity + ? " +
                "WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, increment);
        ps.setInt(2, eventId);
        ps.setInt(3, itemId);

        ps.executeUpdate();
    }

    public void decrementItemQuantity(Integer eventId, Integer itemId, Integer decrement) throws SQLException {
        String sql = "UPDATE food_donation_items SET quantity = quantity - ? " +
                "WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, decrement);
        ps.setInt(2, eventId);
        ps.setInt(3, itemId);

        ps.executeUpdate();
    }

    // ==================== DELETE ====================


    public void deleteFoodDonationItem(Integer eventId, Integer itemId) throws SQLException {
        String sql = "DELETE FROM food_donation_items WHERE donation_event_id = ? AND item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ps.setInt(2, itemId);

        ps.executeUpdate();
    }

    public void deleteItemsByEventId(Integer eventId) throws SQLException {
        String sql = "DELETE FROM food_donation_items WHERE donation_event_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);

        ps.executeUpdate();
    }


    public void deleteItemsByDishId(Integer itemId) throws SQLException {
        String sql = "DELETE FROM food_donation_items WHERE item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, itemId);

        ps.executeUpdate();
    }

    // ==================== STATISTICS ====================


    public int countAllItems() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM food_donation_items";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("count");
        }

        return 0;
    }


    public int countItemsForEvent(Integer eventId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM food_donation_items WHERE donation_event_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("count");
        }

        return 0;
    }


    public int getTotalQuantityForEvent(Integer eventId) throws SQLException {
        String sql = "SELECT SUM(quantity) as total FROM food_donation_items WHERE donation_event_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }


    public int getTotalQuantityForDish(Integer itemId) throws SQLException {
        String sql = "SELECT SUM(quantity) as total FROM food_donation_items WHERE item_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, itemId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    // ==================== HELPER METHODS ====================

    private FoodDonationItem mapResultSetToItem(ResultSet rs) throws SQLException {
        FoodDonationItem item = new FoodDonationItem();
        item.setDonationEventId(rs.getInt("donation_event_id"));
        item.setItemId(rs.getInt("item_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setItemName(rs.getString("item_name"));
        return item;
    }
}