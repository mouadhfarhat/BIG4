package Controllers;

import Utils.Mydatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private Label totalIngredientsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label expiredItemsLabel;
    @FXML private Label wasteRecordsLabel;
    @FXML private Label inventoryValueLabel;
    @FXML private Label totalWasteQtyLabel;

    @FXML private PieChart wasteTypePieChart;
    @FXML private PieChart stockStatusPieChart;
    @FXML private BarChart<String, Number> topWastedBarChart;

    private final Mydatabase database = Mydatabase.getInstance();

    @FXML
    public void initialize() {
        loadStatistics();
        loadWasteTypeChart();
        loadStockStatusChart();
        loadTopWastedChart();
    }

    private void loadStatistics() {
        totalIngredientsLabel.setText(String.valueOf(fetchInt("SELECT COUNT(*) FROM Ingredient")));
        lowStockLabel.setText(String.valueOf(fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock <= minStockLevel")));
        expiredItemsLabel.setText(String.valueOf(fetchInt("SELECT COUNT(*) FROM Ingredient WHERE expiryDate IS NOT NULL AND expiryDate < CURDATE()")));
        wasteRecordsLabel.setText(String.valueOf(fetchInt("SELECT COUNT(*) FROM WasteRecord")));

        double inventoryValue = fetchDouble("SELECT COALESCE(SUM(quantityInStock * unitCost), 0) FROM Ingredient");
        inventoryValueLabel.setText(String.format("$%.2f", inventoryValue));

        double wasteQty = fetchDouble("SELECT COALESCE(SUM(quantityWasted), 0) FROM WasteRecord");
        totalWasteQtyLabel.setText(String.format("%.2f", wasteQty));
    }

    private void loadWasteTypeChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String sql = "SELECT COALESCE(wasteType, 'Unknown') AS type, COUNT(*) AS total FROM WasteRecord GROUP BY wasteType ORDER BY total DESC";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int total = resultSet.getInt("total");
                data.add(new PieChart.Data(type, total));
            }
        } catch (SQLException e) {
            data.clear();
        }

        wasteTypePieChart.setData(data);
    }

    private void loadStockStatusChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        int healthy = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock > minStockLevel AND (expiryDate IS NULL OR expiryDate >= CURDATE())");
        int low = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock <= minStockLevel AND (expiryDate IS NULL OR expiryDate >= CURDATE())");
        int expired = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE expiryDate IS NOT NULL AND expiryDate < CURDATE()");

        data.add(new PieChart.Data("Healthy", healthy));
        data.add(new PieChart.Data("Low Stock", low));
        data.add(new PieChart.Data("Expired", expired));

        stockStatusPieChart.setData(data);
    }

    private void loadTopWastedChart() {
        topWastedBarChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Wasted Quantity");

        String sql = "SELECT COALESCE(i.name, CONCAT('Ingredient #', w.ingredientId)) AS ingredientName, " +
                "COALESCE(SUM(w.quantityWasted), 0) AS totalWasted " +
                "FROM WasteRecord w " +
                "LEFT JOIN Ingredient i ON i.id = w.ingredientId " +
                "GROUP BY ingredientName " +
                "ORDER BY totalWasted DESC " +
                "LIMIT 8";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(
                        resultSet.getString("ingredientName"),
                        resultSet.getDouble("totalWasted")
                ));
            }
        } catch (SQLException e) {
            series.getData().clear();
        }

        topWastedBarChart.getData().add(series);
    }

    private int fetchInt(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    private double fetchDouble(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
