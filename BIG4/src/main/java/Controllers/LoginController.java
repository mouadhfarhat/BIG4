package Controllers;

import Entities.User;
import Services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    private static final String ROLE_DELIVERY_MAN = "DELIVERY_MAN";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CLIENT = "CLIENT";
    private static final java.util.List<String> LOGIN_ROLES = java.util.Arrays.asList("Delivery Man", "Admin", "Client");
    private static final java.util.List<String> SIGNUP_ROLES = java.util.Arrays.asList("Delivery Man", "Client");

    @FXML private ComboBox<String> roleCombo;
    @FXML private VBox loginPanel;
    @FXML private VBox signUpPanel;
    @FXML private HBox backToLoginBox;
    @FXML private HBox deliveryExtraBox;
    @FXML private Hyperlink showSignUpLink;
    @FXML private HBox loginSwitchBox;
    @FXML private Label messageLabel;

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;

    @FXML private TextField signUpName;
    @FXML private TextField signUpEmail;
    @FXML private PasswordField signUpPassword;
    @FXML private PasswordField signUpConfirmPassword;
    @FXML private TextField signUpPhone;
    @FXML private TextField signUpVehicleType;
    @FXML private TextField signUpVehicleNumber;
    @FXML private Button signUpButton;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleCombo.getItems().setAll(LOGIN_ROLES);
        roleCombo.getSelectionModel().selectFirst();
        roleCombo.setOnAction(e -> updateDeliveryExtraVisibility());
        clearMessage();
    }

    private String getSelectedRoleCode() {
        String selected = roleCombo.getSelectionModel().getSelectedItem();
        if (selected == null) return ROLE_CLIENT;
        switch (selected) {
            case "Delivery Man": return ROLE_DELIVERY_MAN;
            case "Admin": return ROLE_ADMIN;
            case "Client": return ROLE_CLIENT;
            default: return ROLE_CLIENT;
        }
    }

    private void updateDeliveryExtraVisibility() {
        boolean isDeliveryMan = ROLE_DELIVERY_MAN.equals(getSelectedRoleCode());
        deliveryExtraBox.setVisible(isDeliveryMan);
        deliveryExtraBox.setManaged(isDeliveryMan);
    }

    @FXML
    private void showSignUp() {
        roleCombo.getItems().setAll(SIGNUP_ROLES);
        roleCombo.getSelectionModel().selectFirst();
        loginPanel.setVisible(false);
        loginPanel.setManaged(false);
        signUpPanel.setVisible(true);
        signUpPanel.setManaged(true);
        backToLoginBox.setVisible(true);
        backToLoginBox.setManaged(true);
        if (loginSwitchBox != null) {
            loginSwitchBox.setVisible(false);
            loginSwitchBox.setManaged(false);
        }
        updateDeliveryExtraVisibility();
        clearMessage();
    }

    @FXML
    private void showLogin() {
        roleCombo.getItems().setAll(LOGIN_ROLES);
        roleCombo.getSelectionModel().selectFirst();
        signUpPanel.setVisible(false);
        signUpPanel.setManaged(false);
        backToLoginBox.setVisible(false);
        backToLoginBox.setManaged(false);
        loginPanel.setVisible(true);
        loginPanel.setManaged(true);
        if (loginSwitchBox != null) {
            loginSwitchBox.setVisible(true);
            loginSwitchBox.setManaged(true);
        }
        clearMessage();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12;");
    }

    private void setMessage(String text, boolean error) {
        messageLabel.setText(text);
        messageLabel.setStyle(error ? "-fx-text-fill: #f87171; -fx-font-size: 12;" : "-fx-text-fill: #4ade80; -fx-font-size: 12;");
    }

    @FXML
    private void handleLogin() {
        clearMessage();
        String email = loginEmail.getText();
        String password = loginPassword.getText();
        if (email == null || email.trim().isEmpty()) {
            setMessage("Please enter your email.", true);
            return;
        }
        if (password == null || password.isEmpty()) {
            setMessage("Please enter your password.", true);
            return;
        }
        String role = getSelectedRoleCode();
        try {
            User user = authService.login(email, password, role);
            if (user != null) {
                AuthService.setCurrentUser(user);
                navigateAfterAuth(user);
            } else {
                setMessage("Invalid email or password for this role.", true);
            }
        } catch (SQLException e) {
            setMessage("Database error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp() {
        clearMessage();
        String name = signUpName.getText();
        String email = signUpEmail.getText();
        String password = signUpPassword.getText();
        String confirm = signUpConfirmPassword.getText();
        String phone = signUpPhone.getText();
        String role = getSelectedRoleCode();

        if (email == null || email.trim().isEmpty()) {
            setMessage("Please enter your email.", true);
            return;
        }
        if (password == null || password.length() < 4) {
            setMessage("Password must be at least 4 characters.", true);
            return;
        }
        if (!password.equals(confirm)) {
            setMessage("Passwords do not match.", true);
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            name = email;
        }

        String vehicleType = null;
        String vehicleNumber = null;
        if (ROLE_DELIVERY_MAN.equals(role)) {
            vehicleType = signUpVehicleType.getText();
            vehicleNumber = signUpVehicleNumber.getText();
            if (vehicleType != null) vehicleType = vehicleType.trim().isEmpty() ? "Motorcycle" : vehicleType.trim();
            if (vehicleNumber != null) vehicleNumber = vehicleNumber.trim().isEmpty() ? "" : vehicleNumber.trim();
        }

        try {
            User user = authService.signUp(email, password, role, name.trim(), phone != null ? phone.trim() : null,
                    vehicleType, vehicleNumber);
            if (user != null) {
                AuthService.setCurrentUser(user);
                setMessage("Account created. Redirecting...", false);
                navigateAfterAuth(user);
            } else {
                setMessage("Sign up failed. Please try again.", true);
            }
        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), true);
        } catch (SQLException e) {
            setMessage("Database error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void navigateAfterAuth(User user) {
        String fxml;
        String title;
        int w = 1250;
        int h = 800;
        if (user.isClient()) {
            fxml = "/homepage.fxml";
            title = "Big4 - Welcome";
        } else if (user.isDeliveryMan()) {
            fxml = "/DeliveryView.fxml";
            title = "DeliveryMan View - Big4";
        } else if (user.isAdmin()) {
            fxml = "/AdminDelivery.fxml";
            title = "Admin Delivery - Big4";
        } else {
            fxml = "/homepage.fxml";
            title = "Big4";
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) roleCombo.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            showAlert("Navigation Error", "Could not open next screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
