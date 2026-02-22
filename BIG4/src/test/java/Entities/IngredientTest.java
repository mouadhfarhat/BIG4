package Entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Ingredient entity")
class IngredientTest {

    private Ingredient buildIngredient() {
        return new Ingredient(1L, "Tomato", 25.5, "kg", 5.0, 2.75, LocalDate.of(2025, 5, 10));
    }

    @Nested
    @DisplayName("Creation")
    class Creation {
        @Test
        @DisplayName("Populates all fields from constructor")
        void populatesFieldsFromConstructor() {
            Ingredient ingredient = buildIngredient();

            assertAll(
                () -> assertEquals(1L, ingredient.getId()),
                () -> assertEquals("Tomato", ingredient.getName()),
                () -> assertEquals(25.5, ingredient.getQuantityInStock()),
                () -> assertEquals("kg", ingredient.getUnit()),
                () -> assertEquals(5.0, ingredient.getMinStockLevel()),
                () -> assertEquals(2.75, ingredient.getUnitCost()),
                () -> assertEquals(LocalDate.of(2025, 5, 10), ingredient.getExpiryDate())
            );
        }
    }

    @Nested
    @DisplayName("Updates")
    class Updates {
        @Test
        @DisplayName("Allows mutating quantities and metadata")
        void allowsMutatingFields() {
            Ingredient ingredient = buildIngredient();

            ingredient.setName("Roma Tomato");
            ingredient.setQuantityInStock(12.0);
            ingredient.setUnit("g");
            ingredient.setMinStockLevel(1.5);
            ingredient.setUnitCost(0.5);
            ingredient.setExpiryDate(LocalDate.of(2025, 6, 1));

            assertAll(
                () -> assertEquals("Roma Tomato", ingredient.getName()),
                () -> assertEquals(12.0, ingredient.getQuantityInStock()),
                () -> assertEquals("g", ingredient.getUnit()),
                () -> assertEquals(1.5, ingredient.getMinStockLevel()),
                () -> assertEquals(0.5, ingredient.getUnitCost()),
                () -> assertEquals(LocalDate.of(2025, 6, 1), ingredient.getExpiryDate())
            );
        }

        @Test
        @DisplayName("toString contains key fields")
        void toStringContainsValues() {
            Ingredient ingredient = buildIngredient();
            String rendered = ingredient.toString();

            assertAll(
                () -> assertTrue(rendered.contains("Tomato")),
                () -> assertTrue(rendered.contains("25.5")),
                () -> assertTrue(rendered.contains("kg")),
                () -> assertTrue(rendered.contains("2025-05-10"))
            );
        }
    }
}
