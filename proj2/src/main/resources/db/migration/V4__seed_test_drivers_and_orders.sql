-- ===================================================================
-- V4__seed_test_drivers_and_orders.sql
-- Seed test drivers with different locations and orders at various distances
-- for testing the driver order assignment and distance-based filtering
-- ===================================================================

-- ============= ENSURE DRIVER USER HAS DRIVER ROLE ==============
-- Make sure the driver user created by DataInitializer has the DRIVER role
INSERT INTO user_roles (user_id, role)
SELECT id, 'DRIVER'
FROM users
WHERE email = 'driver@boozebuddies.com'
  AND id NOT IN (SELECT user_id FROM user_roles WHERE role = 'DRIVER' AND user_id = (SELECT id FROM users WHERE email = 'driver@boozebuddies.com'));

-- ============= CREATE ADDITIONAL TEST DRIVERS ==============
-- Create test drivers at different locations around Raleigh, NC
-- Driver locations are spread out to test distance-based order assignment

-- Driver 2: North Raleigh (about 5km from downtown)
INSERT INTO users (name, email, password_hash, phone, is_active, is_email_verified, age_verified, latitude, longitude, created_at, updated_at)
VALUES 
  ('Alice Driver', 'driver2@boozebuddies.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '(555) 234-5678', TRUE, TRUE, TRUE, 35.8200, -78.6380, NOW(), NOW());

SET @driver2_user_id = LAST_INSERT_ID();

INSERT INTO user_roles (user_id, role)
VALUES (@driver2_user_id, 'DRIVER');

INSERT INTO drivers (user_id, name, email, phone, vehicle_type, license_plate, is_available, current_latitude, current_longitude, rating, total_deliveries, certification_status, certification_number, certification_type, issue_date, expiry_date, valid, created_at, updated_at)
VALUES 
  (@driver2_user_id, 'Alice Driver', 'driver2@boozebuddies.com', '(555) 234-5678', 'SUV', 'DRV-002', TRUE, 35.8200, -78.6380, 4.9, 45, 'APPROVED', 'CERT-DRV-002', 'Alcohol Delivery', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), DATE_ADD(CURDATE(), INTERVAL 9 MONTH), TRUE, NOW(), NOW());

-- Driver 3: East Raleigh (about 3km from downtown)
INSERT INTO users (name, email, password_hash, phone, is_active, is_email_verified, age_verified, latitude, longitude, created_at, updated_at)
VALUES 
  ('Bob Driver', 'driver3@boozebuddies.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '(555) 345-6789', TRUE, TRUE, TRUE, 35.7796, -78.6000, NOW(), NOW());

SET @driver3_user_id = LAST_INSERT_ID();

INSERT INTO user_roles (user_id, role)
VALUES (@driver3_user_id, 'DRIVER');

INSERT INTO drivers (user_id, name, email, phone, vehicle_type, license_plate, is_available, current_latitude, current_longitude, rating, total_deliveries, certification_status, certification_number, certification_type, issue_date, expiry_date, valid, created_at, updated_at)
VALUES 
  (@driver3_user_id, 'Bob Driver', 'driver3@boozebuddies.com', '(555) 345-6789', 'Motorcycle', 'DRV-003', TRUE, 35.7796, -78.6000, 4.6, 32, 'APPROVED', 'CERT-DRV-003', 'Alcohol Delivery', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH), TRUE, NOW(), NOW());

