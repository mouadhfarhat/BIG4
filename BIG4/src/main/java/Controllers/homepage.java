package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.io.IOException;


public class homepage {

    @FXML private Button aboutBtn;
    @FXML private Button menuBtn;
    @FXML private Button reserveBtn;
    @FXML private Button eventsBtn;
    @FXML private Button rapportBtn;
    @FXML private Button adminBtn;
    @FXML private Button contactBtn;
    @FXML private Button deliveryBtn;
    @FXML private Button cartBtn;
    @FXML private Button profileBtn;
    @FXML private Button callBtn;
    @FXML private Button viewMenuBtn;
    @FXML private Button reserveTableBtn;

    // We use a plain Popup instead of ContextMenu for full visual control
    private Popup deliveryPopup;

    @FXML
    public void initialize() {
        buildDeliveryPopup();
    }

    // ── Build popup ───────────────────────────────────────────────────────────

    private void buildDeliveryPopup() {
        deliveryPopup = new Popup();
        deliveryPopup.setAutoHide(true);   // closes when clicking anywhere else
        deliveryPopup.setAutoFix(true);

        // ── Outer card ────────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: rgb(5, 14, 42);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: rgba(255,165,0,0.30);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.60), 24, 0.25, 0, 8);" +
                        "-fx-padding: 10 0 10 0;" +
                        "-fx-min-width: 250;"
        );

        // ── Section header ────────────────────────────────────────────────────
        Label sectionLabel = new Label("D E L I V E R Y");
        sectionLabel.setStyle(
                "-fx-text-fill: #FFA500;" +
                        "-fx-font-size: 9.5px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-padding: 4 20 8 20;" +
                        "-fx-opacity: 0.75;"
        );
        card.getChildren().add(sectionLabel);

        // Hairline divider
        HBox line = new HBox();
        line.setStyle("-fx-background-color: rgba(255,165,0,0.18); -fx-pref-height: 1;");
        VBox.setMargin(line, new Insets(0, 14, 6, 14));
        card.getChildren().add(line);

        // ── Items ─────────────────────────────────────────────────────────────
        card.getChildren().addAll(
                buildItem("📦", "Delivery Dashboard", "Overview & management",  () -> loadDeliveryDashboard()),
                buildItem("➕", "Add Delivery",        "Create a new delivery",  () -> loadAddDelivery()),
                buildItem("🚴", "DeliveryMan View",    "Driver's interface",     () -> loadDeliveryManView())
        );

