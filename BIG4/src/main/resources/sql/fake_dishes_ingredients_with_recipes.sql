-- =========================================================
-- Fake Data: Dishes + Ingredients + Dish Recipes
-- Every dish has its own ingredient lines in dish_ingredient
-- Run AFTER project_full_database_script.sql
-- =========================================================

START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM dish_ingredient;
DELETE FROM dish;
DELETE FROM ingredient;
DELETE FROM menu;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- MENUS
-- ---------------------------------------------------------
INSERT INTO menu (id, title, description, isActive, created_at, updated_at) VALUES
(1001, 'Donation Test Menu', 'Menu for donation optimization testing', 1, NOW(), NOW()),
(1002, 'Kitchen Test Menu', 'Extra menu for recipe variety', 1, NOW(), NOW());

-- ---------------------------------------------------------
-- INGREDIENTS
-- ---------------------------------------------------------
INSERT INTO ingredient (id, name, quantityInStock, unit, createdAt, minStockLevel, unitCost, expiryDate) VALUES
(2001, 'Tomato',         55, 'kg', NOW(), 20, 2.40, CURDATE() + INTERVAL 2 DAY),
(2002, 'Chicken Breast', 36, 'kg', NOW(), 14, 9.80, CURDATE() + INTERVAL 2 DAY),
(2003, 'Milk',           24, 'l',  NOW(), 10, 2.10, CURDATE() + INTERVAL 3 DAY),
(2004, 'Onion',          48, 'kg', NOW(), 20, 1.70, CURDATE() + INTERVAL 1 DAY),
(2005, 'Garlic',         22, 'kg', NOW(),  8, 3.00, CURDATE() + INTERVAL 1 DAY),
(2006, 'Rice',          130, 'kg', NOW(), 45, 1.30, CURDATE() + INTERVAL 60 DAY),
(2007, 'Olive Oil',      28, 'l',  NOW(), 10, 6.50, CURDATE() + INTERVAL 120 DAY),
(2008, 'Cheese',         20, 'kg', NOW(),  9, 10.50, CURDATE() + INTERVAL 8 DAY),
(2009, 'Bell Pepper',    16, 'kg', NOW(),  7, 2.60, CURDATE() + INTERVAL 5 DAY),
(2010, 'Potato',         95, 'kg', NOW(), 35, 1.10, CURDATE() + INTERVAL 25 DAY),
(2011, 'Pasta',          80, 'kg', NOW(), 25, 1.90, CURDATE() + INTERVAL 90 DAY),
(2012, 'Parsley',         8, 'kg', NOW(),  3, 4.20, CURDATE() + INTERVAL 4 DAY),
(2013, 'Carrot',         18, 'kg', NOW(), 10, 1.90, CURDATE() + INTERVAL 6 DAY),
(2014, 'Butter',         14, 'kg', NOW(),  6, 5.80, CURDATE() + INTERVAL 7 DAY),
(2015, 'Flour',          75, 'kg', NOW(), 25, 1.20, CURDATE() + INTERVAL 120 DAY);

-- ---------------------------------------------------------
-- DISHES
-- ---------------------------------------------------------
INSERT INTO dish (id, menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at) VALUES
(3001, 1001, 'Tomato Chicken Stew',      'High near-expiry usage dish', 22.50, 1, 50, '', NOW(), NOW()),
(3002, 1001, 'Creamy Tomato Pasta',      'Tomato + milk based pasta',   19.00, 1, 70, '', NOW(), NOW()),
(3003, 1001, 'Onion Garlic Rice Bowl',   'Onion and garlic focused',    16.00, 1, 80, '', NOW(), NOW()),
(3004, 1001, 'Chicken Rice Plate',       'Chicken with rice and oil',   24.00, 1, 40, '', NOW(), NOW()),
(3005, 1002, 'Cheese Veggie Bake',       'Vegetable + cheese tray',     18.50, 1, 45, '', NOW(), NOW()),
(3006, 1002, 'Potato Carrot Saute',      'Simple hot side dish',        13.50, 1, 90, '', NOW(), NOW()),
(3007, 1002, 'Buttery Garlic Pasta',     'Butter garlic pasta bowl',    17.00, 1, 60, '', NOW(), NOW()),
(3008, 1002, 'Archived Testing Dish',    'Unavailable test record',     11.00, 0, 25, '', NOW(), NOW());

-- ---------------------------------------------------------
-- RECIPE LINES (dish_ingredient)
-- Each dish has its own ingredient composition and quantity
-- quantity_required = quantity consumed per 1 dish portion
-- ---------------------------------------------------------
INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required) VALUES
-- 3001 Tomato Chicken Stew
(3001, 2001, 2.60),
(3001, 2002, 1.80),
(3001, 2004, 0.70),
(3001, 2005, 0.25),
(3001, 2007, 0.20),

-- 3002 Creamy Tomato Pasta
(3002, 2001, 1.90),
(3002, 2003, 1.10),
(3002, 2011, 1.40),
(3002, 2008, 0.45),
(3002, 2014, 0.20),

-- 3003 Onion Garlic Rice Bowl
(3003, 2004, 1.60),
(3003, 2005, 0.55),
(3003, 2006, 1.30),
(3003, 2012, 0.10),

-- 3004 Chicken Rice Plate
(3004, 2002, 1.70),
(3004, 2006, 1.10),
(3004, 2007, 0.25),
(3004, 2013, 0.40),

-- 3005 Cheese Veggie Bake
(3005, 2008, 0.80),
(3005, 2009, 0.90),
(3005, 2010, 1.20),
(3005, 2004, 0.50),

-- 3006 Potato Carrot Saute
(3006, 2010, 1.80),
(3006, 2013, 1.10),
(3006, 2004, 0.40),
(3006, 2007, 0.15),

-- 3007 Buttery Garlic Pasta
(3007, 2011, 1.50),
(3007, 2014, 0.35),
(3007, 2005, 0.30),
(3007, 2008, 0.25),

-- 3008 Archived Testing Dish (still has recipe, but unavailable dish)
(3008, 2001, 1.00),
(3008, 2003, 0.70),
(3008, 2015, 0.60);

COMMIT;

-- Quick check queries:
-- SELECT d.id, d.name, i.name AS ingredient, di.quantity_required
-- FROM dish_ingredient di
-- JOIN dish d ON d.id = di.dish_id
-- JOIN ingredient i ON i.id = di.ingredient_id
-- ORDER BY d.id, i.name;
