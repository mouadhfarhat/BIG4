-- Fake data for BIG4 inventory/waste testing
-- Database: project
-- Tables expected: Ingredient, WasteRecord

USE project;

-- Optional cleanup for repeatable tests
DELETE FROM WasteRecord;
DELETE FROM Ingredient;

-- Ingredient seed data
INSERT INTO Ingredient (name, quantityInStock, unit, minStockLevel, unitCost, expiryDate, createdAt) VALUES
('Tomato', 28.00, 'kg', 20.00, 2.30, DATE_ADD(CURDATE(), INTERVAL 4 DAY), NOW() - INTERVAL 40 DAY),
('Potato', 95.00, 'kg', 35.00, 1.40, DATE_ADD(CURDATE(), INTERVAL 18 DAY), NOW() - INTERVAL 35 DAY),
('Chicken Breast', 22.00, 'kg', 18.00, 8.90, DATE_ADD(CURDATE(), INTERVAL 3 DAY), NOW() - INTERVAL 30 DAY),
('Olive Oil', 16.00, 'l', 8.00, 6.20, DATE_ADD(CURDATE(), INTERVAL 120 DAY), NOW() - INTERVAL 60 DAY),
('Rice', 120.00, 'kg', 40.00, 1.80, DATE_ADD(CURDATE(), INTERVAL 220 DAY), NOW() - INTERVAL 75 DAY),
('Cheese', 18.00, 'kg', 14.00, 10.50, DATE_ADD(CURDATE(), INTERVAL 7 DAY), NOW() - INTERVAL 22 DAY),
('Onion', 42.00, 'kg', 20.00, 1.90, DATE_ADD(CURDATE(), INTERVAL 20 DAY), NOW() - INTERVAL 45 DAY),
('Milk', 24.00, 'l', 16.00, 1.60, DATE_ADD(CURDATE(), INTERVAL 5 DAY), NOW() - INTERVAL 18 DAY),
('Eggs', 190.00, 'unit', 90.00, 0.25, DATE_ADD(CURDATE(), INTERVAL 10 DAY), NOW() - INTERVAL 28 DAY),
('Flour', 70.00, 'kg', 25.00, 1.10, DATE_ADD(CURDATE(), INTERVAL 180 DAY), NOW() - INTERVAL 55 DAY),
('Carrot', 12.00, 'kg', 15.00, 2.00, DATE_ADD(CURDATE(), INTERVAL 6 DAY), NOW() - INTERVAL 25 DAY),
('Bell Pepper', 9.00, 'kg', 12.00, 3.40, DATE_ADD(CURDATE(), INTERVAL 3 DAY), NOW() - INTERVAL 20 DAY);

-- WasteRecord seed data (last 30 days trend)
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 2.5, 'Spoilage', NOW() - INTERVAL 29 DAY, 'Temperature fluctuation' FROM Ingredient i WHERE i.name='Tomato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 2.0, 'Spoilage', NOW() - INTERVAL 23 DAY, 'Over-prep' FROM Ingredient i WHERE i.name='Tomato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.8, 'Expired', NOW() - INTERVAL 16 DAY, 'Shelf-life exceeded' FROM Ingredient i WHERE i.name='Tomato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 2.1, 'Spoilage', NOW() - INTERVAL 9 DAY, 'Storage issue' FROM Ingredient i WHERE i.name='Tomato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.9, 'Preparation Loss', NOW() - INTERVAL 3 DAY, 'Trim loss' FROM Ingredient i WHERE i.name='Tomato';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.2, 'Preparation Loss', NOW() - INTERVAL 26 DAY, 'Peeling waste' FROM Ingredient i WHERE i.name='Potato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.5, 'Preparation Loss', NOW() - INTERVAL 18 DAY, 'Cutting waste' FROM Ingredient i WHERE i.name='Potato';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.0, 'Spoilage', NOW() - INTERVAL 7 DAY, 'Delayed usage' FROM Ingredient i WHERE i.name='Potato';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.6, 'Spoilage', NOW() - INTERVAL 21 DAY, 'Cold chain break' FROM Ingredient i WHERE i.name='Chicken Breast';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 2.2, 'Expired', NOW() - INTERVAL 12 DAY, 'Late prep planning' FROM Ingredient i WHERE i.name='Chicken Breast';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.9, 'Spoilage', NOW() - INTERVAL 4 DAY, 'Unplanned demand drop' FROM Ingredient i WHERE i.name='Chicken Breast';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.6, 'Preparation Loss', NOW() - INTERVAL 20 DAY, 'Cutting trim' FROM Ingredient i WHERE i.name='Cheese';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.8, 'Expired', NOW() - INTERVAL 11 DAY, 'Opened too early' FROM Ingredient i WHERE i.name='Cheese';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.7, 'Spoilage', NOW() - INTERVAL 5 DAY, 'Humidity issue' FROM Ingredient i WHERE i.name='Cheese';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.9, 'Spoilage', NOW() - INTERVAL 17 DAY, 'Broken packaging' FROM Ingredient i WHERE i.name='Milk';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.1, 'Expired', NOW() - INTERVAL 8 DAY, 'Demand misforecast' FROM Ingredient i WHERE i.name='Milk';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.7, 'Spoilage', NOW() - INTERVAL 2 DAY, 'Over-ordering' FROM Ingredient i WHERE i.name='Milk';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 25, 'Customer Return', NOW() - INTERVAL 14 DAY, 'Batch quality issue' FROM Ingredient i WHERE i.name='Eggs';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 14, 'Expired', NOW() - INTERVAL 6 DAY, 'Slow turnover' FROM Ingredient i WHERE i.name='Eggs';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.4, 'Preparation Loss', NOW() - INTERVAL 19 DAY, 'Peel loss' FROM Ingredient i WHERE i.name='Onion';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.1, 'Preparation Loss', NOW() - INTERVAL 10 DAY, 'Trim loss' FROM Ingredient i WHERE i.name='Onion';

INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 0.8, 'Spoilage', NOW() - INTERVAL 13 DAY, 'Storage humidity' FROM Ingredient i WHERE i.name='Bell Pepper';
INSERT INTO WasteRecord (ingredientId, quantityWasted, wasteType, date, reason)
SELECT i.id, 1.0, 'Expired', NOW() - INTERVAL 1 DAY, 'Not prioritized for prep' FROM Ingredient i WHERE i.name='Bell Pepper';

-- Quick validation queries
-- SELECT COUNT(*) FROM Ingredient;
-- SELECT COUNT(*) FROM WasteRecord;
-- SELECT name, quantityInStock, minStockLevel FROM Ingredient ORDER BY name;
