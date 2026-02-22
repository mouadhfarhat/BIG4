package Controllers;

import Entities.Delivery;
import Entities.DeliveryDAO;
import Entities.DeliveryMan;
import Services.DeliverymanService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CreateDelivery {

    // ── Form fields ───────────────────────────────────────────────────────────
    @FXML private TextField orderIdField;
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientPhoneField;
    @FXML private TextArea  deliveryAddressField;
    @FXML private TextField pickupLocationField;
    @FXML private TextArea  deliveryNotesField;

    // ── Navbar buttons ────────────────────────────────────────────────────────
    @FXML private Button aboutBtn;
    @FXML private Button menuBtn;
    @FXML private Button reserveBtn;
    @FXML private Button eventsBtn;
    @FXML private Button rapportBtn;
    @FXML private Button contactBtn;
    @FXML private Button deliveryBtn;
    @FXML private Button cartBtn;
    @FXML private Button profileBtn;
    @FXML private Button callBtn;

    // ── Delivery dropdown ─────────────────────────────────────────────────────
    private Popup deliveryPopup;

    // ── Any visible node used to grab the Stage for navigation ────────────────
    // (orderIdField is always present, so safe to use)
    private Stage getStage() {
        return (Stage) orderIdField.getScene().getWindow();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Init
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        buildDeliveryPopup();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Delivery popup (identical to homepage.java)
    // ═════════════════════════════════════════════════════════════════════════

    private void buildDeliveryPopup() {
        deliveryPopup = new Popup();
        deliveryPopup.setAutoHide(true);
        deliveryPopup.setAutoFix(true);

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

        Label sectionLabel = new Label("D E L I V E R Y");
        sectionLabel.setStyle(
                "-fx-text-fill: #FFA500;" +
                        "-fx-font-size: 9.5px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-padding: 4 20 8 20;" +
                        "-fx-opacity: 0.75;"
        );
        card.getChildren().add(sectionLabel);

        HBox line = new HBox();
        line.setStyle("-fx-background-color: rgba(255,165,0,0.18); -fx-pref-height: 1;");
        VBox.setMargin(line, new Insets(0, 14, 6, 14));
        card.getChildren().add(line);

        card.getChildren().addAll(
                buildPopupItem("📦", "Delivery Dashboard", "Overview & management", () -> navigateTo("/DeliverymanManagement.fxml", "Delivery Dashboard - Big4", 1400, 800)),
                buildPopupItem("➕", "Add Delivery",        "Create a new delivery", () -> { /* already here */ }),
                buildPopupItem("🚴", "DeliveryMan View",    "Driver's interface",    () -> navigateTo("/DeliveryView.fxml", "DeliveryMan View - Big4", 1400, 800))
        );

        deliveryPopup.getContent().add(card);
    }

    private HBox buildPopupItem(String emoji, String title, String subtitle, Runnable action) {
        Label icon = new Label(emoji);
        icon.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-background-color: rgba(255,165,0,0.13);" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 7 9 7 9;" +
                        "-fx-min-width: 38;" +
                        "-fx-alignment: CENTER;"
        );
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

        HBox row = new HBox(12, icon, text);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 18, 9, 14));
        row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 10;");

        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,165,0,0.09); -fx-background-radius: 10;");
            icon.setStyle("-fx-font-size: 17px; -fx-background-color: rgba(255,165,0,0.25); -fx-background-radius: 9; -fx-padding: 7 9 7 9; -fx-min-width: 38; -fx-alignment: CENTER;");
            titleLbl.setStyle("-fx-text-fill: #FFA500; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 10;");
            icon.setStyle("-fx-font-size: 17px; -fx-background-color: rgba(255,165,0,0.13); -fx-background-radius: 9; -fx-padding: 7 9 7 9; -fx-min-width: 38; -fx-alignment: CENTER;");
            titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        });
        row.setOnMouseClicked(e -> { deliveryPopup.hide(); action.run(); });

        return row;
    }

    @FXML
    private void handleDelivery() {
        if (deliveryPopup.isShowing()) {
            deliveryPopup.hide();
        } else {
            javafx.geometry.Bounds b = deliveryBtn.localToScreen(deliveryBtn.getBoundsInLocal());
            deliveryPopup.show(deliveryBtn.getScene().getWindow(), b.getMinX(), b.getMaxY() + 6);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Navbar handlers
    // ═════════════════════════════════════════════════════════════════════════

    @FXML private void handleAbout()        { navigateTo("/homepage.fxml",            "Big4",                  1400, 800); }
    @FXML private void handleMenu()         { navigateTo("/Menu.fxml",                "Menu - Big4",           1400, 800); }
    @FXML private void handleReservations() { navigateTo("/Reservations.fxml",        "Reservations - Big4",   1400, 800); }
    @FXML private void handleEvents()       { navigateTo("/Events.fxml",              "Events - Big4",         1400, 800); }
    @FXML private void handleRapport()      { navigateTo("/Rapport.fxml",             "Reports - Big4",        1400, 800); }
    @FXML private void handleContact()      { navigateTo("/Contact.fxml",             "Contact - Big4",        1400, 800); }
    @FXML private void handleCart()         { navigateTo("/Cart.fxml",                "My Cart - Big4",        1400, 800); }
    @FXML private void handleProfile()      { navigateTo("/Profile.fxml",             "Profile - Big4",        1400, 800); }
    @FXML private void handleCall()         { showAlert("Call Big4",
            "Phone: +33 1 23 45 67 89\n\nMon–Fri: 10:00–23:00\nSat: 11:00–00:00\nSun: 11:00–23:00",
            Alert.AlertType.INFORMATION); }

    // ═════════════════════════════════════════════════════════════════════════
    // Form logic (unchanged)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void createDelivery() {
        if (!validateForm()) return;

        new Thread(() -> {
            try {
                Delivery delivery = new Delivery(
                        Long.parseLong(orderIdField.getText()),
                        recipientNameField.getText(),
                        recipientPhoneField.getText(),
                        deliveryAddressField.getText()
                );
                delivery.setPickupLocation(pickupLocationField.getText().isEmpty() ? null : pickupLocationField.getText());
                delivery.setDeliveryNotes(deliveryNotesField.getText().isEmpty() ? null : deliveryNotesField.getText());

                DeliverymanService service = new DeliverymanService();
                List<DeliveryMan> allDeliveryMen = service.getAllDeliveryMen();
                Optional<DeliveryMan> available = allDeliveryMen.stream()
                        .filter(dm -> "ACTIVE".equals(dm.getStatus()) && !dm.getStatus().contains("ON_DELIVERY"))
                        .findFirst();
                if (available.isPresent()) delivery.setDeliveryManId(available.get().getDeliveryManId());

                boolean success = DeliveryDAO.createDelivery(delivery);

                Platform.runLater(() -> {
                    if (success) {
                        String msg = "Delivery created!\nID: " + delivery.getDeliveryId();
                        if (available.isPresent()) msg += "\nAssigned to: " + available.get().getName();
                        else msg += "\nNo available driver (will be assigned later)";
                        showAlert("Success", msg, Alert.AlertType.INFORMATION);
                        clearForm();
                    } else {
                        showAlert("Error", "Failed to create delivery", Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    @FXML
    public void clearForm() {
        orderIdField.clear();
        recipientNameField.clear();
        recipientPhoneField.clear();
        deliveryAddressField.clear();
        pickupLocationField.clear();
        deliveryNotesField.clear();
    }

    private boolean validateForm() {
        if (orderIdField.getText().trim().isEmpty()) { showAlert("Validation", "Please enter Order ID", Alert.AlertType.WARNING); return false; }
        try { Long.parseLong(orderIdField.getText()); }
        catch (NumberFormatException e) { showAlert("Validation", "Order ID must be a number", Alert.AlertType.WARNING); return false; }
        if (recipientNameField.getText().trim().isEmpty())   { showAlert("Validation", "Please enter recipient name",    Alert.AlertType.WARNING); return false; }
        if (recipientPhoneField.getText().trim().isEmpty())  { showAlert("Validation", "Please enter recipient phone",   Alert.AlertType.WARNING); return false; }
        if (deliveryAddressField.getText().trim().isEmpty()) { showAlert("Validation", "Please enter delivery address",  Alert.AlertType.WARNING); return false; }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getStage();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Error", "Navigation error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}