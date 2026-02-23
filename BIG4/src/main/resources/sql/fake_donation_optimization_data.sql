-- =========================================================
-- Fake Test Data: Donation Optimization Flow
-- Use AFTER schema creation (project_full_database_script.sql)
-- =========================================================

START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

-- Clear business data in FK-safe order
DELETE FROM dish_ingredient;
DELETE FROM food_donation_items;
DELETE FROM sustainability_metrics;
DELETE FROM food_donation_event;
DELETE FROM wasterecord;
DELETE FROM dish;
DELETE FROM ingredient;
DELETE FROM menu;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- MENUS
-- =========================================================
INSERT INTO menu (id, title, description, isActive, created_at, updated_at) VALUES
(101, 'Rescue Menu', 'Dishes designed for near-expiry ingredient usage', 1, NOW(), NOW()),
(102, 'Classic Menu', 'Regular operational dishes', 1, NOW(), NOW());

-- =========================================================
-- DISHES
-- =========================================================
INSERT INTO dish (id, menu_id, name, description, base_price, available, stock_quantity, image_url, created_at, updated_at) VALUES
(201, 101, 'Tomato Chicken Stew', 'High tomato and chicken usage', 22.50, 1, 60, '', NOW(), NOW()),
(202, 101, 'Creamy Tomato Pasta', 'Uses tomato and milk near expiry', 19.00, 1, 80, '', NOW(), NOW()),
(203, 101, 'Onion Garlic Rice Bowl', 'Strong onion/garlic consumption', 16.00, 1, 90, '', NOW(), NOW()),
(204, 102, 'Grilled Chicken Plate', 'Uses chicken + olive oil', 24.00, 1, 40, '', NOW(), NOW()),
(205, 102, 'Veggie Mix', 'Mixed vegetables', 14.50, 1, 70, '', NOW(), NOW()),
(206, 102, 'Archived Test Dish', 'Not available (should not be recommended)', 10.00, 0, 20, '', NOW(), NOW());

-- =========================================================
-- INGREDIENTS
-- Notes:
-- - Some ingredients expire in <= 3 days to trigger optimization
-- - Others expire later for contrast
-- =========================================================
INSERT INTO ingredient (id, name, quantityInStock, unit, createdAt, minStockLevel, unitCost, expiryDate) VALUES
(301, 'Tomato',         60, 'kg',   NOW(), 18,  2.40, CURDATE() + INTERVAL 1 DAY),
(302, 'Chicken Breast', 35, 'kg',   NOW(), 15, 10.50, CURDATE() + INTERVAL 2 DAY),
(303, 'Milk',           28, 'l',    NOW(), 10,  2.00, CURDATE() + INTERVAL 3 DAY),
(304, 'Onion',          42, 'kg',   NOW(), 20,  1.80, CURDATE() + INTERVAL 2 DAY),
(305, 'Garlic',         22, 'kg',   NOW(),  8,  3.20, CURDATE() + INTERVAL 1 DAY),
(306, 'Rice',          140, 'kg',   NOW(), 45,  1.40, CURDATE() + INTERVAL 40 DAY),
(307, 'Olive Oil',      26, 'l',    NOW(), 12,  6.80, CURDATE() + INTERVAL 90 DAY),
(308, 'Cheese',         18, 'kg',   NOW(), 10, 11.00, CURDATE() + INTERVAL 7 DAY),
(309, 'Bell Pepper',    16, 'kg',   NOW(),  8,  2.70, CURDATE() + INTERVAL 5 DAY),
(310, 'Potato',        100, 'kg',   NOW(), 35,  1.20, CURDATE() + INTERVAL 20 DAY);

-- =========================================================
-- DISH <-> INGREDIENT RECIPE LINKS
-- quantity_required = ingredient quantity per 1 dish portion
-- =========================================================
INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity_required) VALUES
(201, 301, 2.50),  -- Tomato Chicken Stew
(201, 302, 1.70),
(201, 304, 0.70),

(202, 301, 1.80),  -- Creamy Tomato Pasta
(202, 303, 1.00),
(202, 308, 0.40),

(203, 304, 1.50),  -- Onion Garlic Rice Bowl
(203, 305, 0.50),
(203, 306, 1.20),

(204, 302, 1.60),  -- Grilled Chicken Plate
(204, 307, 0.30),
(204, 310, 1.00),

(205, 301, 0.90),  -- Veggie Mix
(205, 304, 0.80),
(205, 309, 0.70),

(206, 301, 1.00);  -- Archived dish (available = 0)

-- =========================================================
-- WASTE HISTORY (for dashboard/testing trends)
-- =========================================================
INSERT INTO wasterecord (id, ingredientId, quantityWasted, wasteType, date, reason) VALUES
(401, 301, 2.4, 'Spoilage',         CURDATE() - INTERVAL 18 DAY, 'Storage temperature drift'),
(402, 301, 1.9, 'Expired',          CURDATE() - INTERVAL 10 DAY, 'Demand overestimate'),
(403, 302, 1.2, 'Spoilage',         CURDATE() - INTERVAL 12 DAY, 'Cold-chain delay'),
(404, 303, 0.9, 'Expired',          CURDATE() - INTERVAL  7 DAY, 'Opened too early'),
(405, 304, 1.5, 'Preparation Loss', CURDATE() - INTERVAL 14 DAY, 'Peel waste'),
(406, 305, 0.7, 'Preparation Loss', CURDATE() - INTERVAL 11 DAY, 'Trim waste'),
(407, 309, 0.8, 'Spoilage',         CURDATE() - INTERVAL  5 DAY, 'Humidity issue');

-- =========================================================
-- DONATION EVENTS + ITEMS (existing examples)
-- =========================================================
INSERT INTO food_donation_event (donation_event_id, event_date, total_quantity, charity_name, status, delivery_id, calendar_event_id, created_at, updated_at) VALUES
(501, CURDATE() - INTERVAL 7 DAY, 120, 'Hope Shelter',      'COMPLETED', NULL, NULL, NOW(), NOW()),
(502, CURDATE() - INTERVAL 3 DAY,  90, 'Food For Families', 'COMPLETED', NULL, NULL, NOW(), NOW()),
(503, CURDATE() + INTERVAL 1 DAY, 100, 'City Relief',       'PENDING',   NULL, NULL, NOW(), NOW());

INSERT INTO food_donation_items (donation_event_id, item_id, quantity) VALUES
(501, 201, 35),
(501, 202, 25),
(501, 203, 20),
(502, 204, 30),
(502, 205, 35),
(503, 201, 20),
(503, 203, 25);

-- =========================================================
-- SUSTAINABILITY METRICS
-- =========================================================
INSERT INTO sustainability_metrics (metric_id, donation_event_id, total_quantity, meals_provided, co2_saved_kg, cost_saved, calculated_at) VALUES
(601, 501, 120, 240, 37.20, 225.00, NOW()),
(602, 502,  90, 180, 28.50, 164.00, NOW());

COMMIT;

-- Expected optimization behavior with nearExpiryDays=3:
-- Near-expiry ingredients: Tomato(301), Chicken(302), Milk(303), Onion(304), Garlic(305)
-- Candidate dishes: 201, 202, 203, 204, 205 (206 excluded because available=0)
-- Ranking should prioritize dishes consuming most near-expiry quantity and cost value.
