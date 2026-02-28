package Controllers;

import Utils.AiStockInsightService;
import Utils.Mydatabase;
import Services.IngredientWasteAutomationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label totalIngredientsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label expiredItemsLabel;
    @FXML private Label wasteRecordsLabel;
    @FXML private Label inventoryValueLabel;
    @FXML private Label totalWasteQtyLabel;
    @FXML private ComboBox<String> wastePeriodCombo;
    @FXML private Label nearExpiryLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label avgUnitCostLabel;
    @FXML private Label wasteCostPeriodLabel;
    @FXML private Label lowStockRateLabel;

    @FXML private Label weatherTempLabel;
    @FXML private Label weatherDemandLabel;
    @FXML private Label weatherExpiryLabel;
    @FXML private Label weatherStatusLabel;

    @FXML private ImageView aiChatLauncher;

    @FXML private PieChart wasteTypePieChart;
    @FXML private PieChart stockStatusPieChart;
    @FXML private BarChart<String, Number> topWastedBarChart;

    private final Mydatabase database = Mydatabase.getInstance();
    private final AiStockInsightService aiStockInsightService = new AiStockInsightService();
    private final IngredientWasteAutomationService ingredientWasteAutomationService = new IngredientWasteAutomationService();
    private static final String PERIOD_WEEK = "Week";
    private static final String PERIOD_MONTH = "Month";
    private static final String PERIOD_YEAR = "Year";
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast?latitude=36.8065&longitude=10.1815&current=temperature_2m&timezone=auto";
    private static final long WEATHER_CACHE_TTL_MS = 10 * 60 * 1000;

    private Stage aiChatStage;
    private VBox chatMessagesBox;
    private ScrollPane chatMessagesScrollPane;
    private Label chatStatusValueLabel;
    private TextField chatInputField;

    private List<IngredientInsight> latestInsights = List.of();
    private double latestPredictedWaste7d;
    private int latestPotentialStockouts;
    private double latestSuggestedBudget;
    private WeatherSnapshot latestWeather = WeatherSnapshot.defaultSnapshot();
    private long latestWeatherFetchedAt = 0L;
    private List<String> latestShortageAlerts = List.of();
    private List<String> latestExpiryAlerts = List.of();

    @FXML
    public void initialize() {
        ingredientWasteAutomationService.recordExpiredIngredientWaste();
        setupChatLauncher();
        setupWastePeriodSelector();
        loadStatistics();
        loadWasteTypeChart();
        loadStockStatusChart();
        loadTopWastedChart();
        loadSmartInsights();
    }

    private void setupWastePeriodSelector() {
        if (wastePeriodCombo == null) {
            return;
        }
        wastePeriodCombo.setItems(FXCollections.observableArrayList(PERIOD_WEEK, PERIOD_MONTH, PERIOD_YEAR));
        wastePeriodCombo.getSelectionModel().select(PERIOD_MONTH);
        wastePeriodCombo.valueProperty().addListener((obs, oldValue, newValue) -> loadStatistics());
    }

    private void setupChatLauncher() {
        if (aiChatLauncher == null) {
            return;
        }

        aiChatLauncher.setFitWidth(52);
        aiChatLauncher.setFitHeight(52);
        aiChatLauncher.setPreserveRatio(true);
        aiChatLauncher.setCursor(Cursor.HAND);
        aiChatLauncher.setClip(new Circle(26, 26, 26));
        aiChatLauncher.setEffect(new DropShadow(10, Color.rgb(37, 99, 235, 0.35)));
    }

    private void loadSmartInsights() {
        WeatherSnapshot weather = fetchWeatherSnapshot();
        double weatherMultiplier = weather.demandMultiplier;
        ObservableList<IngredientInsight> insights = FXCollections.observableArrayList();
        List<String> shortageAlerts = new ArrayList<>();
        List<String> expiryAlerts = new ArrayList<>();

        String sql = "SELECT i.id, i.name, i.quantityInStock, i.minStockLevel, i.unitCost, i.expiryDate, " +
                "COALESCE(SUM(CASE WHEN w.date >= NOW() - INTERVAL 30 DAY THEN w.quantityWasted ELSE 0 END), 0) AS waste30 " +
                "FROM Ingredient i " +
                "LEFT JOIN WasteRecord w ON w.ingredientId = i.id " +
                "GROUP BY i.id, i.name, i.quantityInStock, i.minStockLevel, i.unitCost, i.expiryDate " +
                "ORDER BY i.name";

        double predictedWaste7d = 0;
        int potentialStockouts = 0;
        double suggestedBudget = 0;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String ingredientName = resultSet.getString("name");
                double currentStock = resultSet.getDouble("quantityInStock");
                double minStock = resultSet.getDouble("minStockLevel");
                double unitCost = resultSet.getDouble("unitCost");
                double waste30 = resultSet.getDouble("waste30");
                Date expiryDateSql = resultSet.getDate("expiryDate");

                double avgDailyWaste = waste30 / 30.0;
                double predictedNeed7d = avgDailyWaste * 7.0 * weatherMultiplier;
                double safetyBuffer = minStock * 0.5;
                double recommendedTarget = Math.max(minStock, predictedNeed7d + safetyBuffer);
                double recommendedOrder = Math.max(0, recommendedTarget - currentStock);

                predictedWaste7d += predictedNeed7d;
                if (currentStock < predictedNeed7d + minStock) {
                    potentialStockouts++;
                }
                suggestedBudget += recommendedOrder * unitCost;

                LocalDate expiryDate = expiryDateSql == null ? null : expiryDateSql.toLocalDate();
                LocalDate adjustedExpiryDate = computeWeatherAdjustedExpiry(expiryDate, weather.expiryAcceleration);
                String riskLevel = computeRiskLevel(currentStock, minStock, predictedNeed7d, expiryDateSql, weather.expiryAcceleration);

                if (recommendedOrder > 0) {
                    shortageAlerts.add(String.format("%s -> order %.2f units (current %.2f, min %.2f)",
                            ingredientName, round2(recommendedOrder), round2(currentStock), round2(minStock)));
                }

                if (adjustedExpiryDate != null) {
                    long daysAdjusted = ChronoUnit.DAYS.between(LocalDate.now(), adjustedExpiryDate);
                    if (daysAdjusted <= 5) {
                        expiryAlerts.add(String.format("%s -> ~%s (%d days)",
                                ingredientName, adjustedExpiryDate, Math.max(0, daysAdjusted)));
                    }
                }

                insights.add(new IngredientInsight(
                        ingredientName,
                        round2(currentStock),
                        round2(minStock),
                        round2(avgDailyWaste),
                        round2(recommendedOrder),
                        riskLevel,
                        expiryDate,
                        adjustedExpiryDate
                ));
            }
        } catch (SQLException e) {
            latestInsights = List.of();
            latestPredictedWaste7d = 0;
            latestPotentialStockouts = 0;
            latestSuggestedBudget = 0;
            latestShortageAlerts = List.of();
            latestExpiryAlerts = List.of();
            updateWeatherDisplay(weather, false);
            return;
        }

        insights.sort((a, b) -> Double.compare(b.getRecommendedOrder(), a.getRecommendedOrder()));
        latestWeather = weather;
        latestInsights = List.copyOf(insights);
        latestPredictedWaste7d = round2(predictedWaste7d);
        latestPotentialStockouts = potentialStockouts;
        latestSuggestedBudget = round2(suggestedBudget);
        latestShortageAlerts = List.copyOf(shortageAlerts.subList(0, Math.min(5, shortageAlerts.size())));
        latestExpiryAlerts = List.copyOf(expiryAlerts.subList(0, Math.min(5, expiryAlerts.size())));

        updateWeatherDisplay(weather, true);
    }

    @FXML
    private void handleOpenAiChat() {
        if (aiChatStage == null) {
            createAiChatStage();
        }

        if (!aiChatStage.isShowing()) {
            aiChatStage.show();
        }
        aiChatStage.toFront();
    }

    private void createAiChatStage() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #f5f7fb;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #1d4ed8; -fx-background-radius: 10;");
        Label title = new Label("AI Inventory Chat");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        chatStatusValueLabel = new Label("Ready");
        chatStatusValueLabel.setStyle("-fx-text-fill: #dbeafe; -fx-font-size: 11;");
        header.getChildren().addAll(title, spacer, chatStatusValueLabel);

        chatMessagesBox = new VBox(8);
        chatMessagesBox.setPadding(new Insets(6));
        addAssistantMessage("Hi! Ask me about shortages, expiry dates, weather impact, or reorder plan.");

        chatMessagesScrollPane = new ScrollPane(chatMessagesBox);
        chatMessagesScrollPane.setFitToWidth(true);
        chatMessagesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatMessagesScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(chatMessagesScrollPane, Priority.ALWAYS);

        HBox composer = new HBox(8);
        composer.setAlignment(Pos.CENTER_LEFT);
        chatInputField = new TextField();
        chatInputField.setPromptText("Type your message...");
        HBox.setHgrow(chatInputField, Priority.ALWAYS);
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        sendButton.setOnAction(event -> sendChatMessage());
        chatInputField.setOnAction(event -> sendChatMessage());

        composer.getChildren().addAll(chatInputField, sendButton);
        root.getChildren().addAll(header, chatMessagesScrollPane, composer);

        aiChatStage = new Stage();
        aiChatStage.setTitle("AI Chat");
        aiChatStage.setScene(new Scene(root, 430, 560));
    }

    private void sendChatMessage() {
        String message = chatInputField.getText();
        if (message == null || message.isBlank()) {
            return;
        }

        String trimmedMessage = message.trim();
        chatInputField.clear();
        addUserMessage(trimmedMessage);
        chatStatusValueLabel.setText("Sending...");

        String contextPrompt = buildChatContextPrompt(trimmedMessage);

        Thread thread = new Thread(() -> {
            AiStockInsightService.AiResult result = aiStockInsightService.askInventoryAssistant(contextPrompt);
            javafx.application.Platform.runLater(() -> {
                String summary = result.getSummary();
                if (summary == null || summary.isBlank()) {
                    summary = "No response returned from API. Please try again.";
                }
                if (!result.getProvider().startsWith("OpenRouter")) {
                    summary = buildLocalChatFallback(trimmedMessage, summary);
                }
                addAssistantMessage(summary);
                chatStatusValueLabel.setText(result.getProvider());
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String buildChatContextPrompt(String userMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append("User question: ").append(userMessage).append("\n\n");
        builder.append("Dashboard snapshot:\n");
        builder.append("- Potential stockouts (7d): ").append(latestPotentialStockouts).append("\n");
        builder.append("- Predicted waste (7d): ").append(String.format("%.2f", latestPredictedWaste7d)).append("\n");
        builder.append("- Suggested budget: $").append(String.format("%.2f", latestSuggestedBudget)).append("\n");
        builder.append(String.format("- Weather now: %.1f°C\n", latestWeather.temperatureC));
        builder.append(String.format("- Demand multiplier: %.2f\n", latestWeather.demandMultiplier));
        builder.append(String.format("- Expiry acceleration factor: %.2f\n", latestWeather.expiryAcceleration));

        builder.append("Priority shortage checks:\n");
        if (latestShortageAlerts.isEmpty()) {
            builder.append("- none\n");
        } else {
            for (String shortage : latestShortageAlerts) {
                builder.append("- ").append(shortage).append("\n");
            }
        }

        builder.append("Priority expiry checks:\n");
        if (latestExpiryAlerts.isEmpty()) {
            builder.append("- none\n");
        } else {
            for (String expiry : latestExpiryAlerts) {
                builder.append("- ").append(expiry).append("\n");
            }
        }

        builder.append("Top insights:\n");
        int max = Math.min(5, latestInsights.size());
        for (int index = 0; index < max; index++) {
            IngredientInsight insight = latestInsights.get(index);
            builder.append("* ")
                    .append(insight.getIngredientName())
                    .append(" | current=").append(insight.getCurrentStock())
                    .append(" | min=").append(insight.getMinStock())
                    .append(" | rec=").append(insight.getRecommendedOrder())
                    .append(" | risk=").append(insight.getRiskLevel())
                    .append(" | expiry=").append(insight.getExpiryDate() == null ? "N/A" : insight.getExpiryDate())
                    .append(" | weatherAdjustedExpiry=").append(insight.getWeatherAdjustedExpiryDate() == null ? "N/A" : insight.getWeatherAdjustedExpiryDate())
                    .append("\n");
        }
        return builder.toString();
    }

    private String buildLocalChatFallback(String userMessage, String apiError) {
        String question = userMessage == null ? "" : userMessage.toLowerCase();
        StringBuilder fallback = new StringBuilder();
        fallback.append("API is temporarily unavailable, using local helper mode.\n");
        if (apiError != null && !apiError.isBlank()) {
            fallback.append("Reason: ").append(apiError).append("\n\n");
        }

        if (question.contains("expiry") || question.contains("expire") || question.contains("date")) {
            fallback.append("Top weather-adjusted expiry checks:\n");
            if (latestExpiryAlerts.isEmpty()) {
                fallback.append("- No urgent expiry risk in next 5 days.\n");
            } else {
                for (String alert : latestExpiryAlerts) {
                    fallback.append("- ").append(alert).append("\n");
                }
            }
            return fallback.toString();
        }

        if (question.contains("shortage") || question.contains("stock") || question.contains("order")) {
            fallback.append("Top shortage checks:\n");
            if (latestShortageAlerts.isEmpty()) {
                fallback.append("- No immediate shortage detected.\n");
            } else {
                for (String alert : latestShortageAlerts) {
                    fallback.append("- ").append(alert).append("\n");
                }
            }
            fallback.append("\nSuggested budget: $").append(String.format("%.2f", latestSuggestedBudget));
            return fallback.toString();
        }

        fallback.append("Quick summary:\n")
                .append("- Predicted waste (7d): ").append(String.format("%.2f", latestPredictedWaste7d)).append("\n")
                .append("- Potential stockouts (7d): ").append(latestPotentialStockouts).append("\n")
                .append("- Suggested budget: $").append(String.format("%.2f", latestSuggestedBudget)).append("\n");

        if (!latestShortageAlerts.isEmpty()) {
            fallback.append("\nTop shortages:\n");
            for (String alert : latestShortageAlerts) {
                fallback.append("- ").append(alert).append("\n");
            }
        }
        if (!latestExpiryAlerts.isEmpty()) {
            fallback.append("\nTop expiry risks:\n");
            for (String alert : latestExpiryAlerts) {
                fallback.append("- ").append(alert).append("\n");
            }
        }
        return fallback.toString();
    }

    private void addUserMessage(String message) {
        chatMessagesBox.getChildren().add(createBubble(message, false));
        scrollChatToBottom();
    }

    private void addAssistantMessage(String message) {
        chatMessagesBox.getChildren().add(createBubble(message, true));
        scrollChatToBottom();
    }

    private void scrollChatToBottom() {
        if (chatMessagesScrollPane == null) {
            return;
        }
        javafx.application.Platform.runLater(() -> chatMessagesScrollPane.setVvalue(1.0));
    }

    private HBox createBubble(String text, boolean assistant) {
        HBox row = new HBox();
        row.setPadding(new Insets(2, 4, 2, 4));
        row.setAlignment(assistant ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(300);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        if (assistant) {
            bubble.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-background-radius: 14; -fx-border-radius: 14; -fx-text-fill: #0f172a;");
        } else {
            bubble.setStyle("-fx-background-color: #2563eb; -fx-background-radius: 14; -fx-text-fill: white;");
        }

        row.getChildren().add(bubble);
        return row;
    }

    private String computeRiskLevel(double currentStock, double minStock, double predictedNeed7d, Date expiryDateSql, double expiryAcceleration) {
        boolean stockRisk = currentStock < minStock || currentStock < predictedNeed7d;

        long daysToExpiry = Long.MAX_VALUE;
        if (expiryDateSql != null) {
            LocalDate expiryDate = expiryDateSql.toLocalDate();
            daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            daysToExpiry = adjustDaysForWeather(daysToExpiry, expiryAcceleration);
        }

        if (daysToExpiry <= 3 && currentStock > minStock) {
            return "HIGH";
        }
        if (stockRisk || daysToExpiry <= 7) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private LocalDate computeWeatherAdjustedExpiry(LocalDate expiryDate, double expiryAcceleration) {
        if (expiryDate == null) {
            return null;
        }
        long rawDays = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        long adjustedDays = adjustDaysForWeather(rawDays, expiryAcceleration);
        return LocalDate.now().plusDays(Math.max(0, adjustedDays));
    }

    private long adjustDaysForWeather(long daysToExpiry, double expiryAcceleration) {
        if (daysToExpiry == Long.MAX_VALUE) {
            return daysToExpiry;
        }
        if (daysToExpiry <= 0) {
            return 0;
        }
        if (expiryAcceleration <= 0) {
            return daysToExpiry;
        }
        return Math.max(0, Math.round(daysToExpiry / expiryAcceleration));
    }

    private void updateWeatherDisplay(WeatherSnapshot weather, boolean stockLoaded) {
        if (weatherTempLabel != null) {
            weatherTempLabel.setText(String.format("%.1f°C", weather.temperatureC));
        }
        if (weatherDemandLabel != null) {
            weatherDemandLabel.setText(String.format("Demand x%.2f", weather.demandMultiplier));
        }
        if (weatherExpiryLabel != null) {
            weatherExpiryLabel.setText(String.format("Expiry x%.2f", weather.expiryAcceleration));
        }
        if (weatherStatusLabel != null) {
            weatherStatusLabel.setText(stockLoaded ? "Weather API: synced" : "Weather API: stock data unavailable");
        }
    }

    private WeatherSnapshot fetchWeatherSnapshot() {
        long now = System.currentTimeMillis();
        if (latestWeather != null && (now - latestWeatherFetchedAt) < WEATHER_CACHE_TTL_MS) {
            return latestWeather;
        }

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(OPEN_METEO_URL))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                if (weatherStatusLabel != null) {
                    weatherStatusLabel.setText("Weather API: unavailable (cached/default)");
                }
                return WeatherSnapshot.defaultSnapshot();
            }

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\\"temperature_2m\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
            java.util.regex.Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                double temperature = Double.parseDouble(matcher.group(1));
                double demandMultiplier = temperature >= 30 ? 1.15 : (temperature <= 10 ? 0.95 : 1.0);
                double expiryAcceleration = temperature >= 30 ? 1.25 : (temperature <= 10 ? 0.90 : 1.0);
                if (weatherStatusLabel != null) {
                    weatherStatusLabel.setText("Weather API: live");
                }
                WeatherSnapshot snapshot = new WeatherSnapshot(temperature, demandMultiplier, expiryAcceleration);
                latestWeatherFetchedAt = now;
                latestWeather = snapshot;
                return snapshot;
            }
        } catch (java.io.IOException | InterruptedException | NumberFormatException e) {
            if (weatherStatusLabel != null) {
                weatherStatusLabel.setText("Weather API: error (cached/default)");
            }
        }
        return WeatherSnapshot.defaultSnapshot();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void loadStatistics() {
        int totalIngredients = fetchInt("SELECT COUNT(*) FROM Ingredient");
        int lowStockItems = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock <= minStockLevel");
        int expiredItems = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE expiryDate IS NOT NULL AND expiryDate < CURDATE()");
        int wasteRecords = fetchInt("SELECT COUNT(*) FROM WasteRecord");

        totalIngredientsLabel.setText(String.valueOf(totalIngredients));
        lowStockLabel.setText(String.valueOf(lowStockItems));
        expiredItemsLabel.setText(String.valueOf(expiredItems));
        wasteRecordsLabel.setText(String.valueOf(wasteRecords));

        double inventoryValue = fetchDouble("SELECT COALESCE(SUM(quantityInStock * unitCost), 0) FROM Ingredient");
        inventoryValueLabel.setText(String.format("$%.2f", inventoryValue));

        String wasteDateCondition = buildWastePeriodCondition("date");
        String wasteDateConditionWithAlias = buildWastePeriodCondition("w.date");

        double wasteQty = fetchDouble("SELECT COALESCE(SUM(quantityWasted), 0) FROM WasteRecord WHERE " + wasteDateCondition);
        totalWasteQtyLabel.setText(String.format("%.2f", wasteQty));

        int nearExpiry = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE expiryDate IS NOT NULL AND expiryDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)");
        int outOfStock = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock <= 0");
        double avgUnitCost = fetchDouble("SELECT COALESCE(AVG(unitCost), 0) FROM Ingredient");
        double wasteCost = fetchDouble("SELECT COALESCE(SUM(w.quantityWasted * COALESCE(i.unitCost, 0)), 0) " +
                "FROM WasteRecord w LEFT JOIN Ingredient i ON i.id = w.ingredientId WHERE " + wasteDateConditionWithAlias);
        double lowStockRate = totalIngredients == 0 ? 0 : (lowStockItems * 100.0 / totalIngredients);

        if (nearExpiryLabel != null) {
            nearExpiryLabel.setText(String.valueOf(nearExpiry));
        }
        if (outOfStockLabel != null) {
            outOfStockLabel.setText(String.valueOf(outOfStock));
        }
        if (avgUnitCostLabel != null) {
            avgUnitCostLabel.setText(String.format("$%.2f", avgUnitCost));
        }
        if (wasteCostPeriodLabel != null) {
            wasteCostPeriodLabel.setText(String.format("$%.2f", wasteCost));
        }
        if (lowStockRateLabel != null) {
            lowStockRateLabel.setText(String.format("%.1f%%", lowStockRate));
        }
    }

    private String buildWastePeriodCondition(String dateColumn) {
        String selectedPeriod = wastePeriodCombo != null && wastePeriodCombo.getValue() != null
                ? wastePeriodCombo.getValue()
                : PERIOD_MONTH;

        if (PERIOD_WEEK.equals(selectedPeriod)) {
            return "YEAR(" + dateColumn + ") = YEAR(CURDATE()) AND WEEK(" + dateColumn + ", 1) = WEEK(CURDATE(), 1)";
        }
        if (PERIOD_YEAR.equals(selectedPeriod)) {
            return "YEAR(" + dateColumn + ") = YEAR(CURDATE())";
        }
        return "YEAR(" + dateColumn + ") = YEAR(CURDATE()) AND MONTH(" + dateColumn + ") = MONTH(CURDATE())";
    }

    private void loadWasteTypeChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String sql = "SELECT COALESCE(wasteType, 'Unknown') AS type, COUNT(*) AS total FROM WasteRecord GROUP BY wasteType ORDER BY total DESC";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int total = resultSet.getInt("total");
                data.add(new PieChart.Data(type, total));
            }
        } catch (SQLException e) {
            data.clear();
        }

        wasteTypePieChart.setData(data);
    }

    private void loadStockStatusChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        int healthy = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock > minStockLevel AND (expiryDate IS NULL OR expiryDate >= CURDATE())");
        int low = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE quantityInStock <= minStockLevel AND (expiryDate IS NULL OR expiryDate >= CURDATE())");
        int expired = fetchInt("SELECT COUNT(*) FROM Ingredient WHERE expiryDate IS NOT NULL AND expiryDate < CURDATE()");

        data.add(new PieChart.Data("Healthy", healthy));
        data.add(new PieChart.Data("Low Stock", low));
        data.add(new PieChart.Data("Expired", expired));

        stockStatusPieChart.setData(data);
    }

    private void loadTopWastedChart() {
        topWastedBarChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Wasted Quantity");

        String sql = "SELECT COALESCE(i.name, CONCAT('Ingredient #', w.ingredientId)) AS ingredientName, " +
                "COALESCE(SUM(w.quantityWasted), 0) AS totalWasted " +
                "FROM WasteRecord w " +
                "LEFT JOIN Ingredient i ON i.id = w.ingredientId " +
                "GROUP BY ingredientName " +
                "ORDER BY totalWasted DESC " +
                "LIMIT 8";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(
                        resultSet.getString("ingredientName"),
                        resultSet.getDouble("totalWasted")
                ));
            }
        } catch (SQLException e) {
            series.getData().clear();
        }

        topWastedBarChart.getData().add(series);
    }

    private int fetchInt(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    private double fetchDouble(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public static class IngredientInsight {
        private final String ingredientName;
        private final double currentStock;
        private final double minStock;
        private final double avgDailyWaste;
        private final double recommendedOrder;
        private final String riskLevel;
        private final LocalDate expiryDate;
        private final LocalDate weatherAdjustedExpiryDate;

        public IngredientInsight(String ingredientName,
                                 double currentStock,
                                 double minStock,
                                 double avgDailyWaste,
                                 double recommendedOrder,
                                 String riskLevel,
                                 LocalDate expiryDate,
                                 LocalDate weatherAdjustedExpiryDate) {
            this.ingredientName = ingredientName;
            this.currentStock = currentStock;
            this.minStock = minStock;
            this.avgDailyWaste = avgDailyWaste;
            this.recommendedOrder = recommendedOrder;
            this.riskLevel = riskLevel;
            this.expiryDate = expiryDate;
            this.weatherAdjustedExpiryDate = weatherAdjustedExpiryDate;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public double getCurrentStock() {
            return currentStock;
        }

        public double getMinStock() {
            return minStock;
        }

        public double getAvgDailyWaste() {
            return avgDailyWaste;
        }

        public double getRecommendedOrder() {
            return recommendedOrder;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public LocalDate getWeatherAdjustedExpiryDate() {
            return weatherAdjustedExpiryDate;
        }
    }

    private static class WeatherSnapshot {
        private final double temperatureC;
        private final double demandMultiplier;
        private final double expiryAcceleration;

        private WeatherSnapshot(double temperatureC, double demandMultiplier, double expiryAcceleration) {
            this.temperatureC = temperatureC;
            this.demandMultiplier = demandMultiplier;
            this.expiryAcceleration = expiryAcceleration;
        }

        private static WeatherSnapshot defaultSnapshot() {
            return new WeatherSnapshot(22.0, 1.0, 1.0);
        }
    }
}
