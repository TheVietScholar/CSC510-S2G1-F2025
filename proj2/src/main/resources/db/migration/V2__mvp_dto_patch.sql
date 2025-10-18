-- ===== Users: add auth + age + role =====
ALTER TABLE users
  ADD COLUMN password_hash VARCHAR(255) NULL AFTER email,
  ADD COLUMN date_of_birth DATE NULL AFTER password_hash,
  ADD COLUMN role ENUM('CUSTOMER','RESTAURANT_OWNER','DRIVER','ADMIN') NOT NULL DEFAULT 'CUSTOMER' AFTER date_of_birth;

-- Optional: default delivery address marker
ALTER TABLE user_addresses
  ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE AFTER user_id,
  ADD UNIQUE KEY uq_user_default_address (user_id, is_default);

-- ===== Merchants: minimal address + tax =====
ALTER TABLE merchants
  MODIFY COLUMN status ENUM('OPEN','CLOSED','PAUSED') NOT NULL,
  ADD COLUMN line1 VARCHAR(255) NULL AFTER name,
  ADD COLUMN city VARCHAR(120) NULL AFTER line1,
  ADD COLUMN state VARCHAR(120) NULL AFTER city,
  ADD COLUMN postal_code VARCHAR(20) NULL AFTER state,
  ADD COLUMN tax_rate_food DECIMAL(6,4) NOT NULL DEFAULT 0.0200 AFTER status;

-- ===== Products: hot-path index =====
CREATE INDEX idx_products_available ON products(is_available);

-- ===== Orders: type/fees/promo + constrained status + bookkeeping =====
ALTER TABLE orders
  MODIFY COLUMN status ENUM(
  'CREATED','AUTHORIZED','ACCEPTED','PREPARING','READY',
  'EN_ROUTE','DELIVERED','CANCELLED','REJECTED','REFUNDED','AUTHORIZATION_FAILED'
  ) NOT NULL,
  ADD COLUMN order_type ENUM('DELIVERY','PICKUP') NOT NULL DEFAULT 'DELIVERY' AFTER status,
  ADD COLUMN fees_cents INT NOT NULL DEFAULT 0 AFTER tip_cents,
  ADD COLUMN promo_code VARCHAR(64) NULL AFTER fees_cents,
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP;

-- useful indexes
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_merchant_status ON orders(merchant_id, status);

-- ===== Deliveries: make 1:1 with order and enrich status/timestamps =====
ALTER TABLE deliveries
  MODIFY COLUMN status ENUM('UNASSIGNED','ASSIGNED','PICKED_UP','EN_ROUTE','DELIVERED','FAILED','CANCELLED') NOT NULL,
  ADD COLUMN assigned_at TIMESTAMP NULL AFTER status,
  ADD COLUMN eta_seconds INT NULL AFTER assigned_at,
  ADD COLUMN proof_photo_url VARCHAR(512) NULL AFTER handoff_code;

-- 1:1 order_id uniqueness
ALTER TABLE deliveries
  ADD UNIQUE KEY uq_deliveries_order (order_id);

-- status + driver hotpath
CREATE INDEX idx_deliveries_status ON deliveries(status);

-- ===== Drivers: link to user + certification + vehicle + last loc =====
ALTER TABLE drivers
  ADD COLUMN user_id BIGINT NULL AFTER id,
  ADD COLUMN certification_status ENUM('PENDING','VERIFIED','EXPIRED','REVOKED') NOT NULL DEFAULT 'PENDING' AFTER is_available,
  ADD COLUMN vehicle_make VARCHAR(80) NULL AFTER certification_status,
  ADD COLUMN vehicle_model VARCHAR(80) NULL AFTER vehicle_make,
  ADD COLUMN vehicle_plate VARCHAR(40) NULL AFTER vehicle_model,
  ADD COLUMN last_lat DOUBLE NULL AFTER rating,
  ADD COLUMN last_lng DOUBLE NULL AFTER last_lat,
  ADD CONSTRAINT fk_drivers_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_drivers_available ON drivers(is_available);

-- ===== Payments: 1:1 with order + enum status =====
ALTER TABLE payments
  MODIFY COLUMN status ENUM('AUTHORIZED','CAPTURED','REFUNDED','FAILED') NOT NULL,
  ADD UNIQUE KEY uq_payments_order (order_id);
