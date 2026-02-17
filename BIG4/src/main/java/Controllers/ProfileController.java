package Controllers;

import Entities.DeliveryMan;
import Entities.User;
import Services.AuthService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ProfileController {

    @FXML private Label titleLabel;
    @FXML private Label roleLabel;
    @FXML private Label messageLabel;
    @FXML private VBox clientAddressBox;
    @FXML private VBox deliveryExtraBox;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private javafx.scene.control.TextArea addressField;
    @FXML private TextField vehicleTypeField;
    @FXML private TextField vehicleNumberField;
    @FXML private TextArea deliveryAddressField;
    @FXML private Button saveBtn;
    @FXML private Button deleteAccountBtn;
    @FXML private Button backBtn;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }
        loadRoleLabel();
        loadProfile();
        setRoleSpecificVisibility();
    }

    private void loadRoleLabel() {
        String role = currentUser.getRole();
        if ("CLIENT".equals(role)) roleLabel.setText("Diner");
        else if ("DELIVERY_MAN".equals(role)) roleLabel.setText("Delivery driver");
        else roleLabel.setText("Administrator");
    }

    private void setRoleSpecificVisibility() {
        clientAddressBox.setVisible(currentUser.isClient());
        clientAddressBox.setManaged(currentUser.isClient());
        deliveryExtraBox.setVisible(currentUser.isDeliveryMan());
        deliveryExtraBox.setManaged(currentUser.isDeliveryMan());
        deleteAccountBtn.setVisible(currentUser.isClient());
        deleteAccountBtn.setManaged(currentUser.isClient());
    }

    private void loadProfile() {
        try {
            User u = userService.getUserById(currentUser.getId());
            if (u != null) currentUser = u;
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            if (addressField != null) addressField.setText(currentUser.getAddress());

            if (currentUser.isDeliveryMan()) {
                DeliveryMan dm = userService.getDeliveryManByUserId(currentUser.getId());
                if (dm != null) {
                    vehicleTypeField.setText(dm.getVehicleType());
                    vehicleNumberField.setText(dm.getVehicleNumber());
                    deliveryAddressField.setText(dm.getAddress());
                }
            }
        } catch (SQLException e) {
            setMessage("Could not load profile: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSave() {
        setMessage("", false);
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        if (fullName == null || fullName.trim().isEmpty()) {
            setMessage("Full name is required.", true);
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            setMessage("Email is required.", true);
            return;
        }
        try {
            if (currentUser.isDeliveryMan()) {
                String vehicleType = vehicleTypeField.getText();
                String vehicleNumber = vehicleNumberField.getText();
                String address = deliveryAddressField.getText();
                userService.updateDeliveryManProfile(currentUser.getId(), currentUser.getReferenceId(),
                        fullName.trim(), email.trim(), phone != null ? phone.trim() : null,
                        vehicleType != null ? vehicleType.trim() : null,
                        vehicleNumber != null ? vehicleNumber.trim() : null,
                        address != null ? address.trim() : null);
            } else {
                String address = (addressField != null) ? addressField.getText() : null;
                userService.updateUserProfile(currentUser.getId(), fullName.trim(), email.trim(),
                        phone != null ? phone.trim() : null, address != null ? address.trim() : null);
            }
            AuthService.setCurrentUser(userService.getUserById(currentUser.getId()));
            setMessage("Profile updated successfully.", false);
        } catch (SQLException e) {
            setMessage("Update failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleDeleteAccount() {
        if (!currentUser.isClient()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete account");
        confirm.setHeaderText("Permanently delete your account?");
        confirm.setContentText("Your account and data will be removed. You will need to sign up again to use Big4. This cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            userService.deleteUser(currentUser.getId());
            AuthService.logout();
            goToLogin();
        } catch (SQLException e) {
            setMessage("Could not delete account: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleBack() {
        navigateToHomeByRole();
    }

    private void navigateToHomeByRole() {
        String fxml;
        String title;
        int w = 1250, h = 800;
        if (currentUser.isClient()) { fxml = "/homepage.fxml"; title = "Big4 - Welcome"; }
        else if (currentUser.isDeliveryMan()) { fxml = "/DeliveryView.fxml"; title = "DeliveryMan View - Big4"; }
        else { fxml = "/AdminDelivery.fxml"; title = "Admin Delivery - Big4"; }
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            goToLogin();
        }
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) (backBtn != null && backBtn.getScene() != null ? backBtn.getScene().getWindow() : saveBtn.getScene().getWindow());
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("Big4 - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMessage(String text, boolean error) {
        messageLabel.setText(text);
        messageLabel.setStyle(error ? "-fx-text-fill: #dc2626; -fx-font-size: 12;" : "-fx-text-fill: #16a34a; -fx-font-size: 12;");
    }
}
