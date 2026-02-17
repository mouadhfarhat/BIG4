package Controllers;

import Entities.User;
import Services.AuthService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserManagementController {

    @FXML private TableView<User> clientsTable;
    @FXML private TableColumn<User, Long> clientIdCol;
    @FXML private TableColumn<User, String> clientNameCol;
    @FXML private TableColumn<User, String> clientEmailCol;
    @FXML private TableColumn<User, String> clientPhoneCol;
    @FXML private TableColumn<User, Void> clientActionsCol;

    @FXML private TableView<User> deliveryMenTable;
    @FXML private TableColumn<User, Long> dmIdCol;
    @FXML private TableColumn<User, String> dmNameCol;
    @FXML private TableColumn<User, String> dmEmailCol;
    @FXML private TableColumn<User, String> dmPhoneCol;
    @FXML private TableColumn<User, Void> dmActionsCol;

    @FXML private Button backBtn;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        User admin = AuthService.getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            goToLogin();
            return;
        }
        setupClientsTable();
        setupDeliveryMenTable();
        loadClients();
        loadDeliveryMen();
    }

    private void setupClientsTable() {
        clientIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        clientEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        clientPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            { deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12;"); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    deleteBtn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()), "client"));
                    setGraphic(new HBox(deleteBtn));
                }
            }
        });
    }

    private void setupDeliveryMenTable() {
        dmIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        dmNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        dmEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        dmPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dmActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            { deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12;"); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    deleteBtn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()), "driver"));
                    setGraphic(new HBox(deleteBtn));
                }
            }
        });
    }

    private void loadClients() {
        try {
            List<User> list = userService.listClients();
            clientsTable.getItems().setAll(list);
        } catch (SQLException e) {
            setMessage("Could not load clients: " + e.getMessage());
        }
    }

    private void loadDeliveryMen() {
        try {
            List<User> list = userService.listDeliveryMen();
            deliveryMenTable.getItems().setAll(list);
        } catch (SQLException e) {
            setMessage("Could not load delivery drivers: " + e.getMessage());
        }
    }

    private void confirmAndDelete(User target, String type) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete " + type);
        confirm.setHeaderText("Delete this account?");
        confirm.setContentText(target.getFullName() + " (" + target.getEmail() + ") will lose access. This cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        User admin = AuthService.getCurrentUser();
        if (admin == null || !admin.isAdmin()) return;
        try {
            userService.deleteUserByAdmin(admin.getId(), target.getId());
            setMessage("");
            loadClients();
            loadDeliveryMen();
        } catch (SecurityException e) {
            setMessage(e.getMessage());
        } catch (SQLException e) {
            setMessage("Delete failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDelivery.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Admin Delivery - Big4");
        } catch (IOException e) {
            goToLogin();
        }
    }

    private void goToLogin() {
        try {
            AuthService.logout();
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("Big4 - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMessage(String text) {
        messageLabel.setText(text);
    }
}
