-- =========================================================
-- BIG4 / PROJECT - Full Database Script (Adjusted)
-- Includes donation optimization schema extensions
-- MariaDB 10.4+ compatible
-- =========================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE DATABASE IF NOT EXISTS `project` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `project`;

-- =========================================================
-- DROP TABLES (safe reset order)
-- =========================================================
DROP TABLE IF EXISTS `dish_ingredient`;
DROP TABLE IF EXISTS `food_donation_items`;
DROP TABLE IF EXISTS `sustainability_metrics`;
DROP TABLE IF EXISTS `food_donation_event`;
DROP TABLE IF EXISTS `wasterecord`;
DROP TABLE IF EXISTS `dish`;
DROP TABLE IF EXISTS `ingredient`;
DROP TABLE IF EXISTS `menu`;
DROP TABLE IF EXISTS `fleet_car`;
DROP TABLE IF EXISTS `delivery`;
DROP TABLE IF EXISTS `delivery_man`;
DROP TABLE IF EXISTS `user1`;
DROP TABLE IF EXISTS `user`;

-- =========================================================
-- TABLE: delivery_man
-- =========================================================
CREATE TABLE `delivery_man` (
  `delivery_man_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `vehicle_type` varchar(50) DEFAULT NULL,
  `vehicle_number` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'ACTIVE',
  `address` varchar(255) DEFAULT NULL,
  `salary` decimal(10,2) DEFAULT NULL,
  `date_of_joining` date DEFAULT NULL,
  `rating` decimal(3,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`delivery_man_id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `vehicle_number` (`vehicle_number`),
  KEY `idx_status` (`status`),
  KEY `idx_phone` (`phone`),
  KEY `idx_vehicle_type` (`vehicle_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- TABLE: delivery
-- =========================================================
CREATE TABLE `delivery` (
  `delivery_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `delivery_man_id` bigint(20) DEFAULT NULL,
  `delivery_address` varchar(255) NOT NULL,
  `recipient_name` varchar(100) DEFAULT NULL,
  `recipient_phone` varchar(20) DEFAULT NULL,
  `pickup_location` varchar(255) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'PENDING',
  `scheduled_date` timestamp NULL DEFAULT NULL,
  `actual_delivery_date` timestamp NULL DEFAULT current_timestamp(),
  `estimated_time` int(11) DEFAULT NULL,
  `current_latitude` decimal(10,8) DEFAULT NULL,
  `current_longitude` decimal(11,8) DEFAULT NULL,
  `delivery_notes` text DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`delivery_id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_delivery_man_id` (`delivery_man_id`),
  KEY `idx_scheduled_date` (`scheduled_date`),
  CONSTRAINT `fk_delivery_man` FOREIGN KEY (`delivery_man_id`) REFERENCES `delivery_man` (`delivery_man_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: fleet_car
-- =========================================================
CREATE TABLE `fleet_car` (
  `car_id` bigint(20) NOT NULL,
  `make` varchar(128) NOT NULL DEFAULT '',
  `model` varchar(128) NOT NULL DEFAULT '',
  `license_plate` varchar(64) NOT NULL DEFAULT '',
  `vehicle_type` varchar(64) NOT NULL DEFAULT 'Sedan',
  `delivery_man_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`car_id`),
  UNIQUE KEY `uk_fleet_delivery_man` (`delivery_man_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: menu
-- =========================================================
CREATE TABLE `menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(120) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `isActive` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: dish
-- =========================================================
CREATE TABLE `dish` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `menu_id` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `base_price` decimal(10,2) NOT NULL,
  `available` tinyint(1) NOT NULL DEFAULT 1,
  `stock_quantity` int(11) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_dish_menu` (`menu_id`),
  CONSTRAINT `fk_dish_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: ingredient (adjusted for optimization)
-- =========================================================
CREATE TABLE `ingredient` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `quantityInStock` double NOT NULL,
  `unit` varchar(50) NOT NULL,
  `createdAt` datetime DEFAULT NULL,
  `minStockLevel` double NOT NULL,
  `unitCost` decimal(10,2) NOT NULL,
  `expiryDate` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ingredient_expiry_stock` (`expiryDate`,`quantityInStock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: wasterecord
-- =========================================================
CREATE TABLE `wasterecord` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ingredientId` int(11) NOT NULL,
  `quantityWasted` double NOT NULL,
  `wasteType` varchar(255) NOT NULL,
  `date` date NOT NULL,
  `reason` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ingredientId` (`ingredientId`),
  KEY `idx_wasterecord_ingredient_date` (`ingredientId`,`date`),
  CONSTRAINT `fk_waste_ingredients` FOREIGN KEY (`ingredientId`) REFERENCES `ingredient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: food_donation_event
-- =========================================================
CREATE TABLE `food_donation_event` (
  `donation_event_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_date` date NOT NULL,
  `total_quantity` int(11) NOT NULL,
  `charity_name` varchar(100) NOT NULL,
  `status` varchar(50) DEFAULT 'PENDING',
  `delivery_id` int(11) DEFAULT NULL,
  `calendar_event_id` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`donation_event_id`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_status` (`status`),
  KEY `idx_delivery_id` (`delivery_id`),
  KEY `idx_food_donation_event_status_date` (`status`,`event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: food_donation_items (adjusted with FKs)
-- =========================================================
CREATE TABLE `food_donation_items` (
  `donation_event_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  PRIMARY KEY (`donation_event_id`,`item_id`),
  KEY `idx_item_id` (`item_id`),
  CONSTRAINT `fk_food_donation_items_event` FOREIGN KEY (`donation_event_id`) REFERENCES `food_donation_event` (`donation_event_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_food_donation_items_dish` FOREIGN KEY (`item_id`) REFERENCES `dish` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: dish_ingredient (NEW for optimization)
-- =========================================================
CREATE TABLE `dish_ingredient` (
  `dish_id` int(11) NOT NULL,
  `ingredient_id` int(11) NOT NULL,
  `quantity_required` double NOT NULL,
  PRIMARY KEY (`dish_id`,`ingredient_id`),
  KEY `idx_dish_ingredient_ingredient` (`ingredient_id`),
  CONSTRAINT `fk_dish_ingredient_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_dish_ingredient_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE `food_donation_event`
  ADD CONSTRAINT chk_food_donation_event_total_qty CHECK (`total_quantity` > 0);

ALTER TABLE `food_donation_items`
  ADD CONSTRAINT chk_food_donation_items_qty CHECK (`quantity` > 0);

ALTER TABLE `dish_ingredient`
  ADD CONSTRAINT chk_dish_ingredient_qty CHECK (`quantity_required` > 0);

-- =========================================================
-- TABLE: sustainability_metrics
-- =========================================================
CREATE TABLE `sustainability_metrics` (
  `metric_id` int(11) NOT NULL AUTO_INCREMENT,
  `donation_event_id` int(11) NOT NULL,
  `total_quantity` int(11) NOT NULL,
  `meals_provided` int(11) NOT NULL,
  `co2_saved_kg` decimal(10,2) NOT NULL,
  `cost_saved` decimal(12,2) DEFAULT NULL,
  `calculated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`metric_id`),
  KEY `idx_donation_event_id` (`donation_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: user
-- =========================================================
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(512) NOT NULL,
  `role` varchar(32) NOT NULL,
  `reference_id` bigint(20) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `phone` varchar(64) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email_role` (`email`,`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- TABLE: user1
-- =========================================================
CREATE TABLE `user1` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =========================================================
-- OPTIONAL SEED DATA (minimal from your dump)
-- =========================================================
INSERT INTO `delivery_man` (`delivery_man_id`, `name`, `phone`, `email`, `vehicle_type`, `vehicle_number`, `status`, `address`, `salary`, `date_of_joining`, `rating`, `created_at`, `updated_at`) VALUES
(1, 'Aziz', '123456', 'aziz@aziz.com', 'Car', '253', 'ACTIVE', '', 10.00, '2026-02-20', 3.20, '2026-02-20 11:34:07', '2026-02-22 15:14:23');

INSERT INTO `menu` (`id`, `title`, `description`, `isActive`, `created_at`, `updated_at`) VALUES
(10, 'italian menu v2', NULL, 0, '2026-02-12 14:23:24', '2026-02-12 14:23:46');

INSERT INTO `dish` (`id`, `menu_id`, `name`, `description`, `base_price`, `available`, `stock_quantity`, `image_url`, `created_at`, `updated_at`) VALUES
(3, 10, 'makrouna', '', 15.00, 0, 1, '', '2026-02-22 15:41:53', '2026-02-22 15:41:53'),
(4, 10, 'koskous', '', 30.00, 1, 5, '', '2026-02-23 13:55:52', '2026-02-23 13:55:52');

INSERT INTO `ingredient` (`id`, `name`, `quantityInStock`, `unit`, `createdAt`, `minStockLevel`, `unitCost`, `expiryDate`) VALUES
(1, 'viande', 41, 'kg', '2026-02-03 00:00:00', 5, 40.00, '2026-02-23'),
(22, 'Milk', 10, 'L', '2026-02-22 00:00:00', 2, 2.00, '2026-02-23'),
(23, 'Tomato', 28, 'kg', '2026-01-13 00:00:00', 20, 2.00, '2026-02-26'),
(25, 'Chicken Breast', 22, 'kg', '2026-01-23 00:00:00', 18, 9.00, '2026-02-25');

INSERT INTO `food_donation_event` (`donation_event_id`, `event_date`, `total_quantity`, `charity_name`, `status`, `delivery_id`, `calendar_event_id`, `created_at`, `updated_at`) VALUES
(1, '2025-01-10', 120, 'Hope Charity', 'PENDING', NULL, NULL, '2026-02-08 17:59:29', '2026-02-08 17:59:29'),
(2, '2025-01-12', 80, 'Food For All', 'COMPLETED', NULL, NULL, '2026-02-08 17:59:29', '2026-02-08 17:59:29');

INSERT INTO `food_donation_items` (`donation_event_id`, `item_id`, `quantity`) VALUES
(1, 3, 50),
(2, 4, 30);

INSERT INTO `user` (`id`, `email`, `password_hash`, `role`, `reference_id`, `full_name`, `phone`, `address`) VALUES
(1, 'admin@big4.com', '6G94qKPK8LYNjnTllCqm2G3BUM08AzOK7yW30tfjrMc=', 'ADMIN', NULL, 'System Admin', NULL, NULL),
(2, 'aziz@aziz.com', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', 'CLIENT', NULL, 'aziz', '58963741', NULL);

-- Example recipe links (for optimization to produce ranked dishes)
INSERT INTO `dish_ingredient` (`dish_id`, `ingredient_id`, `quantity_required`) VALUES
(3, 22, 1.0),
(3, 23, 1.5),
(4, 23, 2.0),
(4, 25, 1.5)
ON DUPLICATE KEY UPDATE quantity_required = VALUES(quantity_required);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
