package inventory;

import Entities.Ingredient;
import Entities.WasteRecord;
import Utils.Mydatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InventoryIntegrationTest {

    private static final String INGREDIENT_INSERT = "INSERT INTO Ingredient (name, quantityInStock, unit, minStockLevel, unitCost, expiryDate) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INGREDIENT_SELECT = "SELECT id, name, quantityInStock, unit, minStockLevel, unitCost, expiryDate FROM Ingredient WHERE id = ?";
    private static final String INGREDIENT_UPDATE = "UPDATE Ingredient SET quantityInStock = ?, minStockLevel = ?, unitCost = ? WHERE id = ?";
    private static final String INGREDIENT_DELETE = "DELETE FROM Ingredient WHERE id = ?";
    private static final String INGREDIENT_DECREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock - ? WHERE id = ?";
    private static final String INGREDIENT_INCREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock + ? WHERE id = ?";
    private static final String WASTE_INSERT = "INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason) VALUES (?, ?, ?, ?, ?)";
    private static final String WASTE_SELECT = "SELECT id, ingredientId, quantityWasted, wasteType, date, reason FROM WasteRecord WHERE id = ?";
    private static final String WASTE_DELETE = "DELETE FROM WasteRecord WHERE id = ?";

    private Connection connection;

    @BeforeEach
    void setUp() {
        try {
            connection = Mydatabase.getInstance().getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            Assumptions.assumeTrue(false, "Database unavailable: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    @Test
    void ingredientCrudCycle() throws SQLException {
        String uniqueName = "Test Ingredient " + System.nanoTime();
        long ingredientId = insertIngredient(uniqueName, 25.0, "kg", 5.0, 2.5, LocalDate.now().plusDays(7));

        Ingredient created = findIngredient(ingredientId);
        assertNotNull(created, "Ingredient should be retrievable after insert");
        assertEquals(uniqueName, created.getName());
        assertEquals(25.0, created.getQuantityInStock(), 0.001);

        updateIngredientQuantities(ingredientId, 18.0, 3.0, 3.0);
        Ingredient updated = findIngredient(ingredientId);
        assertNotNull(updated, "Ingredient should still exist after update");
        assertEquals(18.0, updated.getQuantityInStock(), 0.001);
        assertEquals(3.0, updated.getMinStockLevel(), 0.001);
        assertEquals(3.0, updated.getUnitCost(), 0.001);

        deleteIngredient(ingredientId);
        Ingredient deleted = findIngredient(ingredientId);
        assertNull(deleted, "Ingredient should be removed after delete operation");
    }

    @Test
    void wasteRecordLifecycle() throws SQLException {
        String uniqueName = "Waste Ingredient " + System.nanoTime();
        long ingredientId = insertIngredient(uniqueName, 20.0, "kg", 4.0, 3.0, LocalDate.now().plusDays(10));

        double wasteQuantity = 3.0;
        String wasteType = "Spoilage";
        String wasteReason = "JUnit verification";
        long wasteId = recordWaste(ingredientId, wasteQuantity, wasteType, wasteReason);

        WasteRecord savedWaste = findWaste(wasteId);
        assertNotNull(savedWaste, "Waste record should be stored");
        assertEquals(ingredientId, savedWaste.getIngredientId());
        assertEquals(wasteQuantity, savedWaste.getQuantityWasted(), 0.001);
        assertEquals(wasteType, savedWaste.getWasteType());
        assertEquals(wasteReason, savedWaste.getReason());

        Ingredient afterWaste = findIngredient(ingredientId);
        assertNotNull(afterWaste, "Ingredient must still exist after recording waste");
        assertEquals(17.0, afterWaste.getQuantityInStock(), 0.001, "Stock should decrease by wasted amount");

        deleteWaste(wasteId, ingredientId, wasteQuantity);
        WasteRecord removedWaste = findWaste(wasteId);
        assertNull(removedWaste, "Waste record should be deleted");

        Ingredient restoredIngredient = findIngredient(ingredientId);
        assertNotNull(restoredIngredient, "Ingredient remains after waste removal");
        assertEquals(20.0, restoredIngredient.getQuantityInStock(), 0.001, "Stock should restore after waste rollback");
    }

    private long insertIngredient(String name, double quantity, String unit, double minStock,
                                  double unitCost, LocalDate expiry) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INGREDIENT_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setDouble(2, quantity);
            statement.setString(3, unit);
            statement.setDouble(4, minStock);
            statement.setDouble(5, unitCost);
            if (expiry != null) {
                statement.setDate(6, Date.valueOf(expiry));
            } else {
                statement.setNull(6, java.sql.Types.DATE);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Unable to insert ingredient");
    }

    private Ingredient findIngredient(long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INGREDIENT_SELECT)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Date expiryDate = resultSet.getDate("expiryDate");
                    return new Ingredient(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getDouble("quantityInStock"),
                            resultSet.getString("unit"),
                            resultSet.getDouble("minStockLevel"),
                            resultSet.getDouble("unitCost"),
                            expiryDate == null ? null : expiryDate.toLocalDate()
                    );
                }
            }
        }
        return null;
    }

    private void updateIngredientQuantities(long id, double quantity, double minStock, double unitCost) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INGREDIENT_UPDATE)) {
            statement.setDouble(1, quantity);
            statement.setDouble(2, minStock);
            statement.setDouble(3, unitCost);
            statement.setLong(4, id);
            statement.executeUpdate();
        }
    }

    private void deleteIngredient(long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INGREDIENT_DELETE)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private long recordWaste(long ingredientId, double quantity, String wasteType, String reason) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement insertWaste = connection.prepareStatement(WASTE_INSERT, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement decrementStock = connection.prepareStatement(INGREDIENT_DECREMENT_STOCK)) {
            insertWaste.setLong(1, ingredientId);
            insertWaste.setDouble(2, quantity);
            insertWaste.setString(3, wasteType);
            insertWaste.setTimestamp(4, Timestamp.valueOf(now));
            insertWaste.setString(5, reason);
            insertWaste.executeUpdate();
            try (ResultSet keys = insertWaste.getGeneratedKeys()) {
                if (keys.next()) {
                    decrementStock.setDouble(1, quantity);
                    decrementStock.setLong(2, ingredientId);
                    decrementStock.executeUpdate();
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Unable to record waste");
    }

    private WasteRecord findWaste(long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(WASTE_SELECT)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("date");
                    return new WasteRecord(
                            resultSet.getLong("id"),
                            resultSet.getLong("ingredientId"),
                            resultSet.getDouble("quantityWasted"),
                            resultSet.getString("wasteType"),
                            timestamp == null ? null : timestamp.toLocalDateTime(),
                            resultSet.getString("reason")
                    );
                }
            }
        }
        return null;
    }

    private void deleteWaste(long wasteId, long ingredientId, double quantity) throws SQLException {
        try (PreparedStatement deleteWaste = connection.prepareStatement(WASTE_DELETE);
             PreparedStatement incrementStock = connection.prepareStatement(INGREDIENT_INCREMENT_STOCK)) {
            deleteWaste.setLong(1, wasteId);
            deleteWaste.executeUpdate();
            incrementStock.setDouble(1, quantity);
            incrementStock.setLong(2, ingredientId);
            incrementStock.executeUpdate();
        }
    }
}
