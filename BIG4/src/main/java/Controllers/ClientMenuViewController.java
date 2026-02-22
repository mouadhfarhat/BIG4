package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class ClientMenuViewController {

    @FXML private ListView<String> lvMenus;
    @FXML private FlowPane fpDishes;
    @FXML private Label lblSelectedMenu;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbSort;

    @FXML
    public void initialize() {
        // demo data
        lvMenus.getItems().setAll("Breakfast Menu", "Lunch Menu", "Dinner Menu");

        lvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldV, menu) -> {
            if (menu != null) {
                lblSelectedMenu.setText(menu);
                loadDishesFor(menu);
            }
        });

        cbSort.getItems().setAll("Prix ↑", "Prix ↓", "Nom A→Z");
    }

    private void loadDishesFor(String menuName) {
        fpDishes.getChildren().clear();

        // Demo dishes - replace by DB results
        List<DishVm> dishes = List.of(
                new DishVm("Chicken Bowl", "Poulet, riz, sauce maison.", "12.500 DT"),
                new DishVm("Pasta Alfredo", "Crème, parmesan, poulet.", "18.000 DT"),
                new DishVm("Salade César", "Poulet, laitue, croûtons.", "10.000 DT")
        );

        for (DishVm d : dishes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DishCard.fxml"));
                Node card = loader.load();
                DishCardController c = loader.getController();
                c.setData(d.name, d.desc, d.price);
                fpDishes.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class DishVm {
        final String name, desc, price;
        DishVm(String n, String d, String p) { name=n; desc=d; price=p; }
    }
}
