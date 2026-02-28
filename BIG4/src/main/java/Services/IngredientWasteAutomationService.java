package Services;

import Utils.Mydatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class IngredientWasteAutomationService {

    private static final String EXPIRED_QUERY = "SELECT id, quantityInStock FROM Ingredient WHERE quantityInStock > 0 AND expiryDate IS NOT NULL AND expiryDate < CURDATE()";
    private static final String INSERT_WASTE = "INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason) VALUES (?, ?, ?, ?, ?)";
    private static final String ZERO_STOCK = "UPDATE Ingredient SET quantityInStock = 0 WHERE id = ?";

    public int recordExpiredIngredientWaste() {
        int affectedRows = 0;
        try (Connection connection = Mydatabase.getInstance().getConnection()) {
            boolean initialAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement expiredPs = connection.prepareStatement(EXPIRED_QUERY);
                 PreparedStatement insertWastePs = connection.prepareStatement(INSERT_WASTE);
                 PreparedStatement zeroStockPs = connection.prepareStatement(ZERO_STOCK);
                 ResultSet rs = expiredPs.executeQuery()) {

                while (rs.next()) {
                    long ingredientId = rs.getLong("id");
                    double quantityInStock = rs.getDouble("quantityInStock");
                    if (quantityInStock <= 0) {
                        continue;
                    }

                    insertWastePs.setLong(1, ingredientId);
                    insertWastePs.setDouble(2, quantityInStock);
                    insertWastePs.setString(3, "Expired");
                    insertWastePs.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    insertWastePs.setString(5, "Auto-recorded: ingredient expired and removed from stock");
                    insertWastePs.addBatch();

                    zeroStockPs.setLong(1, ingredientId);
                    zeroStockPs.addBatch();
                    affectedRows++;
                }

                if (affectedRows > 0) {
                    insertWastePs.executeBatch();
                    zeroStockPs.executeBatch();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException ignored) {
            return 0;
        }

        return affectedRows;
    }
}
