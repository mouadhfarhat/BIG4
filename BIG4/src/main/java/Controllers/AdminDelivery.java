package Controllers;

import Entities.Delivery;
import Services.DeliveryService;
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
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import Services.AuthService;
import java.util.stream.Collectors;

/**
 * Controller for the Admin Delivery Management page.
 * Handles viewing, searching, filtering, and deleting deliveries.
 */
public class AdminDelivery {

    // ══════════════════════════════════════════════════════════════════════════
    // Table Components
    // ══════════════════════════════════════════════════════════════════════════

    @FXML private TableView<Delivery> deliveryTable;
    @FXML private TableColumn<Delivery, Long> idColumn;
    @FXML private TableColumn<Delivery, Long> orderIdColumn;
    @FXML private TableColumn<Delivery, String> deliveryManColumn;
    @FXML private TableColumn<Delivery, String> nameColumn;
    @FXML private TableColumn<Delivery, String> phoneColumn;
    @FXML private TableColumn<Delivery, String> addressColumn;
    @FXML private TableColumn<Delivery, String> statusColumn;
    @FXML private TableColumn<Delivery, LocalDateTime> createdColumn;
    @FXML private TableColumn<Delivery, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label onDeliveryLabel;
    @FXML private Label deliveredLabel;
    @FXML private Label canceledLabel;

    // ══════════════════════════════════════════════════════════════════════════
    // Navbar Buttons
    // ══════════════════════════════════════════════════════════════════════════

    @FXML private Button aboutBtn;
    @FXML private Button menuBtn;
    @FXML private Button reserveBtn;
    @FXML private Button eventsBtn;
    @FXML private Button rapportBtn;
    @FXML private Button contactBtn;
    @FXML private Button deliveryBtn;
    @FXML private Button cartBtn;
    @FXML private Button profileBtn;
    @FXML private Button manageUsersBtn;
    @FXML private Button fleetBtn;
    @FXML private Button callBtn;
    @FXML private Button logoutBtn;

    // ══════════════════════════════════════════════════════════════════════════
    // Services and Data
    // ══════════════════════════════════════════════════════════════════════════

    private DeliveryService deliveryService;
    private ObservableList<Delivery> deliveryList;
    private DateTimeFormatter dateFormatter;
    private Popup deliveryPopup;

    // ══════════════════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        deliveryService = new DeliveryService();
        deliveryList = FXCollections.observableArrayList();
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        buildDeliveryPopup();
        setupTableColumns();
        addActionButtons();
        loadAllDeliveries();

