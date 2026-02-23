-- Migration for donation optimization feature
-- Safe to run on your current `project` database dump.

-- 1) Recipe link table (Dish <-> Ingredient)
CREATE TABLE IF NOT EXISTS dish_ingredient (
    dish_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity_required DOUBLE NOT NULL,
    PRIMARY KEY (dish_id, ingredient_id),
    CONSTRAINT fk_dish_ingredient_dish
        FOREIGN KEY (dish_id) REFERENCES dish(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dish_ingredient_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
        ON DELETE CASCADE
);

-- 2) Helpful indexes for near-expiry optimization queries
CREATE INDEX idx_dish_ingredient_ingredient ON dish_ingredient (ingredient_id);
CREATE INDEX idx_ingredient_expiry_qty ON ingredient (expiryDate, quantityInStock);

-- 3) Add missing foreign keys for donation items table
-- (only run these if not already present in your DB)
ALTER TABLE food_donation_items
    ADD CONSTRAINT fk_food_donation_items_event
        FOREIGN KEY (donation_event_id) REFERENCES food_donation_event(donation_event_id)
        ON DELETE CASCADE;

ALTER TABLE food_donation_items
    ADD CONSTRAINT fk_food_donation_items_dish
        FOREIGN KEY (item_id) REFERENCES dish(id)
        ON DELETE CASCADE;

-- Optional quality improvements (recommended):
-- ALTER TABLE ingredient MODIFY unitCost DECIMAL(10,2) NOT NULL;
-- ALTER TABLE ingredient MODIFY createdAt DATETIME NULL;
