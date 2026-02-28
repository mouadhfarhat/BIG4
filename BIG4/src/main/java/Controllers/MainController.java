package Controllers;

import Entities.Ingredient;
import Entities.WasteRecord;
import Utils.Mydatabase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {

	@FXML
	private TableView<Ingredient> ingredientsTable;
	@FXML
	private TableColumn<Ingredient, String> ingredientNameColumn;
	@FXML
	private TableColumn<Ingredient, Number> ingredientQuantityColumn;
	@FXML
	private TableColumn<Ingredient, String> ingredientUnitColumn;
	@FXML
	private TableColumn<Ingredient, Number> ingredientMinStockColumn;
	@FXML
	private TableColumn<Ingredient, Number> ingredientUnitCostColumn;
	@FXML
	private TableColumn<Ingredient, String> ingredientExpiryColumn;

	@FXML
	private TextField ingredientNameField;
	@FXML
	private TextField ingredientQuantityField;
	@FXML
	private TextField ingredientUnitField;
	@FXML
	private TextField ingredientMinStockField;
	@FXML
	private TextField ingredientUnitCostField;
	@FXML
	private DatePicker ingredientExpiryDatePicker;

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
	private ComboBox<Ingredient> wasteIngredientCombo;
	@FXML
	private TextField wasteQuantityField;
	@FXML
	private ComboBox<String> wasteTypeCombo;
	@FXML
	private TextArea wasteReasonField;

	private final ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
	private final ObservableList<WasteRecord> wasteRecords = FXCollections.observableArrayList();
	private final Mydatabase database = Mydatabase.getInstance();
	private final DateTimeFormatter wasteDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final String INGREDIENT_SELECT_ALL = "SELECT id, name, quantityInStock, unit, minStockLevel, unitCost, expiryDate FROM Ingredient";
	private static final String INGREDIENT_INSERT = "INSERT INTO Ingredient (name, quantityInStock, unit, minStockLevel, unitCost, expiryDate) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INGREDIENT_UPDATE = "UPDATE Ingredient SET name = ?, quantityInStock = ?, unit = ?, minStockLevel = ?, unitCost = ?, expiryDate = ? WHERE id = ?";
	private static final String INGREDIENT_DELETE = "DELETE FROM Ingredient WHERE id = ?";
	private static final String INGREDIENT_DECREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock - ? WHERE id = ?";
	private static final String INGREDIENT_INCREMENT_STOCK = "UPDATE Ingredient SET quantityInStock = quantityInStock + ? WHERE id = ?";
	private static final String WASTE_SELECT_ALL = "SELECT id, ingredientId, quantityWasted, wasteType, date, reason FROM WasteRecord ORDER BY date DESC";
	private static final String WASTE_INSERT = "INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason) VALUES (?, ?, ?, ?, ?)";
	private static final String WASTE_DELETE = "DELETE FROM WasteRecord WHERE id = ?";
	private static final String WASTE_DELETE_BY_INGREDIENT = "DELETE FROM WasteRecord WHERE ingredientId = ?";

	@FXML
	private void initialize() {
		configureIngredientTable();
		configureWasteTable();
		configureWasteInputs();
		loadDataFromDatabase();
	}

	private void configureIngredientTable() {
		ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		ingredientQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantityInStock"));
		ingredientUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
		ingredientMinStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
		ingredientUnitCostColumn.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
		ingredientExpiryColumn.setCellValueFactory(cell -> {
			LocalDate expiry = cell.getValue().getExpiryDate();
			String display = expiry != null ? expiry.toString() : "N/A";
			return new SimpleStringProperty(display);
		});

		ingredientsTable.setItems(ingredients);
		ingredientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			if (newSel == null) {
				clearIngredientForm();
				return;
			}
			populateIngredientForm(newSel);
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

		wasteTable.setItems(wasteRecords);
	}

	private void configureWasteInputs() {
		wasteIngredientCombo.setItems(ingredients);
		wasteIngredientCombo.setCellFactory(box -> new ListCell<>() {
			@Override
			protected void updateItem(Ingredient item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});
		wasteIngredientCombo.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Ingredient item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});

		wasteIngredientCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(Ingredient ingredient) {
				return ingredient == null ? "" : ingredient.getName();
			}

			@Override
			public Ingredient fromString(String string) {
				return ingredients.stream()
						.filter(ingredient -> ingredient.getName().equalsIgnoreCase(string))
						.findFirst()
						.orElse(null);
			}
		});

		wasteTypeCombo.setItems(FXCollections.observableArrayList(
				"Preparation Loss",
				"Spoilage",
				"Expired",
				"Customer Return"
		));
	}

	private void loadDataFromDatabase() {
		try (Connection connection = database.getConnection()) {
			ingredients.setAll(fetchIngredients(connection));
			wasteRecords.setAll(fetchWasteRecords(connection));
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
				data.add(new Ingredient(
						resultSet.getLong("id"),
						resultSet.getString("name"),
						resultSet.getDouble("quantityInStock"),
						resultSet.getString("unit"),
						resultSet.getDouble("minStockLevel"),
						resultSet.getDouble("unitCost"),
						expiry == null ? null : expiry.toLocalDate()
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

	@FXML
	private void handleAddIngredient() {
		String name = ingredientNameField.getText();
		if (name == null || name.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Ingredient name is required.");
			return;
		}

		Double quantity = parseDouble(ingredientQuantityField, "Quantity in stock");
		Double minStock = parseDouble(ingredientMinStockField, "Minimum stock");
		Double unitCost = parseDouble(ingredientUnitCostField, "Unit cost");
		String unit = Optional.ofNullable(ingredientUnitField.getText()).map(String::trim).orElse("");
		LocalDate expiry = ingredientExpiryDatePicker.getValue();

		if (quantity == null || minStock == null || unitCost == null) {
			return;
		}

		if (quantity < 0 || minStock < 0 || unitCost < 0) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Numeric values must be non-negative.");
			return;
		}

		boolean nameExists = ingredients.stream()
				.anyMatch(existing -> existing.getName().equalsIgnoreCase(name));
		if (nameExists) {
			showAlert(Alert.AlertType.ERROR, "Validation", "An ingredient with this name already exists.");
			return;
		}

		String resolvedUnit = unit.isBlank() ? "unit" : unit;
		Ingredient ingredient = new Ingredient(
				null,
				name.trim(),
				quantity,
				resolvedUnit,
				minStock,
				unitCost,
				expiry
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
			return;
		}

		ingredients.add(ingredient);
		clearIngredientForm();
		showAlert(Alert.AlertType.INFORMATION, "Success", "Ingredient added successfully.");
	}

	@FXML
	private void handleUpdateIngredient() {
		Ingredient selected = ingredientsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Selection", "Select an ingredient to update.");
			return;
		}

		String name = ingredientNameField.getText();
		if (name == null || name.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Ingredient name is required.");
			return;
		}

		Double quantity = parseDouble(ingredientQuantityField, "Quantity in stock");
		Double minStock = parseDouble(ingredientMinStockField, "Minimum stock");
		Double unitCost = parseDouble(ingredientUnitCostField, "Unit cost");
		LocalDate expiry = ingredientExpiryDatePicker.getValue();
		String unit = Optional.ofNullable(ingredientUnitField.getText()).map(String::trim).orElse("");

		if (quantity == null || minStock == null || unitCost == null) {
			return;
		}

		boolean differentNameExists = ingredients.stream()
				.filter(ingredient -> !Objects.equals(ingredient.getId(), selected.getId()))
				.anyMatch(ingredient -> ingredient.getName().equalsIgnoreCase(name));
		if (differentNameExists) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Another ingredient already uses this name.");
			return;
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
			return;
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
			clearIngredientForm();
			ingredientsTable.getSelectionModel().clearSelection();
			wasteTable.refresh();
			showAlert(Alert.AlertType.INFORMATION, "Deleted", "Ingredient removed.");
		}
	}

	@FXML
	private void handleClearIngredientForm() {
		ingredientsTable.getSelectionModel().clearSelection();
		clearIngredientForm();
	}

	@FXML
	private void handleRecordWaste() {
		Ingredient ingredient = wasteIngredientCombo.getValue();
		if (ingredient == null) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Select an ingredient for the waste record.");
			return;
		}

		Double quantity = parseDouble(wasteQuantityField, "Waste quantity");
		if (quantity == null) {
			return;
		}

		if (quantity <= 0) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Waste quantity must be positive.");
			return;
		}

		if (ingredient.getQuantityInStock() < quantity) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Not enough stock to record this waste.");
			return;
		}

		String wasteType = wasteTypeCombo.getValue();
		if (wasteType == null || wasteType.isBlank()) {
			showAlert(Alert.AlertType.ERROR, "Validation", "Choose a waste type.");
			return;
		}

		String reason = Optional.ofNullable(wasteReasonField.getText()).map(String::trim).orElse("");
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
				if (reason.isBlank()) {
					insertWaste.setNull(5, Types.VARCHAR);
				} else {
					insertWaste.setString(5, reason);
				}
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
				try {
					connection.rollback();
				} catch (SQLException ignored) {
					// Ignore rollback failure
				}
				showDatabaseError("Unable to record waste", e);
				return;
			} finally {
				try {
					connection.setAutoCommit(initialAutoCommit);
				} catch (SQLException ignored) {
					// Ignore reset failure
				}
			}
		} catch (SQLException e) {
			showDatabaseError("Unable to record waste", e);
			return;
		}

		ingredient.setQuantityInStock(ingredient.getQuantityInStock() - quantity);
		wasteRecords.add(0, record);
		ingredientsTable.refresh();

		if (ingredient.getQuantityInStock() < ingredient.getMinStockLevel()) {
			showAlert(Alert.AlertType.WARNING, "Low stock", ingredient.getName() + " is below the minimum stock level.");
		} else {
			showAlert(Alert.AlertType.INFORMATION, "Success", "Waste recorded successfully.");
		}

		clearWasteForm();
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
		}
	}

	@FXML
	private void handleClearWasteForm() {
		clearWasteForm();
	}

	private void populateIngredientForm(Ingredient ingredient) {
		ingredientNameField.setText(ingredient.getName());
		ingredientQuantityField.setText(Double.toString(ingredient.getQuantityInStock()));
		ingredientUnitField.setText(ingredient.getUnit());
		ingredientMinStockField.setText(Double.toString(ingredient.getMinStockLevel()));
		ingredientUnitCostField.setText(Double.toString(ingredient.getUnitCost()));
		ingredientExpiryDatePicker.setValue(ingredient.getExpiryDate());
	}

	private void clearIngredientForm() {
		ingredientNameField.clear();
		ingredientQuantityField.clear();
		ingredientUnitField.clear();
		ingredientMinStockField.clear();
		ingredientUnitCostField.clear();
		ingredientExpiryDatePicker.setValue(null);
	}

	private void clearWasteForm() {
		wasteIngredientCombo.getSelectionModel().clearSelection();
		wasteQuantityField.clear();
		wasteTypeCombo.getSelectionModel().clearSelection();
		wasteReasonField.clear();
		wasteTable.getSelectionModel().clearSelection();
	}

	private Double parseDouble(TextField field, String label) {
		String content = Optional.ofNullable(field.getText()).map(String::trim).orElse("");
		if (content.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Validation", label + " is required.");
			return null;
		}
		try {
			return Double.parseDouble(content);
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
	}

	private Ingredient ingredientById(Long id) {
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
}
