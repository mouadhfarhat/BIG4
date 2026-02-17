-- Run this in MySQL if you get: Field 'delivery_man_id' doesn't have a default value
-- (e.g. mysql -u root -p project < fix_delivery_man_autoincrement.sql)

ALTER TABLE delivery_man MODIFY delivery_man_id BIGINT NOT NULL AUTO_INCREMENT;
