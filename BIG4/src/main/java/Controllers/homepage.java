package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;


public class homepage {

    @FXML
    private Button aboutBtn;

    @FXML
    private Button menuBtn;

    @FXML
    private Button reserveBtn;

    @FXML
    private Button eventsBtn;

    @FXML
    private Button rapportBtn;

    @FXML
    private Button contactBtn;

    @FXML
    private Button cartBtn;

    @FXML
    private Button profileBtn;

    @FXML
    private Button callBtn;

    @FXML
    private Button viewMenuBtn;

    @FXML
    private Button reserveTableBtn;

    /**
     * Handles the "About" button click
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        showAlert("About", "About Big4",
                "Big4 is a high-end gastronomic restaurant.\n\n" +
                        "Our mission: Where coffee excellence meets gastronomy.\n\n" +
                        "We offer refined cuisine with high-quality fresh products.");
    }

    /**
     * Handles the "Menu" navigation button click
     */
    @FXML
    private void handleMenu(ActionEvent event) {
        loadScene("resources/menu.fxml", "Menu - Big4");
    }

    /**
     * Handles the "Reservations" button click
     */
    @FXML
    private void handleReservations(ActionEvent event) {
        loadScene("resources/reservation.fxml", "Reservations - Big4");
    }

    /**
     * Handles the "Events" button click - Opens Food Donation Events page
     */
    @FXML
    private void handleEvents(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/food-donation-events.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading food donation page: " + e.getMessage());
            e.printStackTrace();
        }
    }





    /**
     * Handles the "Reports" button click
     */
    @FXML
    private void handleRapport(ActionEvent event) {
        showAlert("Reports", "Our Reports",
                "Welcome to our Reports section!\n\n" +
                        "Available Reports:\n" +
                        "• Monthly Sales Report\n" +
                        "• Customer Feedback Summary\n" +
                        "• Restaurant Performance Analysis\n" +
                        "• Menu Popularity Report\n\n" +
                        "Reports are generated and updated regularly.");
    }

    /**
     * Handles the "Contact" button click
     */
    @FXML
    private void handleContact(ActionEvent event) {
        showAlert("Contact", "Contact Us",
                "Email: info@big4restaurant.com\n" +
                        "Phone: +33 1 23 45 67 89\n" +
                        "Address: 123 Gastronomic Street, Paris 75001\n" +
                        "Website: www.big4restaurant.com");
    }

    /**
     * Handles the cart button click
     */
    @FXML
    private void handleCart(ActionEvent event) {
        loadScene("resources/cart.fxml", "My Cart - Big4");
    }

    /**
     * Handles the profile button click
     */
    @FXML
    private void handleProfile(ActionEvent event) {
        showAlert("Profile", "User Profile",
                "Welcome to your profile!\n\n" +
                        "Profile Information:\n" +
                        "• Name: [Your Name]\n" +
                        "• Email: your.email@example.com\n" +
                        "• Phone: Your Phone Number\n" +
                        "• Member Since: 2024\n\n" +
                        "Edit your profile in the profile page.");
    }

    /**
     * Handles the call button click
     */
    @FXML
    private void handleCall(ActionEvent event) {
        showAlert("Call Big4", "Contact Information",
                "Phone: +33 1 23 45 67 89\n\n" +
                        "Business Hours:\n" +
                        "Monday - Friday: 10:00 AM - 11:00 PM\n" +
                        "Saturday: 11:00 AM - 12:00 AM\n" +
                        "Sunday: 11:00 AM - 11:00 PM");
    }

    /**
     * Handles the "View Menu" button click
     */
    @FXML
    private void handleViewMenu(ActionEvent event) {
        loadScene("resources/menu.fxml", "Menu - Big4");
    }

    /**
     * Handles the "Reserve a Table" button click
     */
    @FXML
    private void handleReserveTable(ActionEvent event) {
        loadScene("resources/reservation.fxml", "Reservation - Big4");
    }

    /**
     * Helper method to load a new scene
     */
    private void loadScene(String fxmlPath, String windowTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load page: " + fxmlPath);
        }
    }

    /**
     * Helper method to show alert dialogs
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
