package Controllers;

import Entities.Dish;
import Entities.Menu;
import Services.DishService;
import Services.MenuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class MenuDishCrudController {

    // ===== MENUS UI =====
    @FXML private Label lblSelectedMenu;
    @FXML private TableView<Menu> tvMenus;
    @FXML private TableColumn<Menu, Integer> colMenuId;
    @FXML private TableColumn<Menu, String> colMenuTitle;
    @FXML private TableColumn<Menu, String> colMenuDesc;
    @FXML private TableColumn<Menu, Boolean> colMenuActive;

    @FXML private TextField tfMenuTitle;
    @FXML private TextArea taMenuDesc;
    @FXML private CheckBox cbMenuActive;
    @FXML private Label lblMenuMsg;

    // ===== DISHES UI =====
    @FXML private TableView<Dish> tvDishes;
    @FXML private TableColumn<Dish, Integer> colDishId;
    @FXML private TableColumn<Dish, String> colDishName;
    @FXML private TableColumn<Dish, String> colDishDesc;
    @FXML private TableColumn<Dish, Float> colDishPrice;
    @FXML private TableColumn<Dish, Boolean> colDishAvail;
    @FXML private TableColumn<Dish, Integer> colDishStock;
    @FXML private TableColumn<Dish, String> colDishImage;

    @FXML private TextField tfDishName;
    @FXML private TextArea taDishDesc;
    @FXML private TextField tfDishPrice;
    @FXML private CheckBox cbDishAvailable;
    @FXML private TextField tfDishStock;
    @FXML private TextField tfDishImage;
    @FXML private Label lblDishMsg;

    private final MenuService menuService = new MenuService();
    private final DishService dishService = new DishService();

    private Menu selectedMenu;

    @FXML
    public void initialize() {
        configureMenuTable();
        configureDishTable();

        tvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedMenu = newV;
            onMenuSelected(newV);
        });

        tvDishes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                fillDishForm(newV);
            }
        });

        refreshMenus();
        refreshDishesForSelectedMenu();
    }

    private void configureMenuTable() {
        colMenuId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMenuTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMenuDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMenuActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    private void configureDishTable() {
        colDishId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDishName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDishDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDishPrice.setCellValueFactory(new PropertyValueFactory<>("base_price"));
        colDishAvail.setCellValueFactory(new PropertyValueFactory<>("available"));
        colDishStock.setCellValueFactory(new PropertyValueFactory<>("stock_quantity"));
        colDishImage.setCellValueFactory(new PropertyValueFactory<>("image_url"));
    }

    private void onMenuSelected(Menu menu) {
        clearMessages();
        if (menu == null) {
            lblSelectedMenu.setText("Selected menu: (none)");
            clearDishForm();
            tvDishes.setItems(FXCollections.observableArrayList());
            return;
        }

        lblSelectedMenu.setText("Selected menu: " + menu.getId() + " - " + menu.getTitle());
        fillMenuForm(menu);
        refreshDishesForSelectedMenu();
    }

    private void refreshMenus() {
        try {
            ObservableList<Menu> data = FXCollections.observableArrayList(menuService.getallMenu());
            tvMenus.setItems(data);
        } catch (SQLException e) {
            lblMenuMsg.setText("DB error (menus): " + e.getMessage());
        }
    }

    private void fillMenuForm(Menu menu) {
        tfMenuTitle.setText(menu.getTitle());
        taMenuDesc.setText(menu.getDescription());
        cbMenuActive.setSelected(menu.isActive());
    }

    @FXML
    private void onAddMenu() {
        clearMessages();
        try {
            String title = tfMenuTitle.getText().trim();
            String desc = taMenuDesc.getText().trim();

            if (title.isEmpty()) {
                lblMenuMsg.setText("Title is required.");
                return;
            }

            Menu menu = new Menu();
            menu.setTitle(title);
            menu.setDescription(desc);
            menu.setActive(cbMenuActive.isSelected());

            menuService.addMenu2(menu);

            onClearMenu();
            refreshMenus();

        } catch (SQLException e) {
            lblMenuMsg.setText("Add menu failed: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateMenu() {
        clearMessages();
        Menu menu = tvMenus.getSelectionModel().getSelectedItem();
        if (menu == null) {
            lblMenuMsg.setText("Select a menu first.");
            return;
        }

        try {
            String title = tfMenuTitle.getText().trim();
            String desc = taMenuDesc.getText().trim();
            if (title.isEmpty()) {
                lblMenuMsg.setText("Title is required.");
                return;
            }

            menu.setTitle(title);
            menu.setDescription(desc);
            menu.setActive(cbMenuActive.isSelected());

            menuService.updateMenu(menu);

            refreshMenus();

        } catch (SQLException e) {
            lblMenuMsg.setText("Update menu failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteMenu() {
        clearMessages();

        Menu menu = tvMenus.getSelectionModel().getSelectedItem();
        if (menu == null) {
            lblMenuMsg.setText("Select a menu first.");
            return;
        }

        try {
            menuService.delete(menu.getId());
            selectedMenu = null;
            tvMenus.getSelectionModel().clearSelection();
            lblSelectedMenu.setText("Selected menu: (none)");
            tvDishes.setItems(FXCollections.observableArrayList());

            onClearMenu();
            onClearDish();
            refreshMenus();

        } catch (SQLException e) {
            lblMenuMsg.setText("Delete menu failed: " + e.getMessage());
        }
    }

    @FXML
    private void onClearMenu() {
        tfMenuTitle.clear();
        taMenuDesc.clear();
        cbMenuActive.setSelected(false);
        lblMenuMsg.setText("");
    }

    @FXML
    private void onRefreshMenus() {
        refreshMenus();
    }

    private void refreshDishesForSelectedMenu() {
        if (selectedMenu == null) {
            tvDishes.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            ObservableList<Dish> data = FXCollections.observableArrayList(
                    dishService.getByMenuId(selectedMenu.getId())
            );
            tvDishes.setItems(data);
        } catch (SQLException e) {
            lblDishMsg.setText("DB error (dishes): " + e.getMessage());
        }
    }

    @FXML
    private void onAddDish() {
        clearMessages();

        if (selectedMenu == null) {
            lblDishMsg.setText("Select a menu first (left table).");
            return;
        }

        try {
            String name = tfDishName.getText().trim();
            String desc = taDishDesc.getText().trim();
            String img = tfDishImage.getText().trim();

            if (name.isEmpty()) {
                lblDishMsg.setText("Dish name is required.");
                return;
            }

            Float price = parseFloat(tfDishPrice.getText(), "price");
            if (price == null) {
                return;
            }

            Integer stock = parseInt(tfDishStock.getText(), "stock quantity");
            if (stock == null) {
                return;
            }

            Dish dish = new Dish();
            dish.setMenu_id(selectedMenu.getId());
            dish.setName(name);
            dish.setDescription(desc);
            dish.setBase_price(price);
            dish.setAvailable(cbDishAvailable.isSelected());
            dish.setStock_quantity(stock);
            dish.setImage_url(img);
            dish.setCreated_at(Timestamp.from(Instant.now()));
            dish.setUpdate_at(Timestamp.from(Instant.now()));

            dishService.add(dish);

            onClearDish();
            refreshDishesForSelectedMenu();

        } catch (SQLException e) {
            lblDishMsg.setText("Add dish failed: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateDish() {
        clearMessages();

        Dish dish = tvDishes.getSelectionModel().getSelectedItem();
        if (dish == null) {
            lblDishMsg.setText("Select a dish first.");
            return;
        }
        if (selectedMenu == null) {
            lblDishMsg.setText("Select a menu first.");
            return;
        }

        try {
            String name = tfDishName.getText().trim();
            String desc = taDishDesc.getText().trim();
            String img = tfDishImage.getText().trim();

            if (name.isEmpty()) {
                lblDishMsg.setText("Dish name is required.");
                return;
            }

            Float price = parseFloat(tfDishPrice.getText(), "price");
            if (price == null) {
                return;
            }

            Integer stock = parseInt(tfDishStock.getText(), "stock quantity");
            if (stock == null) {
                return;
            }

            dish.setMenu_id(selectedMenu.getId());
            dish.setName(name);
            dish.setDescription(desc);
            dish.setBase_price(price);
            dish.setAvailable(cbDishAvailable.isSelected());
            dish.setStock_quantity(stock);
            dish.setImage_url(img);
            dish.setUpdate_at(Timestamp.from(Instant.now()));

            dishService.update(dish);

            refreshDishesForSelectedMenu();

        } catch (SQLException e) {
            lblDishMsg.setText("Update dish failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteDish() {
        clearMessages();

        Dish dish = tvDishes.getSelectionModel().getSelectedItem();
        if (dish == null) {
            lblDishMsg.setText("Select a dish first.");
            return;
        }

        try {
            dishService.delete(dish.getId());
            onClearDish();
            refreshDishesForSelectedMenu();

        } catch (SQLException e) {
            lblDishMsg.setText("Delete dish failed: " + e.getMessage());
        }
    }

    @FXML
    private void onClearDish() {
        clearDishForm();
        lblDishMsg.setText("");
    }

    @FXML
    private void onRefreshDishes() {
        refreshDishesForSelectedMenu();
    }

    private void clearDishForm() {
        tfDishName.clear();
        taDishDesc.clear();
        tfDishPrice.clear();
        cbDishAvailable.setSelected(false);
        tfDishStock.clear();
        tfDishImage.clear();
    }

    private void fillDishForm(Dish dish) {
        tfDishName.setText(dish.getName());
        taDishDesc.setText(dish.getDescription());
        tfDishPrice.setText(String.valueOf(dish.getBase_price()));
        cbDishAvailable.setSelected(dish.getAvailable() != null && dish.getAvailable());
        tfDishStock.setText(String.valueOf(dish.getStock_quantity()));
        tfDishImage.setText(dish.getImage_url());
    }

    private Float parseFloat(String value, String label) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException ex) {
            showValidationAlert("Invalid " + label + ".");
            return null;
        }
    }

    private Integer parseInt(String value, String label) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            showValidationAlert("Invalid " + label + ".");
            return null;
        }
    }

    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void clearMessages() {
        lblMenuMsg.setText("");
        lblDishMsg.setText("");
    }
}
