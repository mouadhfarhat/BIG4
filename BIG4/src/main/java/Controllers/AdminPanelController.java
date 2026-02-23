package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminPanelController {

    @FXML private StackPane contentHolder;
    @FXML private Button dashboardBtn;
    @FXML private Button menuAdminBtn;
    @FXML private Button ingredientsBtn;
    @FXML private Button wasteBtn;

    private Parent dashboardView;
    private Parent menuAdminView;
    private Parent mainView;
    private MainController mainController;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        if (dashboardView == null) {
            dashboardView = loadView("/AdminDashboard.fxml", "Unable to load admin dashboard");
        }
        if (dashboardView != null) {
            contentHolder.getChildren().setAll(dashboardView);
            setActiveButton(dashboardBtn);
        }
    }

    @FXML
    private void showMenuAdmin() {
        if (menuAdminView == null) {
            menuAdminView = loadView("/menu-management.fxml", "Unable to load menu admin page");
        }
        if (menuAdminView != null) {
            contentHolder.getChildren().setAll(menuAdminView);
            setActiveButton(menuAdminBtn);
        }
    }

    @FXML
    private void showIngredients() {
        ensureMainViewLoaded();
        if (mainController != null && mainView != null) {
            contentHolder.getChildren().setAll(mainView);
            mainController.showStockTab();
            setActiveButton(ingredientsBtn);
        }
    }

    @FXML
    private void showWasteRecords() {
        ensureMainViewLoaded();
        if (mainController != null && mainView != null) {
            contentHolder.getChildren().setAll(mainView);
            mainController.showWasteTab();
            setActiveButton(wasteBtn);
        }
    }

    private void ensureMainViewLoaded() {
        if (mainView != null && mainController != null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            mainView = loader.load();
            mainController = loader.getController();
            mainController.hideOverviewSection();
        } catch (IOException e) {
            showLoadError("Unable to load admin pages", e);
        }
    }

    private Parent loadView(String fxmlPath, String errorPrefix) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            showLoadError(errorPrefix, e);
            return null;
        }
    }

    private void setActiveButton(Button activeButton) {
        dashboardBtn.getStyleClass().remove("admin-nav-button-active");
        menuAdminBtn.getStyleClass().remove("admin-nav-button-active");
        ingredientsBtn.getStyleClass().remove("admin-nav-button-active");
        wasteBtn.getStyleClass().remove("admin-nav-button-active");

        if (!activeButton.getStyleClass().contains("admin-nav-button-active")) {
            activeButton.getStyleClass().add("admin-nav-button-active");
        }
    }

    private void showLoadError(String errorPrefix, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Loading Error");
        alert.setContentText(errorPrefix + ".\n\nError: " + e.getMessage());
        alert.showAndWait();
    }
}
