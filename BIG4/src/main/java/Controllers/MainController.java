package Controllers;

import Entities.Ingredient;
import Entities.WasteRecord;
import Utils.Mydatabase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

	@FXML
	private TableView<Ingredient> ingredientsTable;
	@FXML
	private TableColumn<Ingredient, String> ingredientNameColumn;
	@FXML
	private TableColumn<Ingredient, String> ingredientQuantityColumn;
	@FXML
	private TableColumn<Ingredient, Number> ingredientMinStockColumn;
	@FXML
	private TableColumn<Ingredient, Number> ingredientUnitCostColumn;
	@FXML
	private TableColumn<Ingredient, String> ingredientExpiryColumn;
	@FXML
	private TableColumn<Ingredient, String> ingredientCreatedColumn;

	@FXML
	private TextField ingredientFilterField;
	@FXML
	private DatePicker ingredientDateFromPicker;
	@FXML
	private DatePicker ingredientDateToPicker;

	@FXML
	private TableView<WasteRecord> wasteTable;
	@FXML
	private TableColumn<WasteRecord, String> wasteIngredientColumn;
	@FXML
	private TableColumn<WasteRecord, Number> wasteQuantityColumn;
	@FXML
	private TableColumn<WasteRecord, String> wasteTypeColumn;
	@FXML
	private TableColumn<WasteRecord, String> wasteDateColumn;
	@FXML
	private TableColumn<WasteRecord, String> wasteReasonColumn;

		@FXML
		private TextField wasteFilterField;

	// ── Date range filter ──
	@FXML
	private DatePicker wasteDateFromPicker;
	@FXML
	private DatePicker wasteDateToPicker;

	// ── Richer stat labels ──
	@FXML
	private Label expiredItemsLabel;
	@FXML
	private Label inventoryValueLabel;
	@FXML
	private Label wasteCostLabel;
	@FXML
	private Label mostWastedLabel;

	// ── Charts ──
	@FXML
	private PieChart wasteTypePieChart;
	@FXML
	private BarChart<String, Number> topWastedBarChart;
	@FXML
	private BarChart<String, Number> stockLevelsBarChart;
		@FXML
		private TabPane mainTabPane;

		@FXML
		private Label totalIngredientsLabel;
		@FXML
		private Label lowStockLabel;
		@FXML
		private Label wasteRecordsLabel;

		@FXML
		private VBox overviewSection;

	private final ObservableList<String> wasteTypes = FXCollections.observableArrayList(
			"Preparation Loss",
			"Spoilage",
			"Expired",
			"Customer Return"
	);

	private final ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
	private final ObservableList<WasteRecord> wasteRecords = FXCollections.observableArrayList();
	private FilteredList<Ingredient> filteredIngredients;
	private FilteredList<WasteRecord> filteredWasteRecords;
	private final Mydatabase database = Mydatabase.getInstance();
	private final DateTimeFormatter wasteDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final String INGREDIENT_SELECT_ALL = "SELECT id, name, quantityInStock, unit, minStockLevel, unitCost, expiryDate, createdAt FROM Ingredient";
	private static final String INGREDIENT_INSERT = "INSERT INTO Ingredient (name, quantityInStock, unit, minStockLevel, unitCost, expiryDate, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String INGREDIENT_UPDATE = "UPDATE Ingredient SET name = ?, quantityInStock = ?, unit = ?, minStockLevel = ?, unitCost = ?, expiryDate = ? WHERE id = ?";
	private static final String INGREDIENT_DELETE = "DELETE FROM Ingredient WHERE id = ?";
	private static final String INGREDIENT_DECREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock - ? WHERE id = ?";
	private static final String INGREDIENT_INCREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock + ? WHERE id = ?";
	private static final String WASTE_SELECT_ALL = "SELECT id, ingredientId, quantityWasted, wasteType, date, reason FROM WasteRecord ORDER BY date DESC";
	private static final String WASTE_INSERT = "INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason) VALUES (?, ?, ?, ?, ?)";
	private static final String WASTE_UPDATE = "UPDATE WasteRecord SET ingredientId = ?, quantityWasted = ?, wasteType = ?, reason = ? WHERE id = ?";
	private static final String WASTE_DELETE = "DELETE FROM WasteRecord WHERE id = ?";
	private static final String WASTE_DELETE_BY_INGREDIENT = "DELETE FROM WasteRecord WHERE ingredientId = ?";

	@FXML
	private void initialize() {
		ensureIngredientCreatedAtColumn();
		configureFilters();
		configureIngredientTable();
		configureIngredientRowFactory();
		configureWasteTable();
		configureDateRangeFilter();
		loadDataFromDatabase();
	}

	private void configureFilters() {
		filteredIngredients = new FilteredList<>(ingredients, this::ingredientMatchesFilter);
		SortedList<Ingredient> sortedIngredients = new SortedList<>(filteredIngredients);
		sortedIngredients.comparatorProperty().bind(ingredientsTable.comparatorProperty());
		ingredientsTable.setItems(sortedIngredients);

		filteredWasteRecords = new FilteredList<>(wasteRecords, this::wasteRecordMatchesFilter);
		SortedList<WasteRecord> sortedWaste = new SortedList<>(filteredWasteRecords);
		sortedWaste.comparatorProperty().bind(wasteTable.comparatorProperty());
		wasteTable.setItems(sortedWaste);

		if (ingredientFilterField != null) {
			ingredientFilterField.textProperty().addListener((obs, oldVal, newVal) ->
					filteredIngredients.setPredicate(this::ingredientMatchesFilter));
		}
		if (ingredientDateFromPicker != null) {
			ingredientDateFromPicker.valueProperty().addListener((obs, oldVal, newVal) ->
					filteredIngredients.setPredicate(this::ingredientMatchesFilter));
		}
		if (ingredientDateToPicker != null) {
			ingredientDateToPicker.valueProperty().addListener((obs, oldVal, newVal) ->
					filteredIngredients.setPredicate(this::ingredientMatchesFilter));
		}
		if (wasteFilterField != null) {
			wasteFilterField.textProperty().addListener((obs, oldVal, newVal) ->
					filteredWasteRecords.setPredicate(this::wasteRecordMatchesFilter));
		}
	}

	private void configureIngredientTable() {
		ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		ingredientQuantityColumn.setCellValueFactory(cell -> {
			double quantity = cell.getValue().getQuantityInStock();
			String unit = Optional.ofNullable(cell.getValue().getUnit()).orElse("").trim();
			String quantityText = String.format("%.2f", quantity);
			return new SimpleStringProperty(unit.isEmpty() ? quantityText : quantityText + " " + unit);
		});
		ingredientMinStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
		ingredientUnitCostColumn.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
		ingredientCreatedColumn.setCellValueFactory(cell -> {
			LocalDateTime createdAt = cell.getValue().getCreatedAt();
			String display = createdAt != null ? createdAt.format(wasteDateFormatter) : "N/A";
			return new SimpleStringProperty(display);
		});

		// ── Expiry countdown display ──
		ingredientExpiryColumn.setCellValueFactory(cell -> {
			LocalDate expiry = cell.getValue().getExpiryDate();
			if (expiry == null) return new SimpleStringProperty("N/A");
			long days = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
			if (days < 0) return new SimpleStringProperty("Expired! (" + Math.abs(days) + "d ago)");
			if (days == 0) return new SimpleStringProperty("Expires today!");
			if (days <= 7) return new SimpleStringProperty(days + " day" + (days > 1 ? "s" : "") + " left \u26a0");
			if (days <= 30) return new SimpleStringProperty(days + " days left");
			return new SimpleStringProperty(expiry.toString());
		});

		// ── Color-code expiry cells ──
		ingredientExpiryColumn.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				getStyleClass().removeAll("expiry-expired", "expiry-warning", "expiry-soon", "expiry-ok");
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item);
					if (item.contains("Expired")) {
						getStyleClass().add("expiry-expired");
					} else if (item.contains("\u26a0")) {
						getStyleClass().add("expiry-warning");
					} else if (item.contains("days left")) {
						getStyleClass().add("expiry-soon");
					} else if (!item.equals("N/A")) {
						getStyleClass().add("expiry-ok");
					}
				}
			}
		});

		ingredientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			// Selection kept for external uses (edit dialog) but no inline form sync needed.
		});
	}

	/** Color-code ingredient rows: red=expired, yellow=low-stock, green=healthy */
	private void configureIngredientRowFactory() {
		ingredientsTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(Ingredient item, boolean empty) {
				super.updateItem(item, empty);
				getStyleClass().removeAll("row-expired", "row-low-stock", "row-healthy");
				if (empty || item == null) {
					return;
				}
				boolean expired = item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now());
				boolean lowStock = item.getQuantityInStock() <= item.getMinStockLevel();
				if (expired) {
					getStyleClass().add("row-expired");
				} else if (lowStock) {
					getStyleClass().add("row-low-stock");
				} else {
					getStyleClass().add("row-healthy");
				}
			}
		});
	}

	private void configureWasteTable() {
		wasteIngredientColumn.setCellValueFactory(cell -> new SimpleStringProperty(
				ingredientNameById(cell.getValue().getIngredientId())
		));
		wasteQuantityColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
				cell.getValue().getQuantityWasted()
		));
		wasteTypeColumn.setCellValueFactory(cell -> new SimpleStringProperty(
				Optional.ofNullable(cell.getValue().getWasteType()).filter(type -> !type.isBlank()).orElse("-")
		));
		wasteDateColumn.setCellValueFactory(cell -> {
			LocalDateTime date = cell.getValue().getDate();
			String display = date != null ? date.format(wasteDateFormatter) : "N/A";
			return new SimpleStringProperty(display);
		});
		wasteReasonColumn.setCellValueFactory(cell -> new SimpleStringProperty(
				Optional.ofNullable(cell.getValue().getReason()).filter(reason -> !reason.isBlank()).orElse("-")
		));

		wasteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			// Selection kept for external uses (edit dialog) but no inline form sync needed.
		});
	}

	/** Wire up date-range DatePickers so waste filter reacts to changes */
	private void configureDateRangeFilter() {
		if (wasteDateFromPicker != null) {
			wasteDateFromPicker.valueProperty().addListener((obs, o, n) ->
					filteredWasteRecords.setPredicate(this::wasteRecordMatchesFilter));
		}
		if (wasteDateToPicker != null) {
			wasteDateToPicker.valueProperty().addListener((obs, o, n) ->
					filteredWasteRecords.setPredicate(this::wasteRecordMatchesFilter));
		}
	}

	@FXML
	private void handleClearDateFilter() {
		if (wasteDateFromPicker != null) wasteDateFromPicker.setValue(null);
		if (wasteDateToPicker != null) wasteDateToPicker.setValue(null);
	}

	@FXML
	private void handleClearIngredientDateFilter() {
		if (ingredientDateFromPicker != null) ingredientDateFromPicker.setValue(null);
		if (ingredientDateToPicker != null) ingredientDateToPicker.setValue(null);
	}

	private void loadDataFromDatabase() {
		try (Connection connection = database.getConnection()) {
			ingredients.setAll(fetchIngredients(connection));
			wasteRecords.setAll(fetchWasteRecords(connection));
			refreshStatistics();
		} catch (SQLException e) {
			showDatabaseError("Unable to load data", e);
		}
	}

	private List<Ingredient> fetchIngredients(Connection connection) throws SQLException {
		List<Ingredient> data = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(INGREDIENT_SELECT_ALL);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				Date expiry = resultSet.getDate("expiryDate");
				Timestamp created = resultSet.getTimestamp("createdAt");
				data.add(new Ingredient(
						resultSet.getLong("id"),
						resultSet.getString("name"),
						resultSet.getDouble("quantityInStock"),
						resultSet.getString("unit"),
						resultSet.getDouble("minStockLevel"),
						resultSet.getDouble("unitCost"),
						expiry == null ? null : expiry.toLocalDate(),
						created == null ? null : created.toLocalDateTime()
				));
			}
		}
		return data;
	}

	private List<WasteRecord> fetchWasteRecords(Connection connection) throws SQLException {
		List<WasteRecord> data = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(WASTE_SELECT_ALL);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				Timestamp timestamp = resultSet.getTimestamp("date");
				data.add(new WasteRecord(
						resultSet.getLong("id"),
						resultSet.getLong("ingredientId"),
						resultSet.getDouble("quantityWasted"),
						resultSet.getString("wasteType"),
						timestamp == null ? null : timestamp.toLocalDateTime(),
						resultSet.getString("reason")
				));
			}
		}
		return data;
	}

	public boolean saveIngredientCreate(IngredientDialogController.IngredientFormData form) {
		List<String> missingFields = new ArrayList<>();
		String name = Optional.ofNullable(form.name()).map(String::trim).orElse("");
		String quantityText = Optional.ofNullable(form.quantity()).map(String::trim).orElse("");
		String minStockText = Optional.ofNullable(form.minStock()).map(String::trim).orElse("");
		String unitCostText = Optional.ofNullable(form.unitCost()).map(String::trim).orElse("");
		String unit = Optional.ofNullable(form.unit()).map(String::trim).orElse("");
		LocalDate expiry = form.expiry();

		if (name.isBlank()) missingFields.add("Name");
		if (quantityText.isBlank()) missingFields.add("Quantity in stock");
		if (minStockText.isBlank()) missingFields.add("Minimum stock");
		if (unitCostText.isBlank()) missingFields.add("Unit cost");

		if (!missingFields.isEmpty()) {
			String message = "Please fill the following fields:\n" + missingFields.stream()
					.map(field -> "- " + field)
					.collect(Collectors.joining("\n"));
			showAlert(Alert.AlertType.ERROR, "Validation", message);
			return false;
		}

		Double quantity = parseNumeric(quantityText, "Quantity in stock");
		Double minStock = parseNumeric(minStockText, "Minimum stock");
		Double unitCost = parseNumeric(unitCostText, "Unit cost");
		if (quantity == null || minStock == null || unitCost == null) {
			return false;
		}

		if (quantity < 0 || minStock < 0 || unitCost < 0) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Numeric values must be non-negative.");
			return false;
		}

		boolean nameExists = ingredients.stream()
				.anyMatch(existing -> existing.getName().equalsIgnoreCase(name));
		if (nameExists) {
			showAlert(Alert.AlertType.ERROR, "Validation", "An ingredient with this name already exists.");
			return false;
		}

		String resolvedUnit = unit.isBlank() ? "unit" : unit;
		Ingredient ingredient = new Ingredient(
				null,
				name.trim(),
				quantity,
				resolvedUnit,
				minStock,
				unitCost,
				expiry,
				LocalDateTime.now()
		);

		try (Connection connection = database.getConnection();
				PreparedStatement statement = connection.prepareStatement(INGREDIENT_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			bindIngredientParameters(statement, ingredient);
			statement.executeUpdate();
			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					ingredient.setId(keys.getLong(1));
				}
			}
		} catch (SQLException e) {
			showDatabaseError("Unable to add ingredient", e);
			return false;
		}

		ingredients.add(ingredient);
		showAlert(Alert.AlertType.INFORMATION, "Success", "Ingredient added successfully.");
		refreshStatistics();
		return true;
	}

	public boolean saveIngredientUpdate(IngredientDialogController.IngredientFormData form) {
		Ingredient selected = ingredientsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select an ingredient to update.");
			return false;
		}

		List<String> missingFields = new ArrayList<>();
		String name = Optional.ofNullable(form.name()).map(String::trim).orElse("");
		String quantityText = Optional.ofNullable(form.quantity()).map(String::trim).orElse("");
		String minStockText = Optional.ofNullable(form.minStock()).map(String::trim).orElse("");
		String unitCostText = Optional.ofNullable(form.unitCost()).map(String::trim).orElse("");
		String unit = Optional.ofNullable(form.unit()).map(String::trim).orElse("");
		LocalDate expiry = form.expiry();

		if (name.isBlank()) missingFields.add("Name");
		if (quantityText.isBlank()) missingFields.add("Quantity in stock");
		if (minStockText.isBlank()) missingFields.add("Minimum stock");
		if (unitCostText.isBlank()) missingFields.add("Unit cost");

		if (!missingFields.isEmpty()) {
			String message = "Please fill the following fields:\n" + missingFields.stream()
					.map(field -> "- " + field)
					.collect(Collectors.joining("\n"));
			showAlert(Alert.AlertType.ERROR, "Validation", message);
			return false;
		}

		Double quantity = parseNumeric(quantityText, "Quantity in stock");
		Double minStock = parseNumeric(minStockText, "Minimum stock");
		Double unitCost = parseNumeric(unitCostText, "Unit cost");
		if (quantity == null || minStock == null || unitCost == null) {
			return false;
		}

		boolean differentNameExists = ingredients.stream()
				.filter(ingredient -> !Objects.equals(ingredient.getId(), selected.getId()))
				.anyMatch(ingredient -> ingredient.getName().equalsIgnoreCase(name));
		if (differentNameExists) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Another ingredient already uses this name.");
			return false;
		}

		String trimmedName = name.trim();
		String resolvedUnit = unit.isBlank() ? "unit" : unit;
		try (Connection connection = database.getConnection();
				PreparedStatement statement = connection.prepareStatement(INGREDIENT_UPDATE)) {
			statement.setString(1, trimmedName);
			statement.setDouble(2, quantity);
			statement.setString(3, resolvedUnit);
			statement.setDouble(4, minStock);
			statement.setDouble(5, unitCost);
			if (expiry != null) {
				statement.setDate(6, Date.valueOf(expiry));
			} else {
				statement.setNull(6, Types.DATE);
			}
			statement.setLong(7, selected.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			showDatabaseError("Unable to update ingredient", e);
			return false;
		}

		selected.setName(trimmedName);
		selected.setQuantityInStock(quantity);
		selected.setUnit(resolvedUnit);
		selected.setMinStockLevel(minStock);
		selected.setUnitCost(unitCost);
		selected.setExpiryDate(expiry);

		ingredientsTable.refresh();
		wasteTable.refresh();
		showAlert(Alert.AlertType.INFORMATION, "Success", "Ingredient updated successfully.");
		refreshStatistics();
		return true;
	}

	@FXML
	private void handleDeleteIngredient() {
		Ingredient selected = ingredientsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select an ingredient to delete.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Confirm deletion");
		confirm.setHeaderText("Delete ingredient " + selected.getName() + "?");
		confirm.setContentText("Linked waste records will also be removed.");

		Optional<ButtonType> response = confirm.showAndWait();
		if (response.isPresent() && response.get() == ButtonType.OK) {
			long ingredientId = selected.getId();
			try (Connection connection = database.getConnection()) {
				boolean initialAutoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				try (PreparedStatement deleteWaste = connection.prepareStatement(WASTE_DELETE_BY_INGREDIENT);
					 PreparedStatement deleteIngredient = connection.prepareStatement(INGREDIENT_DELETE)) {
					deleteWaste.setLong(1, ingredientId);
					deleteWaste.executeUpdate();
					deleteIngredient.setLong(1, ingredientId);
					deleteIngredient.executeUpdate();
					connection.commit();
				} catch (SQLException e) {
					try {
						connection.rollback();
					} catch (SQLException ignored) {
						// Ignore rollback failure
					}
					showDatabaseError("Unable to delete ingredient", e);
					return;
				} finally {
					try {
						connection.setAutoCommit(initialAutoCommit);
					} catch (SQLException ignored) {
						// Ignore reset failure
					}
				}
			} catch (SQLException e) {
				showDatabaseError("Unable to delete ingredient", e);
				return;
			}

			wasteRecords.removeIf(record -> Objects.equals(record.getIngredientId(), ingredientId));
			ingredients.remove(selected);
			ingredientsTable.getSelectionModel().clearSelection();
			wasteTable.refresh();
			showAlert(Alert.AlertType.INFORMATION, "Deleted", "Ingredient removed.");
			refreshStatistics();
		}
	}

	// Inline form clearing removed; dialogs manage their own state.

	public boolean saveWasteCreate(WasteDialogController.WasteFormData form) {
		Ingredient ingredient = form.ingredient();
		if (ingredient == null) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Select an ingredient for the waste record.");
			return false;
		}

		Double quantity = parseNumeric(form.quantity(), "Waste quantity");
		if (quantity == null) return false;
		if (quantity <= 0) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Waste quantity must be positive.");
			return false;
		}

		if (ingredient.getQuantityInStock() < quantity) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Not enough stock to record this waste.");
			return false;
		}

		String wasteType = form.wasteType();
		if (wasteType == null || wasteType.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Choose a waste type.");
			return false;
		}

		String reason = Optional.ofNullable(form.reason()).map(String::trim).orElse("");
		if (reason.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Please provide a reason for this waste record.");
			return false;
		}
		LocalDateTime recordedAt = LocalDateTime.now();
		WasteRecord record = new WasteRecord(
				null,
				ingredient.getId(),
				quantity,
				wasteType,
				recordedAt,
				reason
		);

		try (Connection connection = database.getConnection()) {
			boolean initialAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			try (PreparedStatement insertWaste = connection.prepareStatement(WASTE_INSERT, Statement.RETURN_GENERATED_KEYS);
					 PreparedStatement updateStock = connection.prepareStatement(INGREDIENT_DECREMENT_STOCK)) {
				insertWaste.setLong(1, record.getIngredientId());
				insertWaste.setDouble(2, record.getQuantityWasted());
				insertWaste.setString(3, record.getWasteType());
				insertWaste.setTimestamp(4, Timestamp.valueOf(recordedAt));
				insertWaste.setString(5, reason);
				insertWaste.executeUpdate();
				try (ResultSet keys = insertWaste.getGeneratedKeys()) {
					if (keys.next()) {
						record.setId(keys.getLong(1));
					}
				}

				updateStock.setDouble(1, quantity);
				updateStock.setLong(2, ingredient.getId());
				updateStock.executeUpdate();

				connection.commit();
			} catch (SQLException e) {
				try { connection.rollback(); } catch (SQLException ignored) {}
				showDatabaseError("Unable to record waste", e);
				return false;
			} finally {
				try { connection.setAutoCommit(initialAutoCommit); } catch (SQLException ignored) {}
			}
		} catch (SQLException e) {
			showDatabaseError("Unable to record waste", e);
			return false;
		}

		ingredient.setQuantityInStock(ingredient.getQuantityInStock() - quantity);
		wasteRecords.add(0, record);
		ingredientsTable.refresh();

		if (ingredient.getQuantityInStock() < ingredient.getMinStockLevel()) {
			showAlert(Alert.AlertType.WARNING, "Low stock", ingredient.getName() + " is below the minimum stock level.");
		} else {
			showAlert(Alert.AlertType.INFORMATION, "Success", "Waste recorded successfully.");
		}

		refreshStatistics();
		return true;
	}

	public boolean saveWasteUpdate(WasteDialogController.WasteFormData form) {
		WasteRecord selected = wasteTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select a waste record to update.");
			return false;
		}

		Ingredient ingredient = form.ingredient();
		if (ingredient == null) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Select an ingredient.");
			return false;
		}

		Double newQuantity = parseNumeric(form.quantity(), "Waste quantity");
		if (newQuantity == null) return false;
		if (newQuantity <= 0) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Waste quantity must be positive.");
			return false;
		}

		String wasteType = form.wasteType();
		if (wasteType == null || wasteType.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Choose a waste type.");
			return false;
		}

		String reason = Optional.ofNullable(form.reason()).map(String::trim).orElse("");
		if (reason.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Please provide a reason.");
			return false;
		}

		double oldQuantity = selected.getQuantityWasted();
		long oldIngredientId = selected.getIngredientId();
		double quantityDiff = newQuantity - oldQuantity;
		boolean ingredientChanged = !Objects.equals(oldIngredientId, ingredient.getId());

		// Check stock availability for the new/changed quantity
		if (!ingredientChanged && quantityDiff > 0) {
			if (ingredient.getQuantityInStock() < quantityDiff) {
				showAlert(Alert.AlertType.ERROR, "Validation", "Not enough stock to increase waste quantity.");
				return false;
			}
		}
		if (ingredientChanged) {
			if (ingredient.getQuantityInStock() < newQuantity) {
				showAlert(Alert.AlertType.ERROR, "Validation", "Not enough stock on the new ingredient.");
				return false;
			}
		}

		try (Connection connection = database.getConnection()) {
			boolean initialAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			try {
				// Update waste record row
				try (PreparedStatement updateWaste = connection.prepareStatement(WASTE_UPDATE)) {
					updateWaste.setLong(1, ingredient.getId());
					updateWaste.setDouble(2, newQuantity);
					updateWaste.setString(3, wasteType);
					updateWaste.setString(4, reason);
					updateWaste.setLong(5, selected.getId());
					updateWaste.executeUpdate();
				}

				// Adjust stock: restore old, deduct new
				if (ingredientChanged) {
					try (PreparedStatement inc = connection.prepareStatement(INGREDIENT_INCREMENT_STOCK)) {
						inc.setDouble(1, oldQuantity);
						inc.setLong(2, oldIngredientId);
						inc.executeUpdate();
					}
					try (PreparedStatement dec = connection.prepareStatement(INGREDIENT_DECREMENT_STOCK)) {
						dec.setDouble(1, newQuantity);
						dec.setLong(2, ingredient.getId());
						dec.executeUpdate();
					}
				} else if (quantityDiff != 0) {
					String stockSql = quantityDiff > 0 ? INGREDIENT_DECREMENT_STOCK : INGREDIENT_INCREMENT_STOCK;
					try (PreparedStatement adj = connection.prepareStatement(stockSql)) {
						adj.setDouble(1, Math.abs(quantityDiff));
						adj.setLong(2, ingredient.getId());
						adj.executeUpdate();
					}
				}

				connection.commit();
			} catch (SQLException e) {
				try { connection.rollback(); } catch (SQLException ignored) {}
				showDatabaseError("Unable to update waste record", e);
				return false;
			} finally {
				try { connection.setAutoCommit(initialAutoCommit); } catch (SQLException ignored) {}
			}
		} catch (SQLException e) {
			showDatabaseError("Unable to update waste record", e);
			return false;
		}

		if (ingredientChanged) {
			Ingredient oldIngredient = ingredientById(oldIngredientId);
			if (oldIngredient != null) oldIngredient.setQuantityInStock(oldIngredient.getQuantityInStock() + oldQuantity);
			ingredient.setQuantityInStock(ingredient.getQuantityInStock() - newQuantity);
		} else if (quantityDiff != 0) {
			ingredient.setQuantityInStock(ingredient.getQuantityInStock() - quantityDiff);
		}

		selected.setIngredientId(ingredient.getId());
		selected.setQuantityWasted(newQuantity);
		selected.setWasteType(wasteType);
		selected.setReason(reason);

		ingredientsTable.refresh();
		wasteTable.refresh();
		showAlert(Alert.AlertType.INFORMATION, "Success", "Waste record updated successfully.");
		refreshStatistics();
		return true;
	}

	@FXML
	private void handleDeleteWaste() {
		WasteRecord selected = wasteTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select a waste record to delete.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Confirm deletion");
		confirm.setHeaderText("Delete this waste record?");
		confirm.setContentText("The quantity will be added back to stock.");

		Optional<ButtonType> response = confirm.showAndWait();
		if (response.isPresent() && response.get() == ButtonType.OK) {
			long recordId = selected.getId();
			long ingredientId = selected.getIngredientId();
			double wastedQuantity = selected.getQuantityWasted();
			try (Connection connection = database.getConnection()) {
				boolean initialAutoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				try (PreparedStatement deleteWaste = connection.prepareStatement(WASTE_DELETE);
					 PreparedStatement updateStock = connection.prepareStatement(INGREDIENT_INCREMENT_STOCK)) {
					deleteWaste.setLong(1, recordId);
					deleteWaste.executeUpdate();
					updateStock.setDouble(1, wastedQuantity);
					updateStock.setLong(2, ingredientId);
					updateStock.executeUpdate();
					connection.commit();
				} catch (SQLException e) {
					try {
						connection.rollback();
					} catch (SQLException ignored) {
						// Ignore rollback failure
					}
					showDatabaseError("Unable to delete waste record", e);
					return;
				} finally {
					try {
						connection.setAutoCommit(initialAutoCommit);
					} catch (SQLException ignored) {
						// Ignore reset failure
					}
				}
			} catch (SQLException e) {
				showDatabaseError("Unable to delete waste record", e);
				return;
			}

			Ingredient ingredient = ingredientById(ingredientId);
			if (ingredient != null) {
				ingredient.setQuantityInStock(ingredient.getQuantityInStock() + wastedQuantity);
			}
			wasteRecords.remove(selected);
			ingredientsTable.refresh();
			wasteTable.refresh();
			showAlert(Alert.AlertType.INFORMATION, "Deleted", "Waste record removed.");
			refreshStatistics();
		}
	}

	// Inline waste form clearing removed; dialogs manage their own state.

	// ── Dialog launchers ─────────────────────────────────────────────────────────

	@FXML
	private void showIngredientAddForm() {
		openIngredientDialog(false);
	}

	@FXML
	private void showIngredientEditForm() {
		if (ingredientsTable.getSelectionModel().getSelectedItem() == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select an ingredient to edit.");
			return;
		}
		openIngredientDialog(true);
	}

	@FXML
	private void showWasteAddForm() {
		openWasteDialog(false);
	}

	@FXML
	private void showWasteEditForm() {
		if (wasteTable.getSelectionModel().getSelectedItem() == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select a waste record to edit.");
			return;
		}
		openWasteDialog(true);
	}

	private void openIngredientDialog(boolean editMode) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialogs/IngredientDialog.fxml"));
			Parent root = loader.load();
			IngredientDialogController controller = loader.getController();
			Stage stage = buildDialogStage(root, editMode ? "Edit Ingredient" : "Add Ingredient");
			controller.setup(this, stage, editMode ? ingredientsTable.getSelectionModel().getSelectedItem() : null, editMode);
			stage.showAndWait();
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Dialog", "Unable to open ingredient dialog: " + e.getMessage());
		}
	}

	private void openWasteDialog(boolean editMode) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialogs/WasteDialog.fxml"));
			Parent root = loader.load();
			WasteDialogController controller = loader.getController();
			Stage stage = buildDialogStage(root, editMode ? "Edit Waste" : "Record Waste");
			controller.setup(this, stage, editMode ? wasteTable.getSelectionModel().getSelectedItem() : null, editMode, ingredients, wasteTypes);
			stage.showAndWait();
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Dialog", "Unable to open waste dialog: " + e.getMessage());
		}
	}

	private Stage buildDialogStage(Parent root, String title) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		if (mainTabPane != null && mainTabPane.getScene() != null) {
			stage.initOwner(mainTabPane.getScene().getWindow());
		}
		stage.setTitle(title);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.setMaximized(true);
		return stage;
	}

	private boolean ingredientMatchesFilter(Ingredient ingredient) {
		String query = normalizedFilterText(ingredientFilterField);
		if (!query.isEmpty()) {
			String haystack = (ingredient.getName() + " " + ingredient.getUnit()).toLowerCase();
			if (!haystack.contains(query)) {
				return false;
			}
		}

		if (ingredientDateFromPicker != null && ingredientDateFromPicker.getValue() != null) {
			LocalDate from = ingredientDateFromPicker.getValue();
			if (ingredient.getCreatedAt() == null || ingredient.getCreatedAt().toLocalDate().isBefore(from)) {
				return false;
			}
		}

		if (ingredientDateToPicker != null && ingredientDateToPicker.getValue() != null) {
			LocalDate to = ingredientDateToPicker.getValue();
			if (ingredient.getCreatedAt() == null || ingredient.getCreatedAt().toLocalDate().isAfter(to)) {
				return false;
			}
		}

		return true;
	}

	private boolean wasteRecordMatchesFilter(WasteRecord record) {
		// Text filter
		String query = normalizedFilterText(wasteFilterField);
		if (!query.isEmpty()) {
			String ingredientName = ingredientNameById(record.getIngredientId()).toLowerCase();
			String type = Optional.ofNullable(record.getWasteType()).orElse("").toLowerCase();
			String reason = Optional.ofNullable(record.getReason()).orElse("").toLowerCase();
			String haystack = String.join(" ", ingredientName, type, reason);
			if (!haystack.contains(query)) return false;
		}

		// Date range filter
		if (wasteDateFromPicker != null && wasteDateFromPicker.getValue() != null) {
			LocalDate from = wasteDateFromPicker.getValue();
			if (record.getDate() == null || record.getDate().toLocalDate().isBefore(from)) return false;
		}
		if (wasteDateToPicker != null && wasteDateToPicker.getValue() != null) {
			LocalDate to = wasteDateToPicker.getValue();
			if (record.getDate() == null || record.getDate().toLocalDate().isAfter(to)) return false;
		}

		return true;
	}

	private String normalizedFilterText(TextField field) {
		return Optional.ofNullable(field)
				.map(TextField::getText)
				.map(String::trim)
				.map(String::toLowerCase)
				.orElse("");
	}

	private Double parseNumeric(String value, String label) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			showAlert(Alert.AlertType.ERROR, "Validation", label + " must be a number.");
			return null;
		}
	}

	private void bindIngredientParameters(PreparedStatement statement, Ingredient ingredient) throws SQLException {
		statement.setString(1, ingredient.getName());
		statement.setDouble(2, ingredient.getQuantityInStock());
		statement.setString(3, ingredient.getUnit());
		statement.setDouble(4, ingredient.getMinStockLevel());
		statement.setDouble(5, ingredient.getUnitCost());
		if (ingredient.getExpiryDate() != null) {
			statement.setDate(6, Date.valueOf(ingredient.getExpiryDate()));
		} else {
			statement.setNull(6, Types.DATE);
		}
		if (ingredient.getCreatedAt() != null) {
			statement.setTimestamp(7, Timestamp.valueOf(ingredient.getCreatedAt()));
		} else {
			statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
		}
	}

	private void ensureIngredientCreatedAtColumn() {
		try (Connection connection = database.getConnection();
			 Statement statement = connection.createStatement()) {
			statement.executeUpdate("ALTER TABLE Ingredient ADD COLUMN createdAt DATETIME NULL DEFAULT CURRENT_TIMESTAMP");
		} catch (SQLException ignored) {
			// Column may already exist; ignore.
		}
	}

	public Ingredient ingredientById(Long id) {
		return ingredients.stream()
				.filter(ingredient -> Objects.equals(ingredient.getId(), id))
				.findFirst()
				.orElse(null);
	}

	private String ingredientNameById(Long id) {
		Ingredient ingredient = ingredientById(id);
		return ingredient != null ? ingredient.getName() : "Unknown";
	}

	private void showDatabaseError(String action, SQLException exception) {
		System.err.println(action + ": " + exception.getMessage());
		showAlert(Alert.AlertType.ERROR, "Database error", action + ". " + exception.getMessage());
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.show();
	}

	private void refreshStatistics() {
		if (totalIngredientsLabel != null) {
			totalIngredientsLabel.setText(Integer.toString(ingredients.size()));
		}
		if (lowStockLabel != null) {
			long lowStock = ingredients.stream()
					.filter(item -> item.getQuantityInStock() <= item.getMinStockLevel())
					.count();
			lowStockLabel.setText(Long.toString(lowStock));
		}
		if (wasteRecordsLabel != null) {
			wasteRecordsLabel.setText(Integer.toString(wasteRecords.size()));
		}

		// ── Expired items count ──
		if (expiredItemsLabel != null) {
			long expired = ingredients.stream()
					.filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
					.count();
			expiredItemsLabel.setText(Long.toString(expired));
		}

		// ── Total inventory value ──
		if (inventoryValueLabel != null) {
			double totalValue = ingredients.stream()
					.mapToDouble(item -> item.getQuantityInStock() * item.getUnitCost())
					.sum();
			inventoryValueLabel.setText(String.format("$%.2f", totalValue));
		}

		// ── Total waste cost ──
		if (wasteCostLabel != null) {
			double totalWasteCost = wasteRecords.stream()
					.mapToDouble(record -> {
						Ingredient ing = ingredientById(record.getIngredientId());
						double cost = ing != null ? ing.getUnitCost() : 0;
						return record.getQuantityWasted() * cost;
					})
					.sum();
			wasteCostLabel.setText(String.format("$%.2f", totalWasteCost));
		}

		// ── Most wasted ingredient ──
		if (mostWastedLabel != null) {
			Map<Long, Double> wasteByIngredient = new LinkedHashMap<>();
			for (WasteRecord record : wasteRecords) {
				wasteByIngredient.merge(record.getIngredientId(), record.getQuantityWasted(), Double::sum);
			}
			String mostWasted = wasteByIngredient.entrySet().stream()
					.max(Comparator.comparingDouble(Map.Entry::getValue))
					.map(entry -> ingredientNameById(entry.getKey()))
					.orElse("\u2014");
			mostWastedLabel.setText(mostWasted);
		}

		// ── Refresh charts ──
		refreshCharts();
	}

	// ═══════════════════════════════════════════════════════════════════
	//  CHARTS
	// ═══════════════════════════════════════════════════════════════════

	private void refreshCharts() {
		refreshWasteTypePieChart();
		refreshTopWastedBarChart();
		refreshStockLevelsBarChart();
	}

	private void refreshWasteTypePieChart() {
		if (wasteTypePieChart == null) return;
		Map<String, Double> byType = new LinkedHashMap<>();
		for (WasteRecord record : wasteRecords) {
			String type = Optional.ofNullable(record.getWasteType()).filter(t -> !t.isBlank()).orElse("Other");
			byType.merge(type, record.getQuantityWasted(), Double::sum);
		}
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		byType.forEach((type, qty) -> data.add(new PieChart.Data(type + " (" + String.format("%.1f", qty) + ")", qty)));
		wasteTypePieChart.setData(data);
	}

	private void refreshTopWastedBarChart() {
		if (topWastedBarChart == null) return;
		topWastedBarChart.getData().clear();
		Map<Long, Double> wasteByIngredient = new LinkedHashMap<>();
		for (WasteRecord record : wasteRecords) {
			wasteByIngredient.merge(record.getIngredientId(), record.getQuantityWasted(), Double::sum);
		}
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Waste Quantity");
		wasteByIngredient.entrySet().stream()
				.sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
				.limit(8)
				.forEach(entry -> series.getData().add(
						new XYChart.Data<>(ingredientNameById(entry.getKey()), entry.getValue())
				));
		topWastedBarChart.getData().add(series);
	}

	private void refreshStockLevelsBarChart() {
		if (stockLevelsBarChart == null) return;
		stockLevelsBarChart.getData().clear();
		XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
		stockSeries.setName("In Stock");
		XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
		minSeries.setName("Min Level");
		ingredients.stream().limit(12).forEach(item -> {
			stockSeries.getData().add(new XYChart.Data<>(item.getName(), item.getQuantityInStock()));
			minSeries.getData().add(new XYChart.Data<>(item.getName(), item.getMinStockLevel()));
		});
		stockLevelsBarChart.getData().addAll(stockSeries, minSeries);
	}

	// Public helpers to select tabs when opened from navigation dropdowns.
	public void showStockTab() {
		if (mainTabPane != null && !mainTabPane.getTabs().isEmpty()) {
			mainTabPane.getSelectionModel().select(0);
		}
	}

	public void showWasteTab() {
		if (mainTabPane != null && mainTabPane.getTabs().size() > 1) {
			mainTabPane.getSelectionModel().select(1);
		}
	}

	public void showAnalyticsTab() {
		if (mainTabPane != null && mainTabPane.getTabs().size() > 2) {
			mainTabPane.getSelectionModel().select(2);
		}
	}

	public void hideOverviewSection() {
		if (overviewSection != null) {
			overviewSection.setVisible(false);
			overviewSection.setManaged(false);
		}
	}

	// ═══════════════════════════════════════════════════════════════════
	//  PDF EXPORT
	// ═══════════════════════════════════════════════════════════════════

	@FXML
	private void handleExportIngredientsPdf() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export Ingredients to PDF");
		chooser.setInitialFileName("Ingredients_Report.pdf");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
		File file = chooser.showSaveDialog(ingredientsTable.getScene().getWindow());
		if (file == null) return;

		try (PDDocument doc = new PDDocument()) {
			String[] headers = {"Name", "In Stock", "Unit", "Min Stock", "Unit Cost", "Expiry"};
			float[] colWidths = {120, 70, 60, 70, 70, 100};

			List<Ingredient> data = new ArrayList<>(ingredients);
			int rowsPerPage = 28;
			int totalPages = (int) Math.ceil((double) data.size() / rowsPerPage);
			if (totalPages == 0) totalPages = 1;

			for (int page = 0; page < totalPages; page++) {
				PDPage pdPage = new PDPage(PDRectangle.A4);
				doc.addPage(pdPage);
				try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage)) {
					float y = pdPage.getMediaBox().getHeight() - 40;
					float xStart = 30;

					// Title
					cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
					cs.beginText();
					cs.newLineAtOffset(xStart, y);
					cs.showText("Ingredients Report");
					cs.endText();
					y -= 10;

					// Subtitle
					cs.setFont(PDType1Font.HELVETICA, 9);
					cs.beginText();
					cs.newLineAtOffset(xStart, y);
					cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
					cs.endText();
					y -= 20;

					// Table header
					drawPdfTableRow(cs, xStart, y, colWidths, headers, PDType1Font.HELVETICA_BOLD, 9);
					y -= 16;

					// Rows
					int start = page * rowsPerPage;
					int end = Math.min(start + rowsPerPage, data.size());
					for (int i = start; i < end; i++) {
						Ingredient ing = data.get(i);
						String expiryStr = ing.getExpiryDate() != null ? ing.getExpiryDate().toString() : "N/A";
						String[] row = {
								ing.getName(),
								String.format("%.2f", ing.getQuantityInStock()),
								ing.getUnit(),
								String.format("%.2f", ing.getMinStockLevel()),
								String.format("%.2f", ing.getUnitCost()),
								expiryStr
						};
						drawPdfTableRow(cs, xStart, y, colWidths, row, PDType1Font.HELVETICA, 8);
						y -= 14;
					}

					// Page number
					cs.setFont(PDType1Font.HELVETICA, 8);
					cs.beginText();
					cs.newLineAtOffset(xStart, 20);
					cs.showText("Page " + (page + 1) + " of " + totalPages);
					cs.endText();
				}
			}
			doc.save(file);
			showAlert(Alert.AlertType.INFORMATION, "Export", "Ingredients exported to PDF successfully.");
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export PDF: " + e.getMessage());
		}
	}

	@FXML
	private void handleExportWastePdf() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Export Waste Records to PDF");
		chooser.setInitialFileName("Waste_Report.pdf");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
		File file = chooser.showSaveDialog(wasteTable.getScene().getWindow());
		if (file == null) return;

		try (PDDocument doc = new PDDocument()) {
			String[] headers = {"Ingredient", "Qty Wasted", "Type", "Date", "Reason"};
			float[] colWidths = {110, 70, 90, 120, 140};

			List<WasteRecord> data = new ArrayList<>(filteredWasteRecords);
			int rowsPerPage = 28;
			int totalPages = (int) Math.ceil((double) data.size() / rowsPerPage);
			if (totalPages == 0) totalPages = 1;

			for (int page = 0; page < totalPages; page++) {
				PDPage pdPage = new PDPage(PDRectangle.A4);
				doc.addPage(pdPage);
				try (PDPageContentStream cs = new PDPageContentStream(doc, pdPage)) {
					float y = pdPage.getMediaBox().getHeight() - 40;
					float xStart = 30;

					cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
					cs.beginText();
					cs.newLineAtOffset(xStart, y);
					cs.showText("Waste Records Report");
					cs.endText();
					y -= 10;

					cs.setFont(PDType1Font.HELVETICA, 9);
					cs.beginText();
					cs.newLineAtOffset(xStart, y);
					cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
					cs.endText();
					y -= 20;

					drawPdfTableRow(cs, xStart, y, colWidths, headers, PDType1Font.HELVETICA_BOLD, 9);
					y -= 16;

					int start = page * rowsPerPage;
					int end = Math.min(start + rowsPerPage, data.size());
					for (int i = start; i < end; i++) {
						WasteRecord rec = data.get(i);
						String dateStr = rec.getDate() != null ? rec.getDate().format(wasteDateFormatter) : "N/A";
						String reasonStr = Optional.ofNullable(rec.getReason()).orElse("-");
						if (reasonStr.length() > 30) reasonStr = reasonStr.substring(0, 27) + "...";
						String[] row = {
								ingredientNameById(rec.getIngredientId()),
								String.format("%.2f", rec.getQuantityWasted()),
								Optional.ofNullable(rec.getWasteType()).orElse("-"),
								dateStr,
								reasonStr
						};
						drawPdfTableRow(cs, xStart, y, colWidths, row, PDType1Font.HELVETICA, 8);
						y -= 14;
					}

					cs.setFont(PDType1Font.HELVETICA, 8);
					cs.beginText();
					cs.newLineAtOffset(xStart, 20);
					cs.showText("Page " + (page + 1) + " of " + totalPages);
					cs.endText();
				}
			}
			doc.save(file);
			showAlert(Alert.AlertType.INFORMATION, "Export", "Waste records exported to PDF successfully.");
		} catch (IOException e) {
			showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export PDF: " + e.getMessage());
		}
	}

	private void drawPdfTableRow(PDPageContentStream cs, float x, float y,
								 float[] colWidths, String[] values,
								 PDType1Font font, float fontSize) throws IOException {
		cs.setFont(font, fontSize);
		float currentX = x;
		for (int i = 0; i < values.length && i < colWidths.length; i++) {
			cs.beginText();
			cs.newLineAtOffset(currentX, y);
			String text = values[i] != null ? values[i] : "";
			// Truncate if too wide
			float maxWidth = colWidths[i] - 4;
			while (font.getStringWidth(text) / 1000 * fontSize > maxWidth && text.length() > 1) {
				text = text.substring(0, text.length() - 1);
			}
			cs.showText(text);
			cs.endText();
			currentX += colWidths[i];
		}
	}
}
