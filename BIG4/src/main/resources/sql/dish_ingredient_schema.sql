-- Dish <-> Ingredient many-to-many recipe table
-- quantity_required represents ingredient quantity consumed per 1 dish

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

CREATE INDEX idx_dish_ingredient_ingredient ON dish_ingredient (ingredient_id);
CREATE INDEX idx_ingredient_expiry_qty ON ingredient (expiryDate, quantityInStock);

ALTER TABLE food_donation_items
    ADD CONSTRAINT fk_food_donation_items_event
        FOREIGN KEY (donation_event_id) REFERENCES food_donation_event(donation_event_id)
        ON DELETE CASCADE;

ALTER TABLE food_donation_items
    ADD CONSTRAINT fk_food_donation_items_dish
        FOREIGN KEY (item_id) REFERENCES dish(id)
        ON DELETE CASCADE;

-- Example data
-- INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required)
-- VALUES
--     (1, 10, 5.0),   -- Pizza uses 5 units of Mozzarella
--     (1, 11, 2.0),   -- Pizza uses 2 units of Tuna
--     (1, 12, 1.0);   -- Pizza uses 1 unit of Olive