        deliveryPopup.getContent().add(card);
    }

    private HBox buildItem(String emoji, String title, String subtitle, Runnable action) {

        // Icon bubble
        Label icon = new Label(emoji);
        icon.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-background-color: rgba(255,165,0,0.13);" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 7 9 7 9;" +
                        "-fx-min-width: 38;" +
                        "-fx-alignment: CENTER;"
        );

        // Text
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-family: 'Arial';"
        );
        Label subLbl = new Label(subtitle);
        subLbl.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.40);" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-family: 'Arial';"
        );
        VBox text = new VBox(2, titleLbl, subLbl);
        text.setAlignment(Pos.CENTER_LEFT);

        // Row
        HBox row = new HBox(12, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 18, 9, 14));
        row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 10;");

        // Hover states
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,165,0,0.09); -fx-background-radius: 10;");
            icon.setStyle(
                    "-fx-font-size: 17px;" +
                            "-fx-background-color: rgba(255,165,0,0.25);" +
                            "-fx-background-radius: 9;" +
                            "-fx-padding: 7 9 7 9;" +
                            "-fx-min-width: 38;" +
                            "-fx-alignment: CENTER;"
            );
            titleLbl.setStyle(
                    "-fx-text-fill: #FFA500;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-family: 'Arial';"
            );
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 10;");
            icon.setStyle(
                    "-fx-font-size: 17px;" +
                            "-fx-background-color: rgba(255,165,0,0.13);" +
                            "-fx-background-radius: 9;" +
                            "-fx-padding: 7 9 7 9;" +
                            "-fx-min-width: 38;" +
                            "-fx-alignment: CENTER;"
            );
            titleLbl.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-family: 'Arial';"
            );
        });

        // Click → hide popup then run action
        row.setOnMouseClicked(e -> {
            deliveryPopup.hide();
            action.run();
        });

        return row;
    }

    // ── Delivery button ───────────────────────────────────────────────────────

    @FXML
    private void handleDelivery(ActionEvent event) {
        if (deliveryPopup.isShowing()) {
            deliveryPopup.hide();
        } else {
            // Calculate screen position: directly below the Delivery button
            javafx.geometry.Bounds bounds = deliveryBtn.localToScreen(deliveryBtn.getBoundsInLocal());
            double x = bounds.getMinX();
            double y = bounds.getMaxY() + 6;   // 6px gap below the button
            deliveryPopup.show(deliveryBtn.getScene().getWindow(), x, y);
        }
    }

    // ── Delivery loaders ──────────────────────────────────────────────────────

    private void loadDeliveryDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeliverymanManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) deliveryBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Delivery Dashboard - Big4");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load Delivery Dashboard.\n" +
                            "Make sure DeliverymanManagement.fxml is in src/main/resources/\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void loadAddDelivery() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateDelivery.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) deliveryBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Add Delivery - Big4");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load Add Delivery form.\n" +
                            "Make sure AddDelivery.fxml is in src/main/resources/\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void loadDeliveryManView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeliveryView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) deliveryBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("DeliveryMan View - Big4");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load DeliveryMan View.\n" +
                            "Make sure DeliveryManView.fxml is in src/main/resources/\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    // ── Existing handlers (unchanged) ─────────────────────────────────────────

    @FXML
    private void handleAbout(ActionEvent event) {
        showAlert("About", "About Big4",
                "Big4 is a high-end gastronomic restaurant.\n\n" +
                        "Our mission: Where coffee excellence meets gastronomy.\n\n" +
                        "We offer refined cuisine with high-quality fresh products.");
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        loadScene("menu.fxml", "Menu - Big4", 1400, 800);
    }

    @FXML
    private void handleReservations(ActionEvent event) {
        loadScene("reservation.fxml", "Reservations - Big4", 1400, 800);
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        showAlert("Events", "Upcoming Events",
                "Welcome to our Events section!\n\n" +
                        "Current Events:\n" +
                        "• Wine Tasting Night - Friday 7 PM\n" +
                        "• Chef's Special Dinner - Saturday 8 PM\n" +
                        "• Brunch Event - Sunday 10 AM\n\n" +
                        "Check back soon for more events!");
    }

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

    @FXML
    private void handleAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDelivery.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Admin Delivery - Big4");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Unable to open Admin dashboard.\n\nError: " + e.getMessage());
        }
    }

    @FXML
    private void handleContact(ActionEvent event) {
        showAlert("Contact", "Contact Us",
                "Email: info@big4restaurant.com\n" +
                        "Phone: +33 1 23 45 67 89\n" +
                        "Address: 123 Gastronomic Street, Paris 75001\n" +
                        "Website: www.big4restaurant.com");
    }

    @FXML
    private void handleCart(ActionEvent event) {
        loadScene("cart.fxml", "My Cart - Big4", 1400, 800);
    }

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

    @FXML
    private void handleCall(ActionEvent event) {
        showAlert("Call Big4", "Contact Information",
                "Phone: +33 1 23 45 67 89\n\n" +
                        "Business Hours:\n" +
                        "Monday - Friday: 10:00 AM - 11:00 PM\n" +
                        "Saturday: 11:00 AM - 12:00 AM\n" +
                        "Sunday: 11:00 AM - 11:00 PM");
    }

    @FXML
    private void handleViewMenu(ActionEvent event) {
        loadScene("menu.fxml", "Menu - Big4", 1400, 800);
    }

    @FXML
    private void handleReserveTable(ActionEvent event) {
        loadScene("reservation.fxml", "Reservation - Big4", 1400, 800);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private void loadScene(String fxmlPath, String windowTitle, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root, width, height));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Loading Error",
                    "Unable to load page: " + fxmlPath + "\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}