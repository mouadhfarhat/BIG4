package Services;

import Entities.Dish;
import Entities.DishIngredient;
import Entities.DonationDishRecommendation;
import Entities.Ingredient;
import Utils.Mydatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DonationOptimizationService {

    private final Connection cnx;
    private final DishService dishService;
    private final DishIngredientService dishIngredientService;

    public DonationOptimizationService() {
        try {
            this.cnx = Mydatabase.getInstance().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to obtain database connection", e);
        }
        this.dishService = new DishService();
        this.dishIngredientService = new DishIngredientService();
    }

    public List<DonationDishRecommendation> rankDonationDishes(int nearExpiryDays) throws SQLException {
        return rankDonationDishes(nearExpiryDays, 0.20d);
    }

    /**
     * Ranks dishes for donation optimization.
     *
     * Ranking priority:
     * 1) Maximum usage of near-expiry ingredients
     * 2) Higher cost-saving potential (weighted)
     */
    public List<DonationDishRecommendation> rankDonationDishes(int nearExpiryDays, double costSavingWeight) throws SQLException {
        if (nearExpiryDays < 0) {
            throw new IllegalArgumentException("nearExpiryDays must be >= 0");
        }

        dishIngredientService.ensureDishIngredientTableExists();

        Map<Long, Ingredient> nearExpiryIngredients = fetchNearExpiryIngredients(nearExpiryDays);
        if (nearExpiryIngredients.isEmpty()) {
            return List.of();
        }

        Set<Long> nearExpiryIds = nearExpiryIngredients.keySet();
        List<DishIngredient> nearExpiryRecipeLines = fetchDishIngredientsForIngredients(nearExpiryIds);
        if (nearExpiryRecipeLines.isEmpty()) {
            return List.of();
        }

        Map<Integer, Dish> dishesById = dishService.getAll().stream()
                .filter(dish -> Boolean.TRUE.equals(dish.getAvailable()))
                .collect(Collectors.toMap(Dish::getId, dish -> dish, (left, right) -> left, LinkedHashMap::new));

        Map<Integer, List<DishIngredient>> dishRecipeByDishId = nearExpiryRecipeLines.stream()
                .collect(Collectors.groupingBy(DishIngredient::getDishId));

        List<DonationDishRecommendation> recommendations = new ArrayList<>();

        for (Map.Entry<Integer, List<DishIngredient>> entry : dishRecipeByDishId.entrySet()) {
            Integer dishId = entry.getKey();
            Dish dish = dishesById.get(dishId);
            if (dish == null) {
                continue;
            }

            List<DishIngredient> lines = entry.getValue();
            if (lines.isEmpty()) {
                continue;
            }

            int maxDishCount = lines.stream()
                    .mapToInt(line -> {
                        Ingredient ingredient = nearExpiryIngredients.get(line.getIngredientId());
                        if (ingredient == null || line.getQuantityRequired() <= 0) {
                            return 0;
                        }
                        return (int) Math.floor(ingredient.getQuantityInStock() / line.getQuantityRequired());
                    })
                    .min()
                    .orElse(0);

            if (maxDishCount <= 0) {
                continue;
            }

            DonationDishRecommendation recommendation = new DonationDishRecommendation();
            recommendation.setDish(dish);
            recommendation.setMaxDishCountFromNearExpiry(maxDishCount);

            double nearExpiryUsageScore = 0.0;
            double costSavingScore = 0.0;

            for (DishIngredient line : lines) {
                Ingredient ingredient = nearExpiryIngredients.get(line.getIngredientId());
                if (ingredient == null) {
                    continue;
                }

                double consumed = maxDishCount * line.getQuantityRequired();
                nearExpiryUsageScore += consumed;
                costSavingScore += consumed * ingredient.getUnitCost();

                DonationDishRecommendation.NearExpiryIngredientUsage usage = new DonationDishRecommendation.NearExpiryIngredientUsage();
                usage.setIngredient(ingredient);
                usage.setQuantityRequiredPerDish(line.getQuantityRequired());
                usage.setNearExpiryStockQuantity(ingredient.getQuantityInStock());
                usage.setNearExpiryQuantityConsumed(consumed);
                recommendation.addIngredientUsage(usage);
            }

            recommendation.setNearExpiryUsageScore(nearExpiryUsageScore);
            recommendation.setCostSavingScore(costSavingScore);
            recommendation.setTotalPriorityScore(nearExpiryUsageScore + (costSavingScore * costSavingWeight));
            recommendations.add(recommendation);
        }

        recommendations.sort(
                Comparator.comparingDouble(DonationDishRecommendation::getTotalPriorityScore).reversed()
                        .thenComparingDouble(DonationDishRecommendation::getNearExpiryUsageScore).reversed()
                        .thenComparingDouble(DonationDishRecommendation::getCostSavingScore).reversed()
        );

        return recommendations;
    }

    public List<DonationDishRecommendation> topRecommendationsForEvent(int nearExpiryDays, int topN) throws SQLException {
        List<DonationDishRecommendation> ranked = rankDonationDishes(nearExpiryDays);
        if (topN <= 0 || ranked.isEmpty()) {
            return List.of();
        }
        int toIndex = Math.min(topN, ranked.size());
        return ranked.subList(0, toIndex);
    }

    private Map<Long, Ingredient> fetchNearExpiryIngredients(int nearExpiryDays) throws SQLException {
        String sql = """
                SELECT id, name, quantityInStock, unit, minStockLevel, unitCost, expiryDate, createdAt
                FROM Ingredient
                WHERE expiryDate IS NOT NULL
                  AND quantityInStock > 0
                  AND expiryDate >= ?
                  AND expiryDate <= ?
                """;

        LocalDate today = LocalDate.now();
        LocalDate upperBound = today.plusDays(nearExpiryDays);

        Map<Long, Ingredient> ingredients = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(today));
            ps.setDate(2, Date.valueOf(upperBound));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getDouble("quantityInStock"),
                            rs.getString("unit"),
                            rs.getDouble("minStockLevel"),
                            rs.getDouble("unitCost"),
                            rs.getDate("expiryDate") != null ? rs.getDate("expiryDate").toLocalDate() : null,
                            rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null
                    );
                    ingredients.put(ingredient.getId(), ingredient);
                }
            }
        }
        return ingredients;
    }

    private List<DishIngredient> fetchDishIngredientsForIngredients(Set<Long> ingredientIds) throws SQLException {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return List.of();
        }

        String placeholders = ingredientIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT dish_id, ingredient_id, quantity_required FROM dish_ingredient WHERE ingredient_id IN (" + placeholders + ")";

        List<DishIngredient> lines = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            int index = 1;
            for (Long ingredientId : ingredientIds) {
                ps.setLong(index++, ingredientId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DishIngredient line = new DishIngredient(
                            rs.getInt("dish_id"),
                            rs.getLong("ingredient_id"),
                            rs.getDouble("quantity_required")
                    );
                    if (line.getDishId() != null && line.getIngredientId() != null
                            && line.getQuantityRequired() > 0
                            && Objects.nonNull(line.getDishId())) {
                        lines.add(line);
                    }
                }
            }
        }

        return lines;
    }
}
