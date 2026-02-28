package Controllers;
import Services.AIChatService;
import Services.WeatherService;
import javafx.concurrent.Task;

import Entities.Dish;
import Entities.Menu;
import Services.DishService;
import Services.ExchangeRateService;
import Services.MenuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

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
    @FXML private ComboBox<String> cbCurrency;
    @FXML private ListView<String> lvChat;
    @FXML private TextField tfChat;



    private final AIChatService aiService = new AIChatService();
    private final WeatherService weatherService = new WeatherService();
    private WeatherService.Weather currentWeather;

    private final Services.ExchangeRateService exchangeService = new Services.ExchangeRateService();
    private double currentRate = 1.0;
    private String currentCurrency = "TND";

    private final MenuService menuService = new MenuService();
    private final DishService dishService = new DishService();

    private Menu selectedMenu;
    private List<Dish> currentDishes = List.of();

    @FXML
    public void initialize() {
        System.out.println("ClientMenuViewController initialized ✅");

        // Sort options
        cbSort.getItems().setAll("Prix ↑", "Prix ↓", "Nom A→Z");
        cbSort.getSelectionModel().select("Nom A→Z");
        cbCurrency.getItems().addAll("TND", "EUR", "USD");
        cbCurrency.getSelectionModel().select("TND");

        cbCurrency.setOnAction(e -> {
            String selected = cbCurrency.getValue();
            currentCurrency = selected;

            if ("TND".equals(selected)) {
                currentRate = 1.0;
                applyFilters();
                return;
            }

            currentRate = exchangeService.getRate("TND", selected);
            applyFilters();
        });

        // Show menu title in ListView
        lvMenus.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });

        // Selection listener
        lvMenus.getSelectionModel().selectedItemProperty().addListener((obs, oldV, menu) -> {
            System.out.println("Menu clicked = " + (menu == null ? "null" : menu.getId() + " / " + menu.getTitle()));
            if (menu != null) {
                selectedMenu = menu;
                lblSelectedMenu.setText(menu.getTitle());
                loadDishesForSelectedMenu();
            }
        });

        // Search
        tfSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Sort change
        cbSort.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Load menus (DB)
        Task<WeatherService.Weather> t = new Task<>() {
            @Override
            protected WeatherService.Weather call() {
                return weatherService.getCurrent(36.8065, 10.1815); // Tunis coords
            }
        };
        t.setOnSucceeded(e -> currentWeather = t.getValue());
        t.setOnFailed(e -> currentWeather = null);
        new Thread(t).start();
        loadMenus();
    }

    private void loadMenus() {
        List<Menu> menus = menuService.getActiveMenus();
        ObservableList<Menu> items = FXCollections.observableArrayList(menus);
        lvMenus.setItems(items);

        if (!items.isEmpty()) {
            lvMenus.getSelectionModel().select(0); // triggers listener -> loads dishes
        } else {
            lblSelectedMenu.setText("Aucun menu actif");
            fpDishes.getChildren().clear();
            fpDishes.getChildren().add(makeInfoLabel("Aucun menu actif."));
        }
    }

    private void loadDishesForSelectedMenu() {
        fpDishes.getChildren().clear();

        if (selectedMenu == null) {
            fpDishes.getChildren().add(makeInfoLabel("Sélectionnez un menu."));
            return;
        }

        // Fetch dishes from DB
        currentDishes = dishService.getDishesByMenu(selectedMenu.getId());

        System.out.println("Selected menu id = " + selectedMenu.getId());
        System.out.println("Dishes found = " + currentDishes.size());

        if (currentDishes.isEmpty()) {
            fpDishes.getChildren().add(makeInfoLabel("Aucun plat trouvé pour ce menu."));
            return;
        }

        applyFilters();
    }

    private void applyFilters() {
        if (selectedMenu == null) return;

        String q = tfSearch.getText() == null ? "" : tfSearch.getText().toLowerCase(Locale.ROOT).trim();

        List<Dish> filtered = currentDishes.stream()
                .filter(d -> q.isEmpty()
                        || (d.getName() != null && d.getName().toLowerCase(Locale.ROOT).contains(q))
                        || (d.getDescription() != null && d.getDescription().toLowerCase(Locale.ROOT).contains(q))
                )
                .collect(Collectors.toList());

        String sort = cbSort.getValue();
        if ("Prix ↑".equals(sort)) {
            filtered.sort(Comparator.comparing(Dish::getBase_price));
        } else if ("Prix ↓".equals(sort)) {
            filtered.sort(Comparator.comparing(Dish::getBase_price).reversed());
        } else {
            filtered.sort(Comparator.comparing(
                    Dish::getName,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            ));
        }

        renderDishCards(filtered);
    }

    private void renderDishCards(List<Dish> dishes) {
        fpDishes.getChildren().clear();

        if (dishes == null || dishes.isEmpty()) {
            fpDishes.getChildren().add(makeInfoLabel("Aucun plat à afficher."));
            return;
        }

        for (Dish d : dishes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DishCard.fxml"));
                Node card = loader.load();

                DishCardController c = loader.getController();

                // ✅ Currency conversion here
                double converted = d.getBase_price() * currentRate;

                String symbol = switch (currentCurrency) {
                    case "EUR" -> "€";
                    case "USD" -> "$";
                    default -> "DT";
                };

                String priceText = String.format(Locale.US, "%.2f %s", converted, symbol);

                String desc = d.getDescription() == null ? "" : d.getDescription();

                c.setData(
                        d.getName(),
                        desc,
                        priceText,
                        d.getImage_url() // DB image if exists
                );

                fpDishes.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
                fpDishes.getChildren().add(makeInfoLabel("Erreur chargement carte: " + d.getName()));
            }
        }
    }

    private Label makeInfoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14; -fx-padding: 20;");
        return l;
    }
    @FXML
    private void onSendChat() {
        String msg = tfChat.getText();
        if (msg == null || msg.trim().isEmpty()) return;

        lvChat.getItems().add("You: " + msg);
        tfChat.clear();

        StringBuilder dishesCtx = new StringBuilder();
        for (Dish d : currentDishes) {
            dishesCtx.append("- ")
                    .append(d.getName())
                    .append(" | ")
                    .append(d.getBase_price()).append(" TND")
                    .append(" | ")
                    .append(d.getDescription() == null ? "" : d.getDescription())
                    .append("\n");
        }

        String weatherInfo = (currentWeather == null)
                ? "Weather unknown"
                : ("Temperature: " + currentWeather.temperature + "°C");

        String systemPrompt = """
        You are a restaurant assistant.
        Suggest ONLY from the available dishes list.
        Consider the weather.

        %s

        Available dishes:
        %s
        """.formatted(weatherInfo, dishesCtx);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return aiService.askAI(systemPrompt, msg);
            }
        };

        task.setOnSucceeded(e -> lvChat.getItems().add("Bot: " + task.getValue()));
        task.setOnFailed(e -> lvChat.getItems().add("Bot: Sorry, I couldn't respond."));

        new Thread(task).start();
    }


}
