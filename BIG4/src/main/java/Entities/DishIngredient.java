package Entities;

public class DishIngredient {

    private Integer dishId;
    private Long ingredientId;
    private double quantityRequired;

    private Dish dish;
    private Ingredient ingredient;

    public DishIngredient() {
    }

    public DishIngredient(Integer dishId, Long ingredientId, double quantityRequired) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantityRequired = quantityRequired;
    }

    public DishIngredient(Dish dish, Ingredient ingredient, double quantityRequired) {
        this.dish = dish;
        this.ingredient = ingredient;
        this.dishId = dish != null ? dish.getId() : null;
        this.ingredientId = ingredient != null ? ingredient.getId() : null;
        this.quantityRequired = quantityRequired;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        this.dishId = dish != null ? dish.getId() : this.dishId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        this.ingredientId = ingredient != null ? ingredient.getId() : this.ingredientId;
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "dishId=" + dishId +
                ", ingredientId=" + ingredientId +
                ", quantityRequired=" + quantityRequired +
                '}';
    }
}
