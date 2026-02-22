package Controllers;

import Entities.Ingredient;
import Entities.WasteRecord;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class WasteDialogController {

	@FXML private Label titleLabel;
	@FXML private ComboBox<Ingredient> ingredientCombo;
	@FXML private TextField quantityField;
	@FXML private ComboBox<String> typeCombo;
	@FXML private TextArea reasonField;
	@FXML private Button primaryButton;

	private MainController host;
	private Stage stage;
	private boolean editMode;
	private WasteRecord editing;

	public void setup(MainController host, Stage stage, WasteRecord editing, boolean editMode,
					ObservableList<Ingredient> ingredients, ObservableList<String> wasteTypes) {
		this.host = host;
		this.stage = stage;
		this.editMode = editMode;
		this.editing = editing;
		ingredientCombo.setItems(ingredients);
		ingredientCombo.setCellFactory(box -> new ListCell<>() {
			@Override
			protected void updateItem(Ingredient item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});
		ingredientCombo.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Ingredient item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});
		ingredientCombo.setConverter(new StringConverter<>() {
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
		typeCombo.setItems(wasteTypes);
		if (titleLabel != null) {
			titleLabel.setText(editMode ? "Edit Waste Record" : "Record Waste");
		}
		if (primaryButton != null) {
			primaryButton.setText(editMode ? "Update" : "Save");
		}
		if (editMode && editing != null) {
			ingredientCombo.setValue(host.ingredientById(editing.getIngredientId()));
			quantityField.setText(Double.toString(editing.getQuantityWasted()));
			typeCombo.setValue(editing.getWasteType());
			reasonField.setText(editing.getReason());
		}
	}

	@FXML
	private void handlePrimary() {
		if (host == null || stage == null) return;
		WasteFormData data = new WasteFormData(
				ingredientCombo.getValue(),
				quantityField.getText(),
				typeCombo.getValue(),
				reasonField.getText()
		);
		boolean ok = editMode ? host.saveWasteUpdate(data) : host.saveWasteCreate(data);
		if (ok) stage.close();
	}

	@FXML
	private void handleCancel() {
		if (stage != null) stage.close();
	}

	public record WasteFormData(Ingredient ingredient,
							 String quantity,
							 String wasteType,
							 String reason) {
	}
}
