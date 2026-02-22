package Controllers;

import Entities.Ingredient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class IngredientDialogController {

	@FXML private Label titleLabel;
	@FXML private TextField nameField;
	@FXML private TextField unitField;
	@FXML private TextField quantityField;
	@FXML private TextField minStockField;
	@FXML private TextField unitCostField;
	@FXML private DatePicker expiryPicker;
	@FXML private Button primaryButton;

	private MainController host;
	private Stage stage;
	private boolean editMode;
	private Ingredient editing;

	public void setup(MainController host, Stage stage, Ingredient editing, boolean editMode) {
		this.host = host;
		this.stage = stage;
		this.editMode = editMode;
		this.editing = editing;
		if (titleLabel != null) {
			titleLabel.setText(editMode && editing != null ? "Edit " + editing.getName() : "Add Ingredient");
		}
		if (primaryButton != null) {
			primaryButton.setText(editMode ? "Update" : "Save");
		}
		if (editMode && editing != null) {
			nameField.setText(editing.getName());
			quantityField.setText(Double.toString(editing.getQuantityInStock()));
			unitField.setText(editing.getUnit());
			minStockField.setText(Double.toString(editing.getMinStockLevel()));
			unitCostField.setText(Double.toString(editing.getUnitCost()));
			expiryPicker.setValue(editing.getExpiryDate());
		}
	}

	@FXML
	private void handlePrimary() {
		if (host == null || stage == null) return;
		IngredientFormData data = new IngredientFormData(
				editing != null ? editing.getId() : null,
				nameField.getText(),
				quantityField.getText(),
				unitField.getText(),
				minStockField.getText(),
				unitCostField.getText(),
				expiryPicker.getValue()
		);
		boolean ok = editMode ? host.saveIngredientUpdate(data) : host.saveIngredientCreate(data);
		if (ok) stage.close();
	}

	@FXML
	private void handleCancel() {
		if (stage != null) stage.close();
	}

	public record IngredientFormData(Long id,
								  String name,
								  String quantity,
								  String unit,
								  String minStock,
								  String unitCost,
								  java.time.LocalDate expiry) {
	}
}
