package Controllers;

import Entities.Dish;
import Entities.DishIngredient;
import Entities.Ingredient;
import Entities.Menu;
import Services.DishIngredientService;
import Services.DishService;
import Services.MenuService;
import Utils.Mydatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuDishCrudController {

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
    @FXML private TextField tfDishImage;
    @FXML private ComboBox<Ingredient> cbRecipeIngredient;
    @FXML private TextField tfRecipeQuantity;
    @FXML private TableView<RecipeLine> tvRecipeIngredients;
    @FXML private TableColumn<RecipeLine, String> colRecipeIngredient;
    @FXML private TableColumn<RecipeLine, String> colRecipeUnit;
    @FXML private TableColumn<RecipeLine, Double> colRecipeQty;
    @FXML private Label lblDishMsg;

    private final MenuService menuService = new MenuService();
    private final DishService dishService = new DishService();
    private final DishIngredientService dishIngredientService = new DishIngredientService();

    private final ObservableList<RecipeLine> recipeLines = FXCollections.observableArrayList();
    private final Map<Long, Ingredient> ingredientById = new LinkedHashMap<>();

    private Menu selectedMenu;

    @FXML
    public void initialize() {
        try {
            dishIngredientService.ensureDishIngredientTableExists();
        } catch (SQLException e) {
            if (lblDishMsg != null) {
                lblDishMsg.setText("Unable to initialize dish ingredients table: " + e.getMessage());
            }
        }

        configureMenuTable();
        configureDishTable();
        configureRecipeEditor();

        tvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedMenu = newValue;
            onMenuSelected(newValue);
        });

        tvDishes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                fillDishForm(newValue);
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

    private void configureRecipeEditor() {
        colRecipeIngredient.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        colRecipeUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colRecipeQty.setCellValueFactory(new PropertyValueFactory<>("quantityRequired"));
        tvRecipeIngredients.setItems(recipeLines);

        reloadRecipeIngredients();

        cbRecipeIngredient.setConverter(new StringConverter<>() {
            @Override
            public String toString(Ingredient ingredient) {
                return ingredient == null ? "" : ingredient.getName();
            }

            @Override
            public Ingredient fromString(String string) {
                if (string == null) {
                    return null;
                }
                return cbRecipeIngredient.getItems().stream()
                        .filter(ingredient -> ingredient.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        cbRecipeIngredient.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + normalizeUnit(item.getUnit()) + ")");
            }
        });
    }

    private void reloadRecipeIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredientById.clear();

        String sql = "SELECT id, name, unit, quantityInStock, minStockLevel, unitCost FROM Ingredient ORDER BY name";
        try (Connection connection = Mydatabase.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("quantityInStock"),
                        resultSet.getString("unit"),
                        resultSet.getDouble("minStockLevel"),
                        resultSet.getDouble("unitCost"),
                        null,
                        null
                );
                ingredientById.put(ingredient.getId(), ingredient);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            lblDishMsg.setText("Unable to load ingredients: " + e.getMessage());
        }

        cbRecipeIngredient.setItems(FXCollections.observableArrayList(ingredients));
    }

    private void onMenuSelected(Menu menu) {
        clearMessages();
        if (menu == null) {
            updateSelectedMenuLabel(null);
            onClearMenu();
            clearDishForm();
            tvDishes.setItems(FXCollections.observableArrayList());
            return;
        }

        updateSelectedMenuLabel(menu);
        fillMenuForm(menu);
        refreshDishesForSelectedMenu();
    }

    private void refreshMenus() {
        try {
            Integer selectedMenuId = selectedMenu != null ? selectedMenu.getId() : null;
            ObservableList<Menu> data = FXCollections.observableArrayList(menuService.getAllMenu());
            tvMenus.setItems(data);

            if (selectedMenuId != null) {
                Menu matchingMenu = data.stream()
                        .filter(menu -> menu.getId() == selectedMenuId)
                        .findFirst()
                        .orElse(null);
                if (matchingMenu != null) {
                    tvMenus.getSelectionModel().select(matchingMenu);
                    selectedMenu = matchingMenu;
                    onMenuSelected(matchingMenu);
                } else {
                    selectedMenu = null;
                    tvMenus.getSelectionModel().clearSelection();
                    onMenuSelected(null);
                }
            }
        } catch (Exception e) {
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

        } catch (Exception e) {
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
            fillMenuForm(menu);
            refreshDishesForSelectedMenu();

        } catch (Exception e) {
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
            updateSelectedMenuLabel(null);
            tvDishes.setItems(FXCollections.observableArrayList());

            onClearMenu();
            onClearDish();
            refreshMenus();

        } catch (Exception e) {
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
            ObservableList<Dish> data = FXCollections.observableArrayList(dishService.getByMenuId(selectedMenu.getId()));
            tvDishes.setItems(data);
        } catch (Exception e) {
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

            List<DishIngredient> recipe = recipeFromTable();
            if (recipe.isEmpty()) {
                lblDishMsg.setText("Select ingredients and quantities first.");
                return;
            }

            Dish dish = new Dish();
            dish.setMenu_id(selectedMenu.getId());
            dish.setName(name);
            dish.setDescription(desc);
            dish.setBase_price(price);
            dish.setAvailable(true);
            dish.setStock_quantity(0);
            dish.setImage_url(img);
            dish.setCreated_at(Timestamp.from(Instant.now()));
            dish.setUpdate_at(Timestamp.from(Instant.now()));

            int dishId = dishService.addAndReturnId(dish);
            if (dishId <= 0) {
                lblDishMsg.setText("Dish add failed: unable to retrieve generated ID.");
                return;
            }

            dishIngredientService.replaceDishRecipe(dishId, recipe);

            onClearDish();
            refreshDishesForSelectedMenu();

        } catch (Exception e) {
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

            List<DishIngredient> recipe = recipeFromTable();
            if (recipe.isEmpty()) {
                lblDishMsg.setText("Select ingredients and quantities first.");
                return;
            }

            dish.setMenu_id(selectedMenu.getId());
            dish.setName(name);
            dish.setDescription(desc);
            dish.setBase_price(price);
            dish.setAvailable(true);
            dish.setStock_quantity(0);
            dish.setImage_url(img);
            dish.setUpdate_at(Timestamp.from(Instant.now()));

            dishService.update(dish);
            dishIngredientService.replaceDishRecipe(dish.getId(), recipe);

            refreshDishesForSelectedMenu();

        } catch (Exception e) {
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

        } catch (Exception e) {
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

    @FXML
    private void onAddRecipeIngredient() {
        clearMessages();
        Ingredient ingredient = cbRecipeIngredient.getValue();
        if (ingredient == null) {
            lblDishMsg.setText("Select an ingredient.");
            return;
        }

        Double qty = parsePositiveDouble(tfRecipeQuantity.getText(), "ingredient quantity");
        if (qty == null) {
            return;
        }

        RecipeLine existing = recipeLines.stream()
                .filter(line -> line.getIngredientId().equals(ingredient.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantityRequired(existing.getQuantityRequired() + qty);
            tvRecipeIngredients.refresh();
        } else {
            recipeLines.add(new RecipeLine(ingredient.getId(), ingredient.getName(), normalizeUnit(ingredient.getUnit()), qty));
        }

        cbRecipeIngredient.getSelectionModel().clearSelection();
        tfRecipeQuantity.clear();
    }

    @FXML
    private void onRemoveRecipeIngredient() {
        RecipeLine selected = tvRecipeIngredients.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recipeLines.remove(selected);
        }
    }

    private void clearDishForm() {
        tfDishName.clear();
        taDishDesc.clear();
        tfDishPrice.clear();
        tfDishImage.clear();
        recipeLines.clear();
        if (cbRecipeIngredient != null) {
            cbRecipeIngredient.getSelectionModel().clearSelection();
        }
        if (tfRecipeQuantity != null) {
            tfRecipeQuantity.clear();
        }
    }

    private void fillDishForm(Dish dish) {
        tfDishName.setText(dish.getName());
        taDishDesc.setText(dish.getDescription());
        tfDishPrice.setText(String.valueOf(dish.getBase_price()));
        tfDishImage.setText(dish.getImage_url());

        recipeLines.clear();
        try {
            List<DishIngredient> recipe = dishIngredientService.getByDishId(dish.getId());
            for (DishIngredient line : recipe) {
                Ingredient ingredient = ingredientById.get(line.getIngredientId());
                String ingredientName = ingredient != null ? ingredient.getName() : "Ingredient #" + line.getIngredientId();
                String unit = ingredient != null ? normalizeUnit(ingredient.getUnit()) : "UNIT";
                recipeLines.add(new RecipeLine(line.getIngredientId(), ingredientName, unit, line.getQuantityRequired()));
            }
        } catch (SQLException ignored) {
        }
    }

    private List<DishIngredient> recipeFromTable() {
        List<DishIngredient> recipe = new ArrayList<>();
        for (RecipeLine line : recipeLines) {
            recipe.add(new DishIngredient(null, line.getIngredientId(), line.getQuantityRequired()));
        }
        return recipe;
    }

    private void updateSelectedMenuLabel(Menu menu) {
        if (lblSelectedMenu == null) {
            return;
        }
        if (menu == null) {
            lblSelectedMenu.setText("Selected menu: (none)");
            return;
        }
        lblSelectedMenu.setText("Selected menu: " + menu.getId() + " - " + menu.getTitle());
    }

    private String normalizeUnit(String unit) {
        return unit == null || unit.isBlank() ? "UNIT" : unit.toUpperCase(Locale.ROOT);
    }

    private Double parsePositiveDouble(String value, String label) {
        try {
            double parsed = Double.parseDouble(value.trim());
            if (parsed <= 0) {
                showValidationAlert(label + " must be greater than 0.");
                return null;
            }
            return parsed;
        } catch (Exception ex) {
            showValidationAlert("Invalid " + label + ".");
            return null;
        }
    }

    private Float parseFloat(String value, String label) {
        try {
            return Float.parseFloat(value.trim());
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
        if (lblMenuMsg != null) {
            lblMenuMsg.setText("");
        }
        if (lblDishMsg != null) {
            lblDishMsg.setText("");
        }
    }

    public static class RecipeLine {
        private final Long ingredientId;
        private final String ingredientName;
        private final String unit;
        private Double quantityRequired;

        public RecipeLine(Long ingredientId, String ingredientName, String unit, Double quantityRequired) {
            this.ingredientId = ingredientId;
            this.ingredientName = ingredientName;
            this.unit = unit;
            this.quantityRequired = quantityRequired;
        }

        public Long getIngredientId() {
            return ingredientId;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public String getUnit() {
            return unit;
        }

        public Double getQuantityRequired() {
            return quantityRequired;
        }

        public void setQuantityRequired(Double quantityRequired) {
            this.quantityRequired = quantityRequired;
        }
    }
}
