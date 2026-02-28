package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class homepage {

    @FXML private Button aboutBtn;
    @FXML private Button menuBtn;
    @FXML private Button reserveBtn;
    @FXML private Button eventsBtn;
    @FXML private Button rapportBtn;
    @FXML private Button contactBtn;
    @FXML private Button cartBtn;
    @FXML private Button adminInventoryBtn;
    @FXML private Button adminMenuBtn;
    @FXML private Button profileBtn;
    @FXML private Button callBtn;
    @FXML private Button viewMenuBtn;
    @FXML private Button reserveTableBtn;

    // =========================
    // NAVIGATION METHODS
    // =========================

    @FXML
    private void handleMenu(ActionEvent event) {
        loadScene("/menu.fxml", "Menu - Big4");
    }

    @FXML
    private void handleReservations(ActionEvent event) {
        loadScene("/reservation.fxml", "Reservations - Big4");
    }

    @FXML
    private void handleAdminInventory(ActionEvent event) {
        loadScene("/inventory-view.fxml", "Inventory Management");
    }

    @FXML
    private void handleAdminMenu(ActionEvent event) {
        loadScene("/menu-management.fxml", "Menu & Dish Management");
    }

    @FXML
    private void handleCart(ActionEvent event) {
        loadScene("/cart.fxml", "My Cart - Big4");
    }

    @FXML
    private void handleViewMenu(ActionEvent event) {
        loadScene("/menu.fxml", "Menu - Big4");
    }

    @FXML
    private void handleReserveTable(ActionEvent event) {
        loadScene("/reservation.fxml", "Reservation - Big4");
    }

    // =========================
    // ALERT PAGES
    // =========================

    @FXML
    private void handleAbout(ActionEvent event) {
        showAlert("About", "About Big4",
                "Big4 is a high-end gastronomic restaurant.\n\n" +
                        "Our mission: Where coffee excellence meets gastronomy.\n\n" +
                        "We offer refined cuisine with high-quality fresh products.");
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        showAlert("Events", "Upcoming Events",
                "• Wine Tasting Night - Friday 7 PM\n" +
                        "• Chef's Special Dinner - Saturday 8 PM\n" +
                        "• Brunch Event - Sunday 10 AM");
    }

    @FXML
    private void handleRapport(ActionEvent event) {
        showAlert("Reports", "Available Reports",
                "• Monthly Sales Report\n" +
                        "• Customer Feedback Summary\n" +
                        "• Restaurant Performance Analysis\n" +
                        "• Menu Popularity Report");
    }

    @FXML
    private void handleContact(ActionEvent event) {
        showAlert("Contact", "Contact Us",
                "Email: info@big4restaurant.com\n" +
                        "Phone: +33 1 23 45 67 89\n" +
                        "Address: 123 Gastronomic Street, Paris");
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        showAlert("Profile", "User Profile",
                "Profile page coming soon.");
    }

    @FXML
    private void handleCall(ActionEvent event) {
        showAlert("Call Big4", "Contact Information",
                "Phone: +33 1 23 45 67 89\n\nBusiness Hours: 10:00 - 23:00");
    }

    // =========================
    // CORE WINDOW LOADER (NEW WINDOW)
    // =========================

    private void loadScene(String fxmlPath, String windowTitle) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            Stage stage = new Stage(); // NEW WINDOW
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load page: " + fxmlPath);
        }
    }

    // =========================
    // ALERT HELPER
    // =========================

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
