-- ===================================================================
-- V1__baseline_schema.sql
-- BoozeBuddies: fresh schema aligned to current entities & use-cases
-- MySQL 8.0.43+ (ENGINE InnoDB, utf8mb4)
-- ===================================================================

-- ============= MERCHANTS ==============
CREATE TABLE merchants (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(200) NOT NULL,
  description   TEXT NULL,
  address       VARCHAR(512) NOT NULL,
  phone         VARCHAR(40)  NULL,
  email         VARCHAR(190) NULL,
  cuisine_type  VARCHAR(100) NULL,
  opening_time  TIME NULL,
  closing_time  TIME NULL,
  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
  rating        DOUBLE NOT NULL DEFAULT 0.0,
  total_ratings INT NOT NULL DEFAULT 0,
  image_url     VARCHAR(512) NULL,
  latitude      DOUBLE NULL,
  longitude     DOUBLE NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_merchants_active ON merchants(is_active);
CREATE INDEX idx_merchants_location ON merchants(latitude, longitude);

-- ============= USERS (After V2 Migration) ==================
CREATE TABLE users (
  id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
  name                   VARCHAR(120)      NOT NULL,
  email                  VARCHAR(190)      NOT NULL UNIQUE,
  password_hash          VARCHAR(255)      NOT NULL,
  phone                  VARCHAR(40)       NULL,
  date_of_birth          DATE              NULL,
  age_verified           BOOLEAN           NOT NULL DEFAULT FALSE,
  latitude               DOUBLE            NULL,
  longitude              DOUBLE            NULL,
  merchant_id            BIGINT            NULL,
  is_active              BOOLEAN           NOT NULL DEFAULT TRUE,
  is_email_verified      BOOLEAN           NOT NULL DEFAULT FALSE,
  last_login_at          TIMESTAMP         NULL,
  refresh_token          VARCHAR(512)      NULL,
  refresh_token_expiry   TIMESTAMP         NULL,
  created_at             TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP         NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE SET NULL,
  INDEX idx_users_refresh_token (refresh_token),
  INDEX idx_users_active (is_active),
  INDEX idx_users_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT      NOT NULL,
  role    VARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= CATEGORIES ==============
CREATE TABLE categories (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(160) NOT NULL UNIQUE,
  description VARCHAR(500) NULL,
  image_url   VARCHAR(512) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= PRODUCTS =================
CREATE TABLE products (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(200) NOT NULL,
  description     TEXT         NULL,
  price           DECIMAL(10,2) NOT NULL,
  category_id     BIGINT       NULL,
  merchant_id     BIGINT       NOT NULL,
  is_alcohol      BOOLEAN      NOT NULL DEFAULT FALSE,
  alcohol_content DOUBLE       NULL,
  volume_ml       INT          NULL,
  available       BOOLEAN      NOT NULL DEFAULT TRUE,
  image_url       VARCHAR(512) NULL,
  CONSTRAINT fk_products_category 
      FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
  CONSTRAINT fk_products_merchant 
      FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_products_available ON products(available);
CREATE INDEX idx_products_merchant ON products(merchant_id);
CREATE INDEX idx_products_category ON products(category_id);

-- ============= DRIVERS ==================
CREATE TABLE drivers (
  id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id                BIGINT NOT NULL UNIQUE,
  name                   VARCHAR(120) NOT NULL,
  email                  VARCHAR(190) NOT NULL UNIQUE,
  phone                  VARCHAR(40)  NULL,
  vehicle_type           VARCHAR(80)  NULL,
  license_plate          VARCHAR(40)  NULL,
  is_available           BOOLEAN NOT NULL DEFAULT TRUE,
  current_latitude       DOUBLE NULL,
  current_longitude      DOUBLE NULL,
  rating                 DOUBLE NOT NULL DEFAULT 0.0,
  total_deliveries       INT NOT NULL DEFAULT 0,
  certification_status   VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  certification_number   VARCHAR(100) NULL,
  certification_type     VARCHAR(100) NULL,
  issue_date             DATE NULL,
  expiry_date            DATE NULL,
  valid                  BOOLEAN NOT NULL DEFAULT TRUE,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_drivers_available ON drivers(is_available);
CREATE INDEX idx_drivers_user_id ON drivers(user_id);
CREATE INDEX idx_drivers_certification ON drivers(certification_status);

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
  id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id                BIGINT NOT NULL,
  driver_id               BIGINT NULL,
  status                  VARCHAR(50) NOT NULL,
  delivery_address        VARCHAR(512) NOT NULL,
  delivery_latitude       DOUBLE NULL,
  delivery_longitude      DOUBLE NULL,
  pickup_time             TIMESTAMP NULL,
  cancellation_reason     VARCHAR(500) NULL,
  delivered_time          TIMESTAMP NULL,
  estimated_delivery_time TIMESTAMP NULL,
  
  age_verified            BOOLEAN DEFAULT FALSE,
  id_type                 VARCHAR(50) NULL,
  id_number               VARCHAR(10) NULL,
  age_verified_at         TIMESTAMP NULL,
  

  current_latitude        DOUBLE NULL,
  current_longitude       DOUBLE NULL,
  last_location_update    TIMESTAMP NULL,
  
  created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  CONSTRAINT fk_deliveries_order  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_deliveries_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
  UNIQUE KEY uq_deliveries_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_age_verified ON deliveries(age_verified);

-- ============= PAYMENTS =================
CREATE TABLE payments (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id        BIGINT NOT NULL,
  user_id         BIGINT NOT NULL,
  amount          DECIMAL(10,2) NOT NULL,
  status          VARCHAR(50) NOT NULL,
  payment_method  VARCHAR(100) NULL,
  transaction_id  VARCHAR(200) NULL,
  failure_reason  VARCHAR(500) NULL,
  refund_reason   VARCHAR(500) NULL,
  payment_date    TIMESTAMP NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_payments_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============= RATINGS =========
CREATE TABLE ratings (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  target_type VARCHAR(50) NOT NULL,
  target_id   BIGINT NOT NULL,
  rating      INT NOT NULL,
  review      TEXT NULL,
  merchant_id BIGINT NULL,
  driver_id   BIGINT NULL,
  product_id  BIGINT NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ratings_user     FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
  CONSTRAINT fk_ratings_product  FOREIGN KEY (product_id)  REFERENCES products(id)   ON DELETE SET NULL,
  CONSTRAINT fk_ratings_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)  ON DELETE SET NULL,
  CONSTRAINT fk_ratings_driver   FOREIGN KEY (driver_id)   REFERENCES drivers(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;