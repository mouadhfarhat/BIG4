package Controllers;

import Entities.Delivery;
import Entities.DeliveryDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DeliveryView {

    // ── Table ─────────────────────────────────────────────────────────────────
    @FXML private TableView<Delivery>              deliveryTable;
    @FXML private TableColumn<Delivery, Long>      idColumn;
    @FXML private TableColumn<Delivery, Long>      orderIdColumn;
    @FXML private TableColumn<Delivery, String>    nameColumn;
    @FXML private TableColumn<Delivery, String>    phoneColumn;
    @FXML private TableColumn<Delivery, String>    addressColumn;
    @FXML private TableColumn<Delivery, String>    statusColumn;
    @FXML private TableColumn<Delivery, String>    createdColumn;
    @FXML private TableColumn<Delivery, Void>      actionsColumn;
    @FXML private Label                            statsLabel;

    // ── Navbar buttons ────────────────────────────────────────────────────────
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

    // ── Delivery dropdown ─────────────────────────────────────────────────────
    private Popup deliveryPopup;

    private final ObservableList<Delivery> deliveryList    = FXCollections.observableArrayList();
    private final DateTimeFormatter        dateFormatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Stage getStage() { return (Stage) deliveryTable.getScene().getWindow(); }

    // ═════════════════════════════════════════════════════════════════════════
    // Init
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        buildDeliveryPopup();
        setupTable();
        loadDeliveries();
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
                buildPopupItem("➕", "Add Delivery",        "Create a new delivery", () -> navigateTo("/CreateDelivery.fxml",        "Add Delivery - Big4",       1400, 800)),
                buildPopupItem("🚴", "DeliveryMan View",    "Driver's interface",    () -> { /* already here */ })
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

    @FXML private void handleAbout()        { navigateTo("/homepage.fxml",     "Big4",                 1400, 800); }
    @FXML private void handleMenu()         { navigateTo("/Menu.fxml",          "Menu - Big4",          1400, 800); }
    @FXML private void handleReservations() { navigateTo("/Reservations.fxml",  "Reservations - Big4",  1400, 800); }
    @FXML private void handleEvents()       { navigateTo("/Events.fxml",        "Events - Big4",        1400, 800); }
    @FXML private void handleRapport()      { navigateTo("/Rapport.fxml",       "Reports - Big4",       1400, 800); }
    @FXML private void handleAdmin()        { navigateTo("/AdminDelivery.fxml",  "Admin - Big4",        1400, 800); }
    @FXML private void handleContact()      { navigateTo("/Contact.fxml",       "Contact - Big4",       1400, 800); }
    @FXML private void handleCart()         { navigateTo("/Cart.fxml",          "My Cart - Big4",       1400, 800); }
    @FXML private void handleProfile()      { navigateTo("/Profile.fxml",       "Profile - Big4",       1400,  800); }
    @FXML private void handleCall()         { showAlert("Call Big4",
            "Phone: +33 1 23 45 67 89\n\nMon–Fri: 10:00–23:00\nSat: 11:00–00:00\nSun: 11:00–23:00",
            Alert.AlertType.INFORMATION); }

    // ═════════════════════════════════════════════════════════════════════════
    // Table / data logic (unchanged)
    // ═════════════════════════════════════════════════════════════════════════

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("recipientPhone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        createdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(dateFormatter)));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Update Status");
            private final HBox   hbox      = new HBox(updateBtn);
            {
                updateBtn.setStyle("-fx-padding: 5; -fx-font-size: 10; -fx-background-color: #2563eb; -fx-text-fill: white; -fx-cursor: hand;");
                updateBtn.setOnAction(ev -> openStatusUpdateDialog(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        deliveryTable.setItems(deliveryList);
    }

    private void loadDeliveries() {
        new Thread(() -> {
            List<Delivery> deliveries = DeliveryDAO.getAllDeliveries();
            Platform.runLater(() -> { deliveryList.setAll(deliveries); updateStats(); });
        }).start();
    }

    private void openStatusUpdateDialog(Delivery delivery) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PENDING", "PENDING", "ON_DELIVERY", "DELIVERED", "CANCELED");
        dialog.setTitle("Update Delivery Status");
        dialog.setHeaderText("Update Status for Delivery #" + delivery.getDeliveryId());
        dialog.setContentText("Current Status: " + delivery.getStatus());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> updateStatus(delivery.getDeliveryId(), s));
    }

    private void updateStatus(Long deliveryId, String newStatus) {
        new Thread(() -> {
            boolean success = DeliveryDAO.updateDeliveryStatus(deliveryId, newStatus);
            Platform.runLater(() -> {
                if (success) { showAlert("Success", "Status updated to: " + newStatus, Alert.AlertType.INFORMATION); loadDeliveries(); }
                else showAlert("Error", "Failed to update status", Alert.AlertType.ERROR);
            });
        }).start();
    }

    @FXML
    private void refreshList() { loadDeliveries(); }

    private void updateStats() {
        int total      = deliveryList.size();
        int pending    = (int) deliveryList.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
        int onDelivery = (int) deliveryList.stream().filter(d -> "ON_DELIVERY".equals(d.getStatus())).count();
        int delivered  = (int) deliveryList.stream().filter(d -> "DELIVERED".equals(d.getStatus())).count();
        statsLabel.setText(String.format("Total: %d | Pending: %d | In Transit: %d | Delivered: %d",
                total, pending, onDelivery, delivered));
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