package Controllers;

import Entities.Dish;
import Entities.Menu;
import Services.DishService;
import Services.MenuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClientMenuViewController {

    @FXML private ListView<Menu> lvMenus;
    @FXML private FlowPane fpDishes;
    @FXML private Label lblSelectedMenu;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbSort;

    private final MenuService menuService = new MenuService();
    private final DishService dishService = new DishService();
    private final ObservableList<Menu> menus = FXCollections.observableArrayList();
    private List<Dish> currentDishes = new ArrayList<>();

    @FXML
    public void initialize() {
        lvMenus.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        lvMenus.setItems(menus);

        cbSort.getItems().setAll("Prix ↑", "Prix ↓", "Nom A→Z");
        cbSort.setValue("Nom A→Z");

        lvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldV, menu) -> {
            if (menu != null) {
                lblSelectedMenu.setText(menu.getTitle());
                loadDishesFor(menu);
            }
        });

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> renderDishes());
        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> renderDishes());

        loadMenus();
    }

    private void loadMenus() {
        try {
            List<Menu> allMenus = menuService.getallMenu();
            menus.setAll(allMenus.stream().filter(Menu::isActive).collect(Collectors.toList()));

            if (!menus.isEmpty()) {
                lvMenus.getSelectionModel().selectFirst();
            } else {
                lblSelectedMenu.setText("Aucun menu actif");
                fpDishes.getChildren().clear();
            }
        } catch (SQLException e) {
            lblSelectedMenu.setText("Erreur chargement menus: " + e.getMessage());
            fpDishes.getChildren().clear();
        }
    }

    private void loadDishesFor(Menu menu) {
        try {
            currentDishes = dishService.getByMenuId(menu.getId());
            renderDishes();
        } catch (SQLException e) {
            currentDishes = new ArrayList<>();
            lblSelectedMenu.setText("Erreur chargement plats: " + e.getMessage());
            fpDishes.getChildren().clear();
        }
    }

    private void renderDishes() {
        fpDishes.getChildren().clear();
        String search = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase(Locale.ROOT);

        List<Dish> dishes = currentDishes.stream()
                .filter(dish -> dish.getAvailable() != null && dish.getAvailable())
                .filter(dish -> {
                    if (search.isEmpty()) {
                        return true;
                    }
                    String name = dish.getName() == null ? "" : dish.getName().toLowerCase(Locale.ROOT);
                    String desc = dish.getDescription() == null ? "" : dish.getDescription().toLowerCase(Locale.ROOT);
                    return name.contains(search) || desc.contains(search);
                })
                .collect(Collectors.toList());

        String sort = cbSort.getValue();
        if ("Prix ↑".equals(sort)) {
            dishes.sort(Comparator.comparingDouble(Dish::getBase_price));
        } else if ("Prix ↓".equals(sort)) {
            dishes.sort(Comparator.comparingDouble(Dish::getBase_price).reversed());
        } else {
            dishes.sort(Comparator.comparing(d -> d.getName() == null ? "" : d.getName().toLowerCase(Locale.ROOT)));
        }

        for (Dish dish : dishes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DishCard.fxml"));
                Node card = loader.load();
                DishCardController c = loader.getController();
                String priceText = String.format(Locale.US, "%.3f DT", dish.getBase_price());
                c.setData(
                        dish.getName() == null ? "(Sans nom)" : dish.getName(),
                        dish.getDescription() == null ? "" : dish.getDescription(),
                        priceText
                );
                fpDishes.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