-- Driver 4: West Raleigh (about 4km from downtown)
INSERT INTO users (name, email, password_hash, phone, is_active, is_email_verified, age_verified, latitude, longitude, created_at, updated_at)
VALUES 
  ('Charlie Driver', 'driver4@boozebuddies.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '(555) 456-7890', TRUE, TRUE, TRUE, 35.7796, -78.6800, NOW(), NOW());

SET @driver4_user_id = LAST_INSERT_ID();

INSERT INTO user_roles (user_id, role)
VALUES (@driver4_user_id, 'DRIVER');

INSERT INTO drivers (user_id, name, email, phone, vehicle_type, license_plate, is_available, current_latitude, current_longitude, rating, total_deliveries, certification_status, certification_number, certification_type, issue_date, expiry_date, valid, created_at, updated_at)
VALUES 
  (@driver4_user_id, 'Charlie Driver', 'driver4@boozebuddies.com', '(555) 456-7890', 'Car', 'DRV-004', TRUE, 35.7796, -78.6800, 4.7, 28, 'APPROVED', 'CERT-DRV-004', 'Alcohol Delivery', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_ADD(CURDATE(), INTERVAL 10 MONTH), TRUE, NOW(), NOW());

-- Driver 5: South Raleigh (about 6km from downtown) - Currently offline
INSERT INTO users (name, email, password_hash, phone, is_active, is_email_verified, age_verified, latitude, longitude, created_at, updated_at)
VALUES 
  ('Diana Driver', 'driver5@boozebuddies.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '(555) 567-8901', TRUE, TRUE, TRUE, 35.7500, -78.6380, NOW(), NOW());

SET @driver5_user_id = LAST_INSERT_ID();

INSERT INTO user_roles (user_id, role)
VALUES (@driver5_user_id, 'DRIVER');

INSERT INTO drivers (user_id, name, email, phone, vehicle_type, license_plate, is_available, current_latitude, current_longitude, rating, total_deliveries, certification_status, certification_number, certification_type, issue_date, expiry_date, valid, created_at, updated_at)
VALUES 
  (@driver5_user_id, 'Diana Driver', 'driver5@boozebuddies.com', '(555) 567-8901', 'Car', 'DRV-005', FALSE, 35.7500, -78.6380, 4.5, 15, 'APPROVED', 'CERT-DRV-005', 'Alcohol Delivery', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 7 MONTH), TRUE, NOW(), NOW());

-- ============= CREATE TEST ORDERS AT DIFFERENT DISTANCES ==============
-- Create orders with different statuses at various locations
-- Orders are placed at different distances from driver locations to test filtering

-- Ensure test user exists (created by DataInitializer, but may not exist on fresh DB)
INSERT INTO users (name, email, password_hash, phone, is_active, is_email_verified, age_verified, latitude, longitude, created_at, updated_at)
SELECT 'User', 'user@boozebuddies.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '(555) 111-2222', TRUE, TRUE, TRUE, 35.7830, -78.6380, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user@boozebuddies.com');

-- Ensure test user has USER role
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER'
FROM users
WHERE email = 'user@boozebuddies.com'
  AND id NOT IN (SELECT user_id FROM user_roles WHERE role = 'USER' AND user_id = (SELECT id FROM users WHERE email = 'user@boozebuddies.com'));

-- Get user and merchant IDs (user should exist after the INSERT above)
SET @test_user_id = (SELECT id FROM users WHERE email = 'user@boozebuddies.com' LIMIT 1);
SET @merchant1_id = (SELECT id FROM merchants WHERE name = 'Red Dragon Brewery' LIMIT 1);
SET @merchant2_id = (SELECT id FROM merchants WHERE name = 'Black Label Bar' LIMIT 1);
SET @merchant3_id = (SELECT id FROM merchants WHERE name = 'Crimson Tap House' LIMIT 1);
SET @merchant4_id = (SELECT id FROM merchants WHERE name = 'Scarlet Wine Bar' LIMIT 1);
SET @merchant5_id = (SELECT id FROM merchants WHERE name = 'Burgundy Pub' LIMIT 1);

-- Order 1: PENDING - Close to driver (within 2km) - Red Dragon Brewery
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant1_id, NULL, 'PENDING', 'DELIVERY', 25.97, '123 Main St, Raleigh, NC 27601', 'Leave at door', TRUE, 23.97, 2.00, 0.00, 0.00, NOW(), NOW());

SET @order1_id = LAST_INSERT_ID();

-- Order 2: CONFIRMED - Medium distance (3-5km) - Black Label Bar
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant2_id, NULL, 'CONFIRMED', 'DELIVERY', 40.47, '456 Oak Ave, Raleigh, NC 27602', 'Ring doorbell', TRUE, 37.47, 3.00, 0.00, 0.00, NOW(), NOW());

SET @order2_id = LAST_INSERT_ID();

-- Order 3: PREPARING - Close to driver (within 1km) - Crimson Tap House
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant3_id, NULL, 'PREPARING', 'DELIVERY', 14.48, '789 Pine St, Raleigh, NC 27603', NULL, TRUE, 14.48, 0.00, 0.00, 0.00, NOW(), NOW());

SET @order3_id = LAST_INSERT_ID();

-- Order 4: READY_FOR_PICKUP - Far from driver (6-8km) - Scarlet Wine Bar
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant4_id, NULL, 'READY_FOR_PICKUP', 'DELIVERY', 33.47, '101 Elm Blvd, Raleigh, NC 27604', 'Call when arriving', TRUE, 30.97, 2.50, 0.00, 0.00, NOW(), NOW());

SET @order4_id = LAST_INSERT_ID();

-- Order 5: PENDING - Very close (within 500m) - Burgundy Pub
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant5_id, NULL, 'PENDING', 'DELIVERY', 24.47, '202 Maple Dr, Raleigh, NC 27605', NULL, TRUE, 24.47, 0.00, 0.00, 0.00, NOW(), NOW());

SET @order5_id = LAST_INSERT_ID();

