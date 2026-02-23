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

    public void addFoodDonationItemWithStock(FoodDonationItem item) throws SQLException {
        boolean previousAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            consumeIngredientsForDish(item.getItemId(), item.getQuantity());
            addFoodDonationItem(item);
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(previousAutoCommit);
        }
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

    public void updateItemQuantityWithStock(Integer eventId, Integer itemId, Integer newQuantity) throws SQLException {
        Integer oldQuantity = getCurrentItemQuantity(eventId, itemId);
        if (oldQuantity == null) {
            throw new SQLException("Donation item not found for update (eventId=" + eventId + ", dishId=" + itemId + ")");
        }

        int delta = newQuantity - oldQuantity;

        boolean previousAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            if (delta > 0) {
                consumeIngredientsForDish(itemId, delta);
            } else if (delta < 0) {
                restoreIngredientsForDish(itemId, -delta);
            }
            updateItemQuantity(eventId, itemId, newQuantity);
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(previousAutoCommit);
        }
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

    public void incrementItemQuantityWithStock(Integer eventId, Integer itemId, Integer increment) throws SQLException {
        boolean previousAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            consumeIngredientsForDish(itemId, increment);
            incrementItemQuantity(eventId, itemId, increment);
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(previousAutoCommit);
        }
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

    public void deleteFoodDonationItemWithStock(Integer eventId, Integer itemId) throws SQLException {
        Integer existingQty = getCurrentItemQuantity(eventId, itemId);
        if (existingQty == null) {
            return;
        }

        boolean previousAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            restoreIngredientsForDish(itemId, existingQty);
            deleteFoodDonationItem(eventId, itemId);
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(previousAutoCommit);
        }
    }

    public void deleteItemsByEventId(Integer eventId) throws SQLException {
        String sql = "DELETE FROM food_donation_items WHERE donation_event_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);

        ps.executeUpdate();
    }

    public void deleteItemsByEventIdWithStock(Integer eventId) throws SQLException {
        List<FoodDonationItem> existingItems = getItemsByEventId(eventId);

        boolean previousAutoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            for (FoodDonationItem item : existingItems) {
                restoreIngredientsForDish(item.getItemId(), item.getQuantity());
            }
            deleteItemsByEventId(eventId);
            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(previousAutoCommit);
        }
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

    private Integer getCurrentItemQuantity(Integer eventId, Integer itemId) throws SQLException {
        String sql = "SELECT quantity FROM food_donation_items WHERE donation_event_id = ? AND item_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        return null;
    }

    private List<RecipeLine> getRecipeForDish(Integer dishId) throws SQLException {
        String sql = "SELECT ingredient_id, quantity_required FROM dish_ingredient WHERE dish_id = ?";
        List<RecipeLine> recipe = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recipe.add(new RecipeLine(rs.getInt("ingredient_id"), rs.getDouble("quantity_required")));
                }
            }
        }
        if (recipe.isEmpty()) {
            throw new SQLException("Dish " + dishId + " has no recipe lines in dish_ingredient.");
        }
        return recipe;
    }

    private void consumeIngredientsForDish(Integer dishId, Integer dishQuantity) throws SQLException {
        if (dishQuantity == null || dishQuantity <= 0) {
            throw new SQLException("Dish quantity must be positive.");
        }
        List<RecipeLine> recipe = getRecipeForDish(dishId);

        String sql = "UPDATE ingredient SET quantityInStock = quantityInStock - ? WHERE id = ? AND quantityInStock >= ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (RecipeLine line : recipe) {
                double required = line.quantityRequired * dishQuantity;
                ps.setDouble(1, required);
                ps.setInt(2, line.ingredientId);
                ps.setDouble(3, required);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Insufficient stock for ingredient ID " + line.ingredientId + " to add dish " + dishId + ".");
                }
            }
        }
    }

    private void restoreIngredientsForDish(Integer dishId, Integer dishQuantity) throws SQLException {
        if (dishQuantity == null || dishQuantity <= 0) {
            return;
        }
        List<RecipeLine> recipe = getRecipeForDish(dishId);

        String sql = "UPDATE ingredient SET quantityInStock = quantityInStock + ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (RecipeLine line : recipe) {
                double restore = line.quantityRequired * dishQuantity;
                ps.setDouble(1, restore);
                ps.setInt(2, line.ingredientId);
                ps.executeUpdate();
            }
        }
    }

    private record RecipeLine(int ingredientId, double quantityRequired) {}
}