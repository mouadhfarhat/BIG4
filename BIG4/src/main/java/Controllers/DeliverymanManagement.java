package Controllers;

import Entities.Car;
import Entities.DeliveryMan;
import Services.CarService;
import Services.DeliverymanService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DeliverymanManagement {

    // ── Table ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DeliveryMan>              deliveryTable;
    @FXML private TableColumn<DeliveryMan, Long>      idColumn;
    @FXML private TableColumn<DeliveryMan, String>    nameColumn;
    @FXML private TableColumn<DeliveryMan, String>    phoneColumn;
    @FXML private TableColumn<DeliveryMan, String>    emailColumn;
    @FXML private TableColumn<DeliveryMan, String>    vehicleTypeColumn;
    @FXML private TableColumn<DeliveryMan, String>    vehicleNumberColumn;
    @FXML private TableColumn<DeliveryMan, String>    assignedCarColumn;
    @FXML private TableColumn<DeliveryMan, String>    addressColumn;
    @FXML private TableColumn<DeliveryMan, String>    statusColumn;
    @FXML private TableColumn<DeliveryMan, LocalDate> joiningDateColumn;
    @FXML private TableColumn<DeliveryMan, Double>    salaryColumn;
    @FXML private TableColumn<DeliveryMan, Double>    ratingColumn;
    @FXML private TableColumn<DeliveryMan, Void>      actionsColumn;

    // ── Toolbar ───────────────────────────────────────────────────────────────
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label            totalCountLabel;
    @FXML private Label            activeCountLabel;
    @FXML private Label            avgRatingLabel;

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

    // ── Admin button (new) ────────────────────────────────────────────────────
    @FXML private Button adminDeliveryBtn;

    // ── Delivery dropdown ─────────────────────────────────────────────────────
    private Popup deliveryPopup;

    // ── Services ──────────────────────────────────────────────────────────────
    private DeliverymanService          deliverymanService;
    private CarService                  carService;
    private ObservableList<DeliveryMan> deliveryManList;
    private java.util.Map<Long, String> deliveryManIdToCarDisplay = new java.util.HashMap<>();

    // ═════════════════════════════════════════════════════════════════════════
    // Init
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        deliverymanService = new DeliverymanService();
        carService = new CarService();
        deliveryManList    = FXCollections.observableArrayList();

        buildDeliveryPopup();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryManId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        vehicleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
        vehicleNumberColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleNumber"));
        assignedCarColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                deliveryManIdToCarDisplay.getOrDefault(cell.getValue().getDeliveryManId(), "—")));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        joiningDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfJoining"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        addActionButtons();
        loadAllDeliveryMen();
        statusFilter.setValue("ALL");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Admin Delivery navigation (NEW)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAdminDelivery() {
        navigateTo("/AdminDelivery.fxml", "Admin Delivery - Big4", 1400, 800);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Delivery popup
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
                buildPopupItem("📦", "Delivery Dashboard", "Overview & management", () -> { /* already here */ }),
                buildPopupItem("➕", "Add Delivery",        "Create a new delivery", () -> navigateTo("/CreateDelivery.fxml",  "Add Delivery - Big4",      1400, 800)),
                buildPopupItem("🚗", "Fleet",              "Assign cars to drivers", () -> navigateTo("/FleetManagement.fxml", "Fleet - Big4",             900, 700)),
                buildPopupItem("🚴", "DeliveryMan View",    "Driver's interface",    () -> navigateTo("/DeliveryView.fxml",    "DeliveryMan View - Big4",  1400, 800))
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
    // Table logic (unchanged)
    // ═════════════════════════════════════════════════════════════════════════

    private void loadAllDeliveryMen() {
        try {
            deliveryManIdToCarDisplay.clear();
            List<Car> cars = carService.getAllCars();
            for (Car c : cars) {
                if (c.getDeliveryManId() != null)
                    deliveryManIdToCarDisplay.put(c.getDeliveryManId(), c.getMake() + " " + c.getModel() + " (" + c.getLicensePlate() + ")");
            }
            List<DeliveryMan> men = deliverymanService.getAllDeliveryMen();
            deliveryManList.setAll(men);
            deliveryTable.setItems(deliveryManList);
            updateStatistics();
        } catch (SQLException e) {
            showError("Error loading delivery men: " + e.getMessage());
        }
    }

    private void addActionButtons() {
        Callback<TableColumn<DeliveryMan, Void>, TableCell<DeliveryMan, Void>> cellFactory =
                param -> new TableCell<>() {
                    private final Button editBtn   = new Button("✏ Edit");
                    private final Button deleteBtn = new Button("🗑 Delete");
                    {
                        editBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 3;");
                        deleteBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 3;");
                        editBtn.setOnAction(ev   -> openEditDialog(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(ev -> confirmAndDelete(getTableView().getItems().get(getIndex())));
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) { setGraphic(null); return; }
                        HBox box = new HBox(8, editBtn, deleteBtn);
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                };
        actionsColumn.setCellFactory(cellFactory);
    }

    @FXML private void handleSearch() {
        String term   = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        try {
            List<DeliveryMan> filtered = deliverymanService.getAllDeliveryMen().stream()
                    .filter(dm -> dm.getName().toLowerCase().contains(term) ||
                            dm.getPhone().contains(term) ||
                            dm.getEmail().toLowerCase().contains(term))
                    .filter(dm -> status == null || status.equals("ALL") || dm.getStatus().equals(status))
                    .collect(Collectors.toList());
            deliveryManList.setAll(filtered);
            updateStatistics();
        } catch (SQLException e) { showError("Error searching: " + e.getMessage()); }
    }

    @FXML private void handleFilterByStatus() { handleSearch(); }

    @FXML private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue("ALL");
        loadAllDeliveryMen();
    }

    @FXML private void handleAddNew() { openAddDialog(); }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEditDeliveryManDialog.fxml"));
            Parent root = loader.load();
            AddEditDeliveryManDialog ctrl = loader.getController();
            ctrl.setMode("ADD");
            ctrl.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Delivery Man");
            stage.setScene(new Scene(root));
            stage.setWidth(700); stage.setHeight(700);
            stage.show();
        } catch (IOException e) { showError("Error opening add dialog: " + e.getMessage()); }
    }

    private void openEditDialog(DeliveryMan dm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEditDeliveryManDialog.fxml"));
            Parent root = loader.load();
            AddEditDeliveryManDialog ctrl = loader.getController();
            ctrl.setMode("EDIT");
            ctrl.setDeliveryMan(dm);
            ctrl.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Edit Delivery Man");
            stage.setScene(new Scene(root));
            stage.setWidth(700); stage.setHeight(700);
            stage.show();
        } catch (IOException e) { showError("Error opening edit dialog: " + e.getMessage()); }
    }

    private void confirmAndDelete(DeliveryMan dm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Delivery Man");
        alert.setContentText("Are you sure you want to delete " + dm.getName() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                deliverymanService.deleteDeliveryMan(dm.getDeliveryManId());
                loadAllDeliveryMen();
                showSuccess("Delivery man deleted successfully");
            } catch (SQLException e) { showError("Error deleting: " + e.getMessage()); }
        }
    }

    private void updateStatistics() {
        try {
            totalCountLabel.setText(String.valueOf(deliverymanService.countDeliveryMen()));
            activeCountLabel.setText(String.valueOf(deliverymanService.getActiveDeliveryMen().size()));
            double avg = deliveryManList.stream()
                    .filter(dm -> dm.getRating() != null)
                    .mapToDouble(DeliveryMan::getRating)
                    .average().orElse(0.0);
            avgRatingLabel.setText(String.format("%.2f ⭐", avg));
        } catch (SQLException e) { showError("Error updating statistics: " + e.getMessage()); }
    }

    public void refreshTable() { loadAllDeliveryMen(); }

    // ═════════════════════════════════════════════════════════════════════════
    // Navbar handlers
    // ═════════════════════════════════════════════════════════════════════════

    @FXML private void handleAbout()        { navigateTo("/homepage.fxml",    "Big4",                 1400, 800); }
    @FXML private void handleMenu()         { navigateTo("/Menu.fxml",         "Menu - Big4",          1400, 800); }
    @FXML private void handleReservations() { navigateTo("/Reservations.fxml", "Reservations - Big4",  1400, 800); }
    @FXML private void handleEvents()       { navigateTo("/Events.fxml",       "Events - Big4",        1400, 800); }
    @FXML private void handleRapport()      { navigateTo("/Rapport.fxml",      "Reports - Big4",       1400, 800); }
    @FXML private void handleContact()      { navigateTo("/Contact.fxml",      "Contact - Big4",       1400, 800); }
    @FXML private void handleCart()         { navigateTo("/Cart.fxml",         "My Cart - Big4",       1400, 800); }
    @FXML private void handleProfile()      { navigateTo("/profile.fxml",      "My Profile - Big4",    900, 700); }
    @FXML private void handleCall()         { showInfo("Call feature not yet implemented"); }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    private void navigateTo(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) deliveryTable.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (IOException e) { showError("Navigation error: " + e.getMessage()); }
    }

    private void showError(String msg)   { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle("Error");       a.setContentText(msg); a.showAndWait(); }
    private void showSuccess(String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Success");     a.setContentText(msg); a.showAndWait(); }
    private void showInfo(String msg)    { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Information"); a.setContentText(msg); a.showAndWait(); }
}