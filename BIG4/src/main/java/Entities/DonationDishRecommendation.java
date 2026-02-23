package Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonationDishRecommendation {

    private Dish dish;
    private double nearExpiryUsageScore;
    private double costSavingScore;
    private double totalPriorityScore;
    private int maxDishCountFromNearExpiry;
    private final List<NearExpiryIngredientUsage> ingredientUsages = new ArrayList<>();

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public double getNearExpiryUsageScore() {
        return nearExpiryUsageScore;
    }

    public void setNearExpiryUsageScore(double nearExpiryUsageScore) {
        this.nearExpiryUsageScore = nearExpiryUsageScore;
    }

    public double getCostSavingScore() {
        return costSavingScore;
    }

    public void setCostSavingScore(double costSavingScore) {
        this.costSavingScore = costSavingScore;
    }

    public double getTotalPriorityScore() {
        return totalPriorityScore;
    }

    public void setTotalPriorityScore(double totalPriorityScore) {
        this.totalPriorityScore = totalPriorityScore;
    }

    public int getMaxDishCountFromNearExpiry() {
        return maxDishCountFromNearExpiry;
    }

    public void setMaxDishCountFromNearExpiry(int maxDishCountFromNearExpiry) {
        this.maxDishCountFromNearExpiry = maxDishCountFromNearExpiry;
    }

    public List<NearExpiryIngredientUsage> getIngredientUsages() {
        return Collections.unmodifiableList(ingredientUsages);
    }

    public void setIngredientUsages(List<NearExpiryIngredientUsage> usages) {
        ingredientUsages.clear();
        if (usages != null) {
            ingredientUsages.addAll(usages);
        }
    }

    public void addIngredientUsage(NearExpiryIngredientUsage usage) {
        if (usage != null) {
            ingredientUsages.add(usage);
        }
    }

    @Override
    public String toString() {
        String dishName = dish != null ? dish.getName() : "Unknown";
        return "DonationDishRecommendation{" +
                "dish=" + dishName +
                ", nearExpiryUsageScore=" + nearExpiryUsageScore +
                ", costSavingScore=" + costSavingScore +
                ", totalPriorityScore=" + totalPriorityScore +
                ", maxDishCountFromNearExpiry=" + maxDishCountFromNearExpiry +
                ", ingredientUsages=" + ingredientUsages.size() +
                '}';
    }

    public static class NearExpiryIngredientUsage {
        private Ingredient ingredient;
        private double quantityRequiredPerDish;
        private double nearExpiryStockQuantity;
        private double nearExpiryQuantityConsumed;

        public Ingredient getIngredient() {
            return ingredient;
        }

        public void setIngredient(Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        public double getQuantityRequiredPerDish() {
            return quantityRequiredPerDish;
        }

        public void setQuantityRequiredPerDish(double quantityRequiredPerDish) {
            this.quantityRequiredPerDish = quantityRequiredPerDish;
        }

        public double getNearExpiryStockQuantity() {
            return nearExpiryStockQuantity;
        }

        public void setNearExpiryStockQuantity(double nearExpiryStockQuantity) {
            this.nearExpiryStockQuantity = nearExpiryStockQuantity;
        }

        public double getNearExpiryQuantityConsumed() {
            return nearExpiryQuantityConsumed;
        }

        public void setNearExpiryQuantityConsumed(double nearExpiryQuantityConsumed) {
            this.nearExpiryQuantityConsumed = nearExpiryQuantityConsumed;
        }
    }
}
