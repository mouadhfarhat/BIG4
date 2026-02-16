package Controllers;

import Entities.Dish;
import Entities.Menu;
import Services.DishService;
import Services.MenuService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    // ===== Services =====
    private final MenuService menuService = new MenuService();
    private final DishService dishService = new DishService();

    // ===== State =====
    private Menu selectedMenu;

    @FXML
    public void initialize() {
        // Menu columns
        colMenuId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colMenuTitle.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        colMenuDesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
        colMenuActive.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("active"));

        // Dish columns
        colDishId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colDishName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colDishDesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
        colDishPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("base_price"));
        colDishAvail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("available"));
        colDishStock.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock_quantity"));
        colDishImage.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("image_url"));

        // Selection listeners
        tvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            selectedMenu = newV;
            onMenuSelected(newV);
        });

        tvDishes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) fillDishForm(newV);
        });

        // Load initial data
        refreshMenus();
        refreshDishesForSelectedMenu();
    }

    // ===================== MENUS =====================

    private void onMenuSelected(Menu m) {
        lblMenuMsg.setText("");
        lblDishMsg.setText("");
        if (m == null) {
            lblSelectedMenu.setText("Selected menu: (none)");
            clearDishForm();
            tvDishes.setItems(FXCollections.observableArrayList());
            return;
        }

        lblSelectedMenu.setText("Selected menu: " + m.getId() + " - " + m.getTitle());
        fillMenuForm(m);
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

    private void fillMenuForm(Menu m) {
        tfMenuTitle.setText(m.getTitle());
        taMenuDesc.setText(m.getDescription());
        cbMenuActive.setSelected(m.isActive());
    }

    @FXML
    private void onAddMenu() {
        lblMenuMsg.setText("");
        try {
            String title = tfMenuTitle.getText().trim();
            String desc = taMenuDesc.getText().trim();

            if (title.isEmpty()) {
                lblMenuMsg.setText("Title is required.");
                return;
            }

            Menu m = new Menu();
            m.setTitle(title);
            m.setDescription(desc);
            m.setActive(cbMenuActive.isSelected());

            menuService.addMenu2(m);

            onClearMenu();
            refreshMenus();

        } catch (SQLException e) {
            lblMenuMsg.setText("Add menu failed: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateMenu() {
        lblMenuMsg.setText("");
        Menu m = tvMenus.getSelectionModel().getSelectedItem();
        if (m == null) {
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

            m.setTitle(title);
            m.setDescription(desc);
            m.setActive(cbMenuActive.isSelected());

            menuService.updateMenu(m);

            refreshMenus();

        } catch (SQLException e) {
            lblMenuMsg.setText("Update menu failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteMenu() {
        lblMenuMsg.setText("");

        Menu m = tvMenus.getSelectionModel().getSelectedItem();
        if (m == null) {
            lblMenuMsg.setText("Select a menu first.");
            return;
        }

        try {
            menuService.delete(m.getId());

            // because ON DELETE CASCADE -> dishes will be deleted too
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

    // ===================== DISHES =====================

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
        lblDishMsg.setText("");

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

            float price;
            try { price = Float.parseFloat(tfDishPrice.getText().trim()); }
            catch (Exception ex) { lblDishMsg.setText("Invalid price."); return; }

            int stock;
            try { stock = Integer.parseInt(tfDishStock.getText().trim()); }
            catch (Exception ex) { lblDishMsg.setText("Invalid stock quantity."); return; }

            Dish d = new Dish();
            d.setMenu_id(selectedMenu.getId());  // ✅ CRITICAL (no more 0)
            d.setName(name);
            d.setDescription(desc);
            d.setBase_price(price);
            d.setAvailable(cbDishAvailable.isSelected());
            d.setStock_quantity(stock);
            d.setImage_url(img);

            // because your DishService requires timestamps:
            d.setCreated_at(Timestamp.from(Instant.now()));
            d.setUpdate_at(Timestamp.from(Instant.now()));

            dishService.add(d);

            onClearDish();
            refreshDishesForSelectedMenu();

        } catch (SQLException e) {
            lblDishMsg.setText("Add dish failed: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdateDish() {
        lblDishMsg.setText("");

        Dish d = tvDishes.getSelectionModel().getSelectedItem();
        if (d == null) {
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

            float price;
            try { price = Float.parseFloat(tfDishPrice.getText().trim()); }
            catch (Exception ex) { lblDishMsg.setText("Invalid price."); return; }

            int stock;
            try { stock = Integer.parseInt(tfDishStock.getText().trim()); }
            catch (Exception ex) { lblDishMsg.setText("Invalid stock quantity."); return; }

            d.setMenu_id(selectedMenu.getId());
            d.setName(name);
            d.setDescription(desc);
            d.setBase_price(price);
            d.setAvailable(cbDishAvailable.isSelected());
            d.setStock_quantity(stock);
            d.setImage_url(img);

            d.setUpdate_at(Timestamp.from(Instant.now()));

            dishService.update(d);

            refreshDishesForSelectedMenu();

        } catch (SQLException e) {
            lblDishMsg.setText("Update dish failed: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteDish() {
        lblDishMsg.setText("");

        Dish d = tvDishes.getSelectionModel().getSelectedItem();
        if (d == null) {
            lblDishMsg.setText("Select a dish first.");
            return;
        }

        try {
            dishService.delete(d.getId());
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

    private void clearDishForm() {
        tfDishName.clear();
        taDishDesc.clear();
        tfDishPrice.clear();
        cbDishAvailable.setSelected(false);
        tfDishStock.clear();
        tfDishImage.clear();
    }

    private void fillDishForm(Dish d) {
        tfDishName.setText(d.getName());
        taDishDesc.setText(d.getDescription());
        tfDishPrice.setText(String.valueOf(d.getBase_price()));
        cbDishAvailable.setSelected(d.getAvailable() != null && d.getAvailable());
        tfDishStock.setText(String.valueOf(d.getStock_quantity()));
        tfDishImage.setText(d.getImage_url());
    }

    @FXML
    private void onRefreshDishes() {
        refreshDishesForSelectedMenu();
    }
}
