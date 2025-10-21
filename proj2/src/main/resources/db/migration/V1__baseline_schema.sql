-- ===================================================================
-- V1__baseline_schema.sql
-- BoozeBuddies: fresh schema aligned to current entities & use-cases
-- MySQL 8.0.43+ (ENGINE InnoDB, utf8mb4)
-- ===================================================================

-- ============= USERS ==================
CREATE TABLE users (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(120)      NOT NULL,
  email         VARCHAR(190)      NOT NULL UNIQUE,
  password_hash VARCHAR(255)      NOT NULL,
  phone         VARCHAR(40)       NULL,
  date_of_birth DATE              NULL,
  age_verified  BOOLEAN           NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP         NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT      NOT NULL,
  role    VARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= MERCHANTS ==============
CREATE TABLE merchants (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  name         VARCHAR(200) NOT NULL,
  status       ENUM('OPEN','CLOSED','PAUSED') NOT NULL DEFAULT 'OPEN',
  line1        VARCHAR(255) NULL,
  city         VARCHAR(120) NULL,
  state        VARCHAR(120) NULL,
  postal_code  VARCHAR(20)  NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP    NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= CATEGORIES ==============
CREATE TABLE categories (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(160) NOT NULL,
  merchant_id BIGINT       NULL,
  CONSTRAINT fk_categories_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= PRODUCTS =================
CREATE TABLE products (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  name           VARCHAR(200) NOT NULL,
  description    TEXT         NULL,
  price          DECIMAL(10,2) NOT NULL,
  category_id    BIGINT       NULL,
  merchant_id    BIGINT       NOT NULL,
  is_alcohol     BOOLEAN      NOT NULL DEFAULT FALSE,
  alcohol_content DOUBLE      NULL,
  stock_quantity INT          NULL DEFAULT 0,
  available      BOOLEAN      NOT NULL DEFAULT TRUE,
  image_url      VARCHAR(512) NULL,
  version        BIGINT       NULL,
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  CONSTRAINT fk_products_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_products_available ON products(available);
CREATE INDEX idx_products_merchant ON products(merchant_id);

-- ============= DRIVERS ==================
CREATE TABLE drivers (
  id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id                BIGINT NULL,
  name                   VARCHAR(120) NULL,
  is_available           BOOLEAN NOT NULL DEFAULT FALSE,
  rating                 DECIMAL(3,2) NULL,
  certification_status   ENUM('PENDING','VERIFIED','EXPIRED','REVOKED') NOT NULL DEFAULT 'PENDING',
  vehicle_make           VARCHAR(80)  NULL,
  vehicle_model          VARCHAR(80)  NULL,
  vehicle_plate          VARCHAR(40)  NULL,
  last_lat               DOUBLE NULL,
  last_lng               DOUBLE NULL,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_drivers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_drivers_available ON drivers(is_available);

-- ============= ORDERS ===================
CREATE TABLE orders (
  id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id                  BIGINT       NOT NULL,
  merchant_id              BIGINT       NOT NULL,
  driver_id                BIGINT       NULL,
  status                   ENUM('PENDING','CONFIRMED','PREPARING','READY_FOR_PICKUP','PICKED_UP','IN_TRANSIT','DELIVERED','COMPLETED','CANCELLED','FAILED') NOT NULL,
  order_type               ENUM('DELIVERY','PICKUP') NOT NULL DEFAULT 'DELIVERY',
  total_amount             DECIMAL(10,2) NULL,
  delivery_address         VARCHAR(512)  NOT NULL,
  special_instructions     VARCHAR(512)  NULL,
  age_verified             BOOLEAN       NOT NULL DEFAULT FALSE,
  promo_code               VARCHAR(64)   NULL,
  subtotal                 DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tax                      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tip                      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  fees                     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  created_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP     NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  estimated_delivery_time  TIMESTAMP     NULL,
  CONSTRAINT fk_orders_user     FOREIGN KEY (user_id)     REFERENCES users(id),
  CONSTRAINT fk_orders_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id),
  CONSTRAINT fk_orders_driver   FOREIGN KEY (driver_id)   REFERENCES drivers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at);
CREATE INDEX idx_orders_merchant_status ON orders(merchant_id, status);

-- ============= ORDER ITEMS ==============
CREATE TABLE order_items (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id    BIGINT NOT NULL,
  product_id  BIGINT NULL,        -- keep NULL to allow deleting products without breaking history
  line_no     INT NOT NULL,       -- preserves the order of lines in the cart
  name        VARCHAR(200) NOT NULL,   -- snapshot
  unit_price  DECIMAL(10,2) NOT NULL,  -- snapshot
  quantity    INT NOT NULL,
  subtotal    DECIMAL(10,2) NOT NULL,  -- snapshot = unit_price * quantity
  CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)  REFERENCES orders(id)   ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);


-- ============= DELIVERIES ===============
CREATE TABLE deliveries (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id        BIGINT NOT NULL,
  driver_id       BIGINT NULL,
  status          ENUM('UNASSIGNED','ASSIGNED','PICKED_UP','IN_TRANSIT','DELIVERED','FAILED','CANCELLED') NOT NULL DEFAULT 'UNASSIGNED',
  assigned_at     TIMESTAMP NULL,
  picked_up_at    TIMESTAMP NULL,
  delivered_at    TIMESTAMP NULL,
  eta_seconds     INT NULL,
  handoff_code    VARCHAR(16)  NULL,
  proof_photo_url VARCHAR(512) NULL,
  CONSTRAINT fk_deliveries_order  FOREIGN KEY (order_id) REFERENCES orders(id)   ON DELETE CASCADE,
  CONSTRAINT fk_deliveries_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
  UNIQUE KEY uq_deliveries_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_deliveries_status ON deliveries(status);

-- ============= PAYMENTS =================
CREATE TABLE payments (
  id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id               BIGINT NOT NULL,
  status                 ENUM('AUTHORIZED','CAPTURED','REFUNDED','FAILED') NOT NULL,
  provider               VARCHAR(80)  NULL,
  auth_id                VARCHAR(120) NULL,
  capture_id             VARCHAR(120) NULL,
  amount_authorized      DECIMAL(10,2) NULL,
  amount_captured        DECIMAL(10,2) NULL,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  UNIQUE KEY uq_payments_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= RATINGS (minimal) =========
CREATE TABLE ratings (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT NOT NULL,
  product_id BIGINT NULL,
  merchant_id BIGINT NULL,
  order_id   BIGINT NULL,
  score      INT NOT NULL,
  comment    VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ratings_user     FOREIGN KEY (user_id)    REFERENCES users(id)      ON DELETE CASCADE,
  CONSTRAINT fk_ratings_product  FOREIGN KEY (product_id) REFERENCES products(id)   ON DELETE SET NULL,
  CONSTRAINT fk_ratings_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE SET NULL,
  CONSTRAINT fk_ratings_order    FOREIGN KEY (order_id)   REFERENCES orders(id)     ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;