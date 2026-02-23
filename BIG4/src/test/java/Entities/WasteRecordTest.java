package Entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WasteRecord entity")
class WasteRecordTest {

    private WasteRecord buildWasteRecord() {
        return new WasteRecord(7L, 3L, 2.5, "Expired", LocalDateTime.of(2025, 1, 15, 12, 30), "Past expiry");
    }

    @Nested
    @DisplayName("Creation")
    class Creation {
        @Test
        @DisplayName("Id-only constructor leaves other fields null/zero")
        void idOnlyConstructorDefaults() {
            WasteRecord record = new WasteRecord(5L);

            assertAll(
                () -> assertEquals(5L, record.getId()),
                () -> assertNull(record.getIngredientId()),
                () -> assertEquals(0.0, record.getQuantityWasted()),
                () -> assertNull(record.getWasteType()),
                () -> assertNull(record.getDate()),
                () -> assertNull(record.getReason())
            );
        }

        @Test
        @DisplayName("Full constructor populates all fields")
        void fullConstructorPopulatesFields() {
            WasteRecord record = buildWasteRecord();

            assertAll(
                () -> assertEquals(7L, record.getId()),
                () -> assertEquals(3L, record.getIngredientId()),
                () -> assertEquals(2.5, record.getQuantityWasted()),
                () -> assertEquals("Expired", record.getWasteType()),
                () -> assertEquals(LocalDateTime.of(2025, 1, 15, 12, 30), record.getDate()),
                () -> assertEquals("Past expiry", record.getReason())
            );
        }
    }

    @Nested
    @DisplayName("Updates")
    class Updates {
        @Test
        @DisplayName("Allows mutating properties after creation")
        void allowsMutatingProperties() {
            WasteRecord record = buildWasteRecord();

            record.setId(9L);
            record.setIngredientId(4L);
            record.setQuantityWasted(1.0);
            record.setWasteType("Damaged");
            record.setDate(LocalDateTime.of(2025, 2, 1, 8, 15));
            record.setReason("Packaging failure");

            assertAll(
                () -> assertEquals(9L, record.getId()),
                () -> assertEquals(4L, record.getIngredientId()),
                () -> assertEquals(1.0, record.getQuantityWasted()),
                () -> assertEquals("Damaged", record.getWasteType()),
                () -> assertEquals(LocalDateTime.of(2025, 2, 1, 8, 15), record.getDate()),
                () -> assertEquals("Packaging failure", record.getReason())
            );
        }

        @Test
        @DisplayName("toString contains key fields")
        void toStringContainsValues() {
            WasteRecord record = buildWasteRecord();
            String rendered = record.toString();

            assertAll(
                () -> assertTrue(rendered.contains("7")),
                () -> assertTrue(rendered.contains("3")),
                () -> assertTrue(rendered.contains("2.5")),
                () -> assertTrue(rendered.contains("Expired")),
                () -> assertTrue(rendered.contains("Past expiry"))
            );
        }
    }
}