-- Order 6: CONFIRMED - Medium distance (4km) - Red Dragon Brewery
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant1_id, NULL, 'CONFIRMED', 'DELIVERY', 18.98, '303 Cedar Ln, Raleigh, NC 27606', 'Leave with neighbor', TRUE, 17.98, 1.00, 0.00, 0.00, NOW(), NOW());

SET @order6_id = LAST_INSERT_ID();

-- Order 7: PREPARING - Far distance (7km) - Black Label Bar
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant2_id, NULL, 'PREPARING', 'DELIVERY', 28.48, '404 Birch Way, Raleigh, NC 27607', NULL, TRUE, 26.48, 2.00, 0.00, 0.00, NOW(), NOW());

SET @order7_id = LAST_INSERT_ID();

-- Order 8: READY_FOR_PICKUP - Close (1.5km) - Crimson Tap House
INSERT INTO orders (user_id, merchant_id, driver_id, status, order_type, total_amount, delivery_address, special_instructions, age_verified, subtotal, tax, tip, fees, created_at, updated_at)
VALUES 
  (@test_user_id, @merchant3_id, NULL, 'READY_FOR_PICKUP', 'DELIVERY', 13.98, '505 Spruce Ct, Raleigh, NC 27608', 'Gate code: 1234', TRUE, 13.98, 0.00, 0.00, 0.00, NOW(), NOW());

SET @order8_id = LAST_INSERT_ID();

-- ============= CREATE ORDER ITEMS FOR TEST ORDERS ==============
-- Get product IDs
SET @product1_id = (SELECT id FROM products WHERE name = 'Craft IPA' LIMIT 1);
SET @product2_id = (SELECT id FROM products WHERE name = 'Whiskey Sour' LIMIT 1);
SET @product3_id = (SELECT id FROM products WHERE name = 'Amber Lager' LIMIT 1);
SET @product4_id = (SELECT id FROM products WHERE name = 'Pinot Noir' LIMIT 1);
SET @product5_id = (SELECT id FROM products WHERE name = 'House Lager' LIMIT 1);
SET @product6_id = (SELECT id FROM products WHERE name = 'Pretzel Bites' LIMIT 1);

-- Order 1 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order1_id, @product1_id, 1, 'Craft IPA', 8.99, 2, 17.98),
  (@order1_id, @product6_id, 2, 'Pretzel Bites', 5.99, 1, 5.99);

-- Order 2 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order2_id, @product2_id, 1, 'Whiskey Sour', 12.99, 2, 25.98),
  (@order2_id, @product6_id, 2, 'Pretzel Bites', 5.99, 2, 11.49);

-- Order 3 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order3_id, @product3_id, 1, 'Amber Lager', 6.99, 2, 13.98);

-- Order 4 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order4_id, @product4_id, 1, 'Pinot Noir', 13.99, 2, 27.98),
  (@order4_id, @product6_id, 2, 'Pretzel Bites', 5.99, 1, 5.99);

-- Order 5 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order5_id, @product5_id, 1, 'House Lager', 5.99, 4, 23.96);

-- Order 6 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order6_id, @product1_id, 1, 'Craft IPA', 8.99, 2, 17.98);

-- Order 7 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order7_id, @product2_id, 1, 'Whiskey Sour', 12.99, 2, 25.98);

-- Order 8 items
INSERT INTO order_items (order_id, product_id, line_no, name, unit_price, quantity, subtotal)
VALUES 
  (@order8_id, @product3_id, 1, 'Amber Lager', 6.99, 2, 13.98);

-- ============= NOTES ==============
-- Test driver credentials (all use password: "password"):
-- Password hash format: $2a$10$... (bcrypt with cost factor 10)
-- If login fails, verify the password hash matches Spring Security's BCryptPasswordEncoder
-- - driver@boozebuddies.com (Demo Driver) - Downtown Raleigh (35.7800, -78.6380)
-- - driver2@boozebuddies.com (Alice Driver) - North Raleigh (35.8200, -78.6380) ~5km
-- - driver3@boozebuddies.com (Bob Driver) - East Raleigh (35.7796, -78.6000) ~3km
-- - driver4@boozebuddies.com (Charlie Driver) - West Raleigh (35.7796, -78.6800) ~4km
-- - driver5@boozebuddies.com (Diana Driver) - South Raleigh (35.7500, -78.6380) ~6km (OFFLINE)
--
-- Test orders are created with different statuses:
-- - PENDING: Orders 1, 5
-- - CONFIRMED: Orders 2, 6
-- - PREPARING: Orders 3, 7
-- - READY_FOR_PICKUP: Orders 4, 8
--
-- All orders are unassigned (driver_id = NULL) and ready for driver assignment
-- Orders are placed at various distances from merchant locations to test distance filtering

