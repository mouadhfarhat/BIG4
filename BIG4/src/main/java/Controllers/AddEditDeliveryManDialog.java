package Controllers;

import Entities.DeliveryMan;
import Services.DeliverymanService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddEditDeliveryManDialog {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> vehicleTypeCombo;
    @FXML private TextField vehicleNumberField;
    @FXML private TextArea addressField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker joiningDatePicker;
    @FXML private TextField salaryField;
    @FXML private TextField ratingField;
    @FXML private Label validationLabel;
    @FXML private Button saveBtn;

    private DeliverymanService deliverymanService;
    private DeliveryMan currentDeliveryMan;
    private String mode; // "ADD" or "EDIT"
    private DeliverymanManagement parentController;

    @FXML
    public void initialize() {
        deliverymanService = new DeliverymanService();
        setDefaultValues();
    }

    /**
     * Set default values for new delivery man
     */
    private void setDefaultValues() {
        statusCombo.setValue("ACTIVE");
        vehicleTypeCombo.setValue("Motorcycle");
        ratingField.setText("0.0");
        joiningDatePicker.setValue(LocalDate.now());
    }

    /**
     * Set the mode (ADD or EDIT)
     */
    public void setMode(String mode) {
        this.mode = mode;
        if ("EDIT".equals(mode)) {
            titleLabel.setText("Edit Delivery Man");
            saveBtn.setText("Update");
        } else {
            titleLabel.setText("Add New Delivery Man");
            saveBtn.setText("Add");
        }
    }

    /**
     * Set delivery man data (for EDIT mode)
     */
    public void setDeliveryMan(DeliveryMan deliveryMan) {
        this.currentDeliveryMan = deliveryMan;
        populateFields();
    }

    /**
     * Populate fields with delivery man data
     */
    private void populateFields() {
        nameField.setText(currentDeliveryMan.getName());
        phoneField.setText(currentDeliveryMan.getPhone());
        emailField.setText(currentDeliveryMan.getEmail());
        vehicleTypeCombo.setValue(currentDeliveryMan.getVehicleType());
        vehicleNumberField.setText(currentDeliveryMan.getVehicleNumber());
        addressField.setText(currentDeliveryMan.getAddress() != null ? currentDeliveryMan.getAddress() : "");
        statusCombo.setValue(currentDeliveryMan.getStatus());

        if (currentDeliveryMan.getDateOfJoining() != null) {
            joiningDatePicker.setValue(currentDeliveryMan.getDateOfJoining());
        }

        if (currentDeliveryMan.getSalary() != null) {
            salaryField.setText(String.valueOf(currentDeliveryMan.getSalary()));
        }
        if (currentDeliveryMan.getRating() != null) {
            ratingField.setText(String.valueOf(currentDeliveryMan.getRating()));
        }
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        validationLabel.setText("");

        if (nameField.getText().trim().isEmpty()) {
            setValidationError("Name is required");
            return false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            setValidationError("Phone is required");
            return false;
        }

        if (!isValidPhone(phoneField.getText().trim())) {
            setValidationError("Phone must contain only digits and be at least 8 characters");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            setValidationError("Email is required");
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            setValidationError("Invalid email format");
            return false;
        }

        if (vehicleTypeCombo.getValue() == null) {
            setValidationError("Vehicle type is required");
            return false;
        }

        if (vehicleNumberField.getText().trim().isEmpty()) {
            setValidationError("Vehicle number is required");
            return false;
        }

        if (statusCombo.getValue() == null) {
            setValidationError("Status is required");
            return false;
        }

        // Validate salary if provided
        if (!salaryField.getText().trim().isEmpty()) {
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (salary < 0) {
                    setValidationError("Salary cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                setValidationError("Salary must be a valid number");
                return false;
            }
        }

        // Validate rating if provided
        if (!ratingField.getText().trim().isEmpty()) {
            try {
                double rating = Double.parseDouble(ratingField.getText().trim());
                if (rating < 0 || rating > 5) {
                    setValidationError("Rating must be between 0 and 5");
                    return false;
                }
            } catch (NumberFormatException e) {
                setValidationError("Rating must be a valid number");
                return false;
            }
        }

        return true;
    }

    /**
     * Check if phone is valid
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8,}");
    }

    /**
     * Check if email is valid
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Set validation error message
     */
    private void setValidationError(String message) {
        validationLabel.setText(message);
        validationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    /**
     * Handle Save button
     */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            DeliveryMan deliveryMan = createDeliveryManFromFields();

            if ("ADD".equals(mode)) {
                deliverymanService.addDeliveryMan2(deliveryMan);
                showSuccess("Delivery man added successfully");
            } else {
                deliveryMan.setDeliveryManId(currentDeliveryMan.getDeliveryManId());
                deliverymanService.updateDeliveryMan2(deliveryMan);
                showSuccess("Delivery man updated successfully");
            }

            parentController.refreshTable();
            closeDialog();
        } catch (SQLException e) {
            setValidationError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create DeliveryMan object from form fields
     */
    private DeliveryMan createDeliveryManFromFields() {
        DeliveryMan dm = new DeliveryMan();
        dm.setName(nameField.getText().trim());
        dm.setPhone(phoneField.getText().trim());
        dm.setEmail(emailField.getText().trim());
        dm.setVehicleType(vehicleTypeCombo.getValue());
        dm.setVehicleNumber(vehicleNumberField.getText().trim());
        dm.setAddress(addressField.getText().trim());
        dm.setStatus(statusCombo.getValue());
        dm.setDateOfJoining(joiningDatePicker.getValue());

        if (!salaryField.getText().trim().isEmpty()) {
            dm.setSalary(Double.parseDouble(salaryField.getText().trim()));
        } else {
            dm.setSalary(0.0);
        }

        if (!ratingField.getText().trim().isEmpty()) {
            dm.setRating(Double.parseDouble(ratingField.getText().trim()));
        } else {
            dm.setRating(0.0);
        }

        return dm;
    }

    /**
     * Handle Cancel button
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    /**
     * Set parent controller
     */
    public void setParentController(DeliverymanManagement controller) {
        this.parentController = controller;
    }
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}