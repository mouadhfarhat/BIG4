package Controllers;

import Entities.DonationDishRecommendation;
import Entities.Fooddonationevent;
import Entities.FoodDonationItem;
import Services.Fooddonationeventservice;
import Services.FoodDonationItemService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FoodDonationController {

    private static final int DEFAULT_NEAR_EXPIRY_DAYS = 3;

    // ==================== DONATION ITEMS COMPONENTS ====================
    @FXML private TableView<FoodDonationItem> donationItemsTable;
    @FXML private TableColumn<FoodDonationItem, Number> itemEventIdColumn;
    @FXML private TableColumn<FoodDonationItem, Number> itemIdColumn;
    @FXML private TableColumn<FoodDonationItem, String> itemNameColumn;
    @FXML private TableColumn<FoodDonationItem, Number> itemQuantityColumn;

    @FXML private TextField itemEventIdField;
    @FXML private TextField itemDishIdField;
    @FXML private TextField itemQuantityField;

    @FXML private Label totalItemsLabel;
    @FXML private Label totalQuantityLabel;

    // ==================== DONATION EVENTS COMPONENTS ====================
    @FXML private TableView<Fooddonationevent> donationEventsTable;
    @FXML private TableColumn<Fooddonationevent, Number> donationEventIdColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationEventDateColumn;
    @FXML private TableColumn<Fooddonationevent, Number> donationTotalQuantityColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationCharityNameColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationStatusColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationDeliveryIdColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationCreatedAtColumn;
    @FXML private TableColumn<Fooddonationevent, String> donationUpdatedAtColumn;

    @FXML private DatePicker donationEventDatePicker;
    @FXML private TextField donationTotalQuantityField;
    @FXML private TextField donationCharityNameField;
    @FXML private ComboBox<String> donationStatusCombo;
    @FXML private ComboBox<String> donationDeliveryCombo;

    @FXML private Label totalEventsLabel;
    @FXML private Label pendingEventsLabel;
    @FXML private Label completedEventsLabel;

    // ==================== OPTIMIZATION PLANNER VIEW ====================
    @FXML private TableView<Fooddonationevent> optimizationEventsTable;
    @FXML private TableColumn<Fooddonationevent, Number> optEventIdColumn;
    @FXML private TableColumn<Fooddonationevent, String> optEventDateColumn;
    @FXML private TableColumn<Fooddonationevent, String> optEventCharityColumn;
    @FXML private TableColumn<Fooddonationevent, String> optEventStatusColumn;

    @FXML private TableView<DonationDishRecommendation> recommendationTable;
    @FXML private TableColumn<DonationDishRecommendation, Number> recDishIdColumn;
    @FXML private TableColumn<DonationDishRecommendation, String> recDishNameColumn;
    @FXML private TableColumn<DonationDishRecommendation, Number> recMaxCountColumn;
    @FXML private TableColumn<DonationDishRecommendation, String> recUsageScoreColumn;
    @FXML private TableColumn<DonationDishRecommendation, String> recCostScoreColumn;
    @FXML private TextField plannerDishQuantityField;

    // ==================== SERVICES AND DATA ====================
    private final FoodDonationItemService itemService = new FoodDonationItemService();
    private final Fooddonationeventservice eventService = new Fooddonationeventservice();

    private final ObservableList<FoodDonationItem> donationItems = FXCollections.observableArrayList();
    private final ObservableList<Fooddonationevent> donationEvents = FXCollections.observableArrayList();
    private final ObservableList<DonationDishRecommendation> dishRecommendations = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        setupItemsTable();
        setupEventsTable();
        setupOptimizationView();
        loadAllData();
    }

    // ==================== SETUP METHODS ====================

    private void setupItemsTable() {
        itemEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("donationEventId"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        donationItemsTable.setItems(donationItems);

        // Auto-populate form on selection
        donationItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                itemEventIdField.setText(String.valueOf(newSel.getDonationEventId()));
                itemDishIdField.setText(String.valueOf(newSel.getItemId()));
                itemQuantityField.setText(String.valueOf(newSel.getQuantity()));
            }
        });
    }

    private void setupEventsTable() {
        // Event ID
        donationEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("donationEventId"));

        // Event Date
        donationEventDateColumn.setCellValueFactory(cell -> {
            Date eventDate = cell.getValue().getEventDate();
            String display = eventDate != null ? eventDate.toLocalDate().format(dateFormatter) : "N/A";
            return new SimpleStringProperty(display);
        });

        // Total Quantity
        donationTotalQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));

        // Charity Name
        donationCharityNameColumn.setCellValueFactory(new PropertyValueFactory<>("charityName"));

        // Status
        donationStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Delivery ID (shows NULL if not assigned)
        donationDeliveryIdColumn.setCellValueFactory(cell -> {
            Long deliveryId = cell.getValue().getDeliveryId();
            String display = deliveryId != null ? String.valueOf(deliveryId) : "NULL";
            return new SimpleStringProperty(display);
        });

        // Created At
        donationCreatedAtColumn.setCellValueFactory(cell -> {
            Timestamp createdAt = cell.getValue().getCreatedAt();
            String display = createdAt != null ? createdAt.toLocalDateTime().format(timestampFormatter) : "N/A";
            return new SimpleStringProperty(display);
        });

        // Updated At
        donationUpdatedAtColumn.setCellValueFactory(cell -> {
            Timestamp updatedAt = cell.getValue().getUpdatedAt();
            String display = updatedAt != null ? updatedAt.toLocalDateTime().format(timestampFormatter) : "N/A";
            return new SimpleStringProperty(display);
        });

        donationEventsTable.setItems(donationEvents);

        // Configure status combo
        if (donationStatusCombo != null) {
            donationStatusCombo.setItems(FXCollections.observableArrayList(
                "PENDING", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
            ));
        }

        // Configure delivery combo (optional - you can add delivery IDs here later)
        if (donationDeliveryCombo != null) {
            donationDeliveryCombo.setItems(FXCollections.observableArrayList(
                "None (NULL)"
            ));
        }

        // Auto-populate form on selection
        donationEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateEventForm(newSel);
            }
        });
    }

        private void setupOptimizationView() {
        if (optimizationEventsTable != null) {
            optEventIdColumn.setCellValueFactory(new PropertyValueFactory<>("donationEventId"));
            optEventDateColumn.setCellValueFactory(cell -> {
            Date eventDate = cell.getValue().getEventDate();
            String display = eventDate != null ? eventDate.toLocalDate().format(dateFormatter) : "N/A";
            return new SimpleStringProperty(display);
            });
            optEventCharityColumn.setCellValueFactory(new PropertyValueFactory<>("charityName"));
            optEventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            optimizationEventsTable.setItems(donationEvents);

            optimizationEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                loadOptimizationRecommendations()
            );
        }

        if (recommendationTable != null) {
            recDishIdColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(
                    cell.getValue().getDish() != null ? cell.getValue().getDish().getId() : null
                )
            );
            recDishNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                    cell.getValue().getDish() != null ? cell.getValue().getDish().getName() : "N/A"
                )
            );
            recMaxCountColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue().getMaxDishCountFromNearExpiry())
            );
            recUsageScoreColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f", cell.getValue().getNearExpiryUsageScore()))
            );
            recCostScoreColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f", cell.getValue().getCostSavingScore()))
            );
            recommendationTable.setItems(dishRecommendations);
        }
        }

    // ==================== DATA LOADING ====================

    private void loadAllData() {
        loadItems();
        loadEvents();
        loadOptimizationRecommendations();
        updateStatistics();
    }

    private void loadItems() {
        try {
            List<FoodDonationItem> items = itemService.getAllFoodDonationItems();
            donationItems.setAll(items);
            updateItemStatistics();
        } catch (SQLException e) {
            showError("Failed to load donation items", e);
        }
    }

    private void loadEvents() {
        try {
            List<Fooddonationevent> events = eventService.getAllFoodDonationEvents();
            donationEvents.setAll(events);
            updateEventStatistics();
            loadOptimizationRecommendations();
        } catch (SQLException e) {
            showError("Failed to load donation events", e);
        }
    }

    private void loadOptimizationRecommendations() {
        try {
            List<DonationDishRecommendation> recommendations = eventService.suggestOptimizedDonationDishes(DEFAULT_NEAR_EXPIRY_DAYS);
            dishRecommendations.setAll(recommendations);
        } catch (SQLException e) {
            dishRecommendations.clear();
            showError("Failed to load optimization recommendations", e);
        }
    }

    @FXML
    private void handleRefreshOptimizationView() {
        loadEvents();
        loadOptimizationRecommendations();
        showInfo("Optimization view refreshed!");
    }

    @FXML
    private void handleAddRecommendedDishToEvent() {
        Fooddonationevent selectedEvent = optimizationEventsTable != null
                ? optimizationEventsTable.getSelectionModel().getSelectedItem()
                : null;
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an event from the left list.");
            return;
        }

        DonationDishRecommendation selectedRecommendation = recommendationTable != null
                ? recommendationTable.getSelectionModel().getSelectedItem()
                : null;
        if (selectedRecommendation == null || selectedRecommendation.getDish() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a dish from the right list.");
            return;
        }

        Integer quantity = parseInteger(
                plannerDishQuantityField != null ? plannerDishQuantityField.getText() : null,
                "Dish quantity"
        );
        if (quantity == null || quantity <= 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Dish quantity must be a positive number.");
            return;
        }

        try {
            int eventId = selectedEvent.getDonationEventId();
            int dishId = selectedRecommendation.getDish().getId();

            if (itemService.itemExists(eventId, dishId)) {
                itemService.incrementItemQuantityWithStock(eventId, dishId, quantity);
            } else {
                FoodDonationItem item = new FoodDonationItem(eventId, dishId, quantity);
                itemService.addFoodDonationItemWithStock(item);
            }

            if (plannerDishQuantityField != null) {
                plannerDishQuantityField.clear();
            }

            loadItems();
            showSuccess("Dish added to selected event successfully!");
        } catch (SQLException e) {
            showError("Failed to add dish to selected event", e);
        }
    }

    // ==================== DONATION ITEMS CRUD ====================

    @FXML
    private void handleAddDonationItem() {
        try {
            Integer eventId = parseInteger(itemEventIdField.getText(), "Event ID");
            Integer itemId = parseInteger(itemDishIdField.getText(), "Dish ID");
            Integer quantity = parseInteger(itemQuantityField.getText(), "Quantity");

            if (eventId == null || itemId == null || quantity == null) return;

            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be positive!");
                return;
            }

            // Check if item already exists
            if (itemService.itemExists(eventId, itemId)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Item",
                        "This item already exists in the event. Use Update instead.");
                return;
            }

            FoodDonationItem item = new FoodDonationItem(eventId, itemId, quantity);
            itemService.addFoodDonationItemWithStock(item);

            clearItemForm();
            loadItems();
            showSuccess("Item added successfully!");

        } catch (SQLException e) {
            showError("Failed to add item", e);
        }
    }

    @FXML
    private void handleUpdateDonationItem() {
        FoodDonationItem selected = donationItemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to update.");
            return;
        }

        try {
            Integer quantity = parseInteger(itemQuantityField.getText(), "Quantity");
            if (quantity == null || quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be positive!");
                return;
            }

            itemService.updateItemQuantityWithStock(selected.getDonationEventId(), selected.getItemId(), quantity);

            loadItems();
            showSuccess("Item updated successfully!");

        } catch (SQLException e) {
            showError("Failed to update item", e);
        }
    }

    @FXML
    private void handleDeleteDonationItem() {
        FoodDonationItem selected = donationItemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete this item?");
        confirm.setContentText("Event #" + selected.getDonationEventId() + " - " + selected.getItemName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itemService.deleteFoodDonationItemWithStock(selected.getDonationEventId(), selected.getItemId());
                clearItemForm();
                loadItems();
                showSuccess("Item deleted successfully!");
            } catch (SQLException e) {
                showError("Failed to delete item", e);
            }
        }
    }

    @FXML
    private void handleRefreshItems() {
        loadItems();
        clearItemForm();
        showInfo("Items refreshed!");
    }

    // ==================== DONATION EVENTS CRUD ====================

    @FXML
    private void handleAddDonationEvent() {
        try {
            LocalDate eventDate = donationEventDatePicker.getValue();
            if (eventDate == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select an event date!");
                return;
            }

            String charityInput = donationCharityNameField != null ? donationCharityNameField.getText() : null;
            String charityName = (charityInput == null || charityInput.trim().isEmpty())
                    ? "Donation Event " + eventDate
                    : charityInput.trim();

            Fooddonationevent event = new Fooddonationevent();
            event.setEventDate(Date.valueOf(eventDate));
            event.setTotalQuantity(1);
            event.setCharityName(charityName);
            event.setStatus("PENDING");
            event.setDeliveryId(null); // Optional - can be set later
            event.setCalendarEventId(null); // Optional

            eventService.addFoodDonationEvent(event);

            clearEventForm();
            loadEvents();
            showSuccess("Event created successfully!");

        } catch (SQLException e) {
            showError("Failed to create event", e);
        }
    }

    @FXML
    private void handleUpdateDonationEvent() {
        Fooddonationevent selected = donationEventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to update.");
            return;
        }

        try {
            LocalDate eventDate = donationEventDatePicker.getValue();
            if (eventDate == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select an event date!");
                return;
            }

            String charityInput = donationCharityNameField != null ? donationCharityNameField.getText() : null;
            String charityName = (charityInput == null || charityInput.trim().isEmpty())
                    ? "Donation Event " + eventDate
                    : charityInput.trim();

            selected.setEventDate(Date.valueOf(eventDate));
            selected.setCharityName(charityName);

            eventService.updateFoodDonationEvent(selected);

            loadEvents();
            showSuccess("Event updated successfully!");

        } catch (SQLException e) {
            showError("Failed to update event", e);
        }
    }

    @FXML
    private void handleDeleteDonationEvent() {
        Fooddonationevent selected = donationEventsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete this event?");
        confirm.setContentText(selected.getCharityName() + " - " + selected.getEventDate());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete items first
                itemService.deleteItemsByEventIdWithStock(selected.getDonationEventId());
                // Then delete event
                eventService.deleteFoodDonationEvent(selected.getDonationEventId());

                clearEventForm();
                loadEvents();
                loadItems();
                showSuccess("Event and its items deleted successfully!");
            } catch (SQLException e) {
                showError("Failed to delete event", e);
            }
        }
    }

    @FXML
    private void handleRefreshEvents() {
        loadEvents();
        clearEventForm();
        showInfo("Events refreshed!");
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleBackToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/homepage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Failed to return to home", e);
        }
    }

    // ==================== UTILITY METHODS ====================

    private void populateEventForm(Fooddonationevent event) {
        if (event.getEventDate() != null) {
            donationEventDatePicker.setValue(event.getEventDate().toLocalDate());
        }
        if (donationTotalQuantityField != null && event.getTotalQuantity() != null) {
            donationTotalQuantityField.setText(String.valueOf(event.getTotalQuantity()));
        }
        if (donationCharityNameField != null) {
            donationCharityNameField.setText(event.getCharityName());
        }
        if (donationStatusCombo != null) {
            donationStatusCombo.setValue(event.getStatus());
        }
    }

    private void clearItemForm() {
        itemEventIdField.clear();
        itemDishIdField.clear();
        itemQuantityField.clear();
        donationItemsTable.getSelectionModel().clearSelection();
    }

    private void clearEventForm() {
        donationEventDatePicker.setValue(null);
        if (donationTotalQuantityField != null) {
            donationTotalQuantityField.clear();
        }
        if (donationCharityNameField != null) {
            donationCharityNameField.clear();
        }
        if (donationStatusCombo != null) {
            donationStatusCombo.getSelectionModel().clearSelection();
        }
        donationEventsTable.getSelectionModel().clearSelection();
    }

    private void updateStatistics() {
        updateItemStatistics();
        updateEventStatistics();
    }

    private void updateItemStatistics() {
        try {
            int count = itemService.countAllItems();
            totalItemsLabel.setText(String.valueOf(count));

            int totalQty = donationItems.stream().mapToInt(FoodDonationItem::getQuantity).sum();
            totalQuantityLabel.setText(String.valueOf(totalQty));
        } catch (SQLException e) {
            // Silently fail for statistics
        }
    }

    private void updateEventStatistics() {
        try {
            int total = eventService.countAllEvents();
            totalEventsLabel.setText(String.valueOf(total));

            long pending = donationEvents.stream().filter(e -> "PENDING".equals(e.getStatus())).count();
            pendingEventsLabel.setText(String.valueOf(pending));

            long completed = donationEvents.stream().filter(e -> "COMPLETED".equals(e.getStatus())).count();
            completedEventsLabel.setText(String.valueOf(completed));
        } catch (SQLException e) {
            // Silently fail for statistics
        }
    }

    private Integer parseInteger(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " is required!");
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", fieldName + " must be a valid number!");
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
    }

    private void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Info", message);
    }

    private void showError(String action, Exception e) {
        System.err.println(action + ": " + e.getMessage());
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", action + "\n" + e.getMessage());
    }
}