package Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ingredient {

    private Long id;
    private String name;
    private double quantityInStock;
    private String unit;
    private double minStockLevel;
    private double unitCost;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
    private final List<DishIngredient> dishUsages = new ArrayList<>();

    public Ingredient(Long id, String name, double quantityInStock,
                      String unit, double minStockLevel,
                      double unitCost, LocalDate expiryDate) {
        this(id, name, quantityInStock, unit, minStockLevel, unitCost, expiryDate, null);
    }

    public Ingredient(Long id, String name, double quantityInStock,
                      String unit, double minStockLevel,
                      double unitCost, LocalDate expiryDate,
                      LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.quantityInStock = quantityInStock;
        setUnit(unit);
        this.minStockLevel = minStockLevel;
        this.unitCost = unitCost;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    public double getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(double quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.toUpperCase();
    }

    public double getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(double minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DishIngredient> getDishUsages() {
        return Collections.unmodifiableList(dishUsages);
    }

    public void setDishUsages(List<DishIngredient> usages) {
        dishUsages.clear();
        if (usages == null) {
            return;
        }
        for (DishIngredient usage : usages) {
            addDishUsage(usage);
        }
    }

    public void addDishUsage(DishIngredient usage) {
        if (usage == null) {
            return;
        }
        usage.setIngredient(this);
        usage.setIngredientId(this.id);
        dishUsages.add(usage);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantityInStock=" + quantityInStock +
                ", unit='" + unit + '\'' +
                ", minStockLevel=" + minStockLevel +
                ", unitCost=" + unitCost +
                ", expiryDate=" + expiryDate +
                ", createdAt=" + createdAt +
                ", dishUsages=" + dishUsages.size() +
                '}';
    }
}