        if (statusFilter != null) {
            statusFilter.setValue("ALL");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Delivery Dropdown Popup (Same as DeliveryView)
    // ══════════════════════════════════════════════════════════════════════════

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
                buildPopupItem("📦", "Admin Dashboard", "Management & overview", () -> { /* already here */ }),
                buildPopupItem("➕", "Add Delivery", "Create a new delivery", () -> navigateTo("/CreateDelivery.fxml", "Add Delivery - Big4", 1200, 800)),
                buildPopupItem("🚴", "DeliveryMan View", "Driver's interface", () -> navigateTo("/DeliveryView.fxml", "DeliveryMan View - Big4", 1250, 800))
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

        row.setOnMouseClicked(e -> {
            deliveryPopup.hide();
            action.run();
        });

        return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Table Setup
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Setup table columns with PropertyValueFactory
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        // Delivery man - show ID since name not in entity
        deliveryManColumn.setCellValueFactory(cellData -> {
            Long deliveryManId = cellData.getValue().getDeliveryManId();
            if (deliveryManId != null) {
                return new javafx.beans.property.SimpleStringProperty("ID: " + deliveryManId);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("recipientPhone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format created date column
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdColumn.setCellFactory(column -> new TableCell<Delivery, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        // Style status column with colors
        statusColumn.setCellFactory(column -> new TableCell<Delivery, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "ON_DELIVERY":
                            setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                            break;
                        case "DELIVERED":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "CANCELED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Load all deliveries from database
     */
    private void loadAllDeliveries() {
        try {
            List<Delivery> deliveries = deliveryService.getAllDeliveries();
            deliveryList.setAll(deliveries);
            deliveryTable.setItems(deliveryList);
            updateStatistics();
        } catch (SQLException e) {
            showError("Error loading deliveries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add Delete button to actions column
     */
    private void addActionButtons() {
        Callback<TableColumn<Delivery, Void>, TableCell<Delivery, Void>> cellFactory =
                new Callback<TableColumn<Delivery, Void>, TableCell<Delivery, Void>>() {
                    @Override
                    public TableCell<Delivery, Void> call(final TableColumn<Delivery, Void> param) {
                        return new TableCell<Delivery, Void>() {
                            private final Button deleteBtn = new Button("🗑 Delete");

                            {
                                deleteBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 3;");

                                deleteBtn.setOnAction(event -> {
                                    Delivery selected = getTableView().getItems().get(getIndex());
                                    confirmAndDelete(selected);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    HBox container = new HBox(8);
                                    container.setAlignment(Pos.CENTER);
                                    container.getChildren().add(deleteBtn);
                                    setGraphic(container);
                                }
                            }
                        };
                    }
                };
        actionsColumn.setCellFactory(cellFactory);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Search and Filter
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Handle search functionality
     */
    @FXML
    private void performSearch() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        String statusFilterValue = statusFilter.getValue();

        try {
            List<Delivery> allDeliveries;

            if (!searchTerm.isEmpty()) {
                allDeliveries = deliveryService.searchDeliveries(searchTerm);
            } else {
                allDeliveries = deliveryService.getAllDeliveries();
            }

            // Apply status filter
            if (statusFilterValue != null && !statusFilterValue.equals("ALL")) {
                allDeliveries = allDeliveries.stream()
                        .filter(d -> d.getStatus().equals(statusFilterValue))
                        .collect(Collectors.toList());
            }

            deliveryList.setAll(allDeliveries);
            updateStatistics();
        } catch (SQLException e) {
            showError("Error searching deliveries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle status filter
     */
    @FXML
    private void filterByStatus() {
        performSearch();
    }

    /**
     * Refresh data
     */
    @FXML
    private void refreshData() {
        searchField.clear();
        statusFilter.setValue("ALL");
        loadAllDeliveries();
        showInfo("Data refreshed successfully");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Delete Functionality
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Confirm and delete delivery
     */
    private void confirmAndDelete(Delivery delivery) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Delivery");
        alert.setContentText("Are you sure you want to delete delivery #" + delivery.getDeliveryId() +
                " for " + delivery.getRecipientName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                deliveryService.deleteDelivery(delivery.getDeliveryId());
                loadAllDeliveries();
                showSuccess("Delivery deleted successfully");
            } catch (SQLException e) {
                showError("Error deleting delivery: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        try {
            List<Delivery> allDeliveries = deliveryService.getAllDeliveries();

            int total = allDeliveries.size();
            int pending = (int) allDeliveries.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
            int onDelivery = (int) allDeliveries.stream().filter(d -> "ON_DELIVERY".equals(d.getStatus())).count();
            int delivered = (int) allDeliveries.stream().filter(d -> "DELIVERED".equals(d.getStatus())).count();
            int canceled = (int) allDeliveries.stream().filter(d -> "CANCELED".equals(d.getStatus())).count();

            if (totalLabel != null) {
                totalLabel.setText(String.valueOf(total));
            }
            if (pendingLabel != null) {
                pendingLabel.setText(String.valueOf(pending));
            }
            if (onDeliveryLabel != null) {
                onDeliveryLabel.setText(String.valueOf(onDelivery));
            }
            if (deliveredLabel != null) {
                deliveredLabel.setText(String.valueOf(delivered));
            }
            if (canceledLabel != null) {
                canceledLabel.setText(String.valueOf(canceled));
            }
        } catch (SQLException e) {
            showError("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Navigation Handlers (Same as DeliveryView)
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleDelivery() {
        if (deliveryPopup.isShowing()) {
            deliveryPopup.hide();
        } else {
            javafx.geometry.Bounds b = deliveryBtn.localToScreen(deliveryBtn.getBoundsInLocal());
            deliveryPopup.show(deliveryBtn.getScene().getWindow(), b.getMinX(), b.getMaxY() + 6);
        }
    }

    @FXML
    private void handleAbout() {
        navigateTo("/homepage.fxml", "Big4", 1250, 800);
    }

    @FXML
    private void handleMenu() {
        navigateTo("/Menu.fxml", "Menu - Big4", 1200, 800);
    }

    @FXML
    private void handleReservations() {
        navigateTo("/Reservations.fxml", "Reservations - Big4", 1200, 800);
    }

    @FXML
    private void handleEvents() {
        navigateTo("/Events.fxml", "Events - Big4", 1200, 800);
    }

    @FXML
    private void handleRapport() {
        navigateTo("/Rapport.fxml", "Reports - Big4", 1200, 800);
    }

    @FXML
    private void handleContact() {
        navigateTo("/Contact.fxml", "Contact - Big4", 1200, 800);
    }

    @FXML
    private void handleCart() {
        navigateTo("/Cart.fxml", "My Cart - Big4", 1200, 800);
    }

    @FXML
    private void handleProfile() {
        navigateTo("/profile.fxml", "My Profile - Big4", 900, 700);
    }

    @FXML
    private void handleManageUsers() {
        navigateTo("/UserManagement.fxml", "Manage users - Big4", 900, 700);
    }

    @FXML
    private void handleFleet() {
        navigateTo("/FleetManagement.fxml", "Fleet - Assign car to delivery man - Big4", 900, 700);
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) deliveryTable.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("Big4 - Login");
        } catch (IOException e) {
            showError("Could not open login: " + e.getMessage());
        }
    }

    @FXML
    private void handleCall() {
        showAlert("Call Big4",
                "Phone: +33 1 23 45 67 89\n\nMon–Fri: 10:00–23:00\nSat: 11:00–00:00\nSun: 11:00–23:00",
                Alert.AlertType.INFORMATION);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Navigation helper
     */
    private void navigateTo(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) deliveryTable.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Alert methods
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}