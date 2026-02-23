-- Adjusted database script for Donation Optimization
-- Based on your current `project` schema dump.
-- Target: MariaDB 10.4.x

START TRANSACTION;

-- =========================================================
-- 1) Normalize ingredient columns for better precision/use
-- =========================================================
ALTER TABLE ingredient
  MODIFY COLUMN unitCost DECIMAL(10,2) NOT NULL,
  MODIFY COLUMN createdAt DATETIME NULL;

-- Helpful index for near-expiry lookup + stock filtering
CREATE INDEX idx_ingredient_expiry_stock ON ingredient (expiryDate, quantityInStock);

-- =========================================================
-- 2) Add Dish <-> Ingredient recipe relationship (M:N)
-- =========================================================
CREATE TABLE IF NOT EXISTS dish_ingredient (
  dish_id INT NOT NULL,
  ingredient_id INT NOT NULL,
  quantity_required DOUBLE NOT NULL,
  PRIMARY KEY (dish_id, ingredient_id),
  CONSTRAINT fk_dish_ingredient_dish
    FOREIGN KEY (dish_id) REFERENCES dish(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_dish_ingredient_ingredient
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_dish_ingredient_ingredient ON dish_ingredient (ingredient_id);

-- =========================================================
-- 3) Ensure donation items table has real FK integrity
-- =========================================================
ALTER TABLE food_donation_items
  ADD CONSTRAINT fk_food_donation_items_event
    FOREIGN KEY (donation_event_id) REFERENCES food_donation_event(donation_event_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT fk_food_donation_items_dish
    FOREIGN KEY (item_id) REFERENCES dish(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- =========================================================
-- 4) Optional quality indexes for existing usage patterns
-- =========================================================
CREATE INDEX idx_food_donation_event_status_date ON food_donation_event (status, event_date);
CREATE INDEX idx_wasterecord_ingredient_date ON wasterecord (ingredientId, date);

COMMIT;

-- ---------------------------------------------------------
-- Optional sample recipe links (replace IDs with your real data)
-- ---------------------------------------------------------
-- INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required) VALUES
--   (4, 23, 2.0), -- dish 4 uses 2.0 KG tomato
--   (4, 15, 1.0), -- dish 4 uses 1.0 KG onion
--   (4, 25, 3.0); -- dish 4 uses 3.0 KG chicken breast
