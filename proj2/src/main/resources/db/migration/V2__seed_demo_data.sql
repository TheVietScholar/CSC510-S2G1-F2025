-- Seed demo merchants, categories, and products
-- Ensure alignment with V1__baseline_schema.sql and entity non-null constraints

-- ============= MERCHANTS ==============
INSERT INTO merchants (name, description, address, phone, email, cuisine_type, opening_time, closing_time, is_active, rating, total_ratings, image_url)
VALUES
  ('Red Dragon Brewery', 'Craft brewery with rotating taps', '123 Red St, Raleigh, NC', '919-555-0101', 'reddragon@example.com', 'Brewery', '10:00:00', '23:00:00', TRUE, 4.5, 120, 'https://images.unsplash.com/photo-1541558619105-5cf502ee0d52'),
  ('Black Label Bar', 'Cocktail specials and curated spirits', '456 Black Ave, Raleigh, NC', '919-555-0102', 'blacklabel@example.com', 'Cocktail Bar', '16:00:00', '02:00:00', TRUE, 4.2, 89, 'https://images.unsplash.com/photo-1514362545857-3bc16c4c76a6'),
  ('Crimson Tap House', 'Beer bar with local and international picks', '789 Crimson Blvd, Raleigh, NC', '919-555-0103', 'crimsontap@example.com', 'Beer Bar', '12:00:00', '00:00:00', TRUE, 4.7, 210, 'https://images.unsplash.com/photo-1509223197845-458d87318791'),
  ('Scarlet Wine Bar', 'Fine wines and small plates', '101 Scarlet Ln, Raleigh, NC', '919-555-0104', 'scarletwine@example.com', 'Wine Bar', '14:00:00', '23:59:59', TRUE, 4.4, 142, 'https://images.unsplash.com/photo-1514362545857-3bc16c4c76a6'),
  ('Burgundy Pub', 'Sports bar with hearty fare', '202 Burgundy Rd, Raleigh, NC', '919-555-0105', 'burgundy@example.com', 'Sports Bar', '11:00:00', '01:00:00', TRUE, 4.1, 98, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836');

-- ============= CATEGORIES ==============
-- Associate categories with merchants by name lookup to avoid hardcoding IDs
INSERT INTO categories (name, description, image_url)
VALUES
  ('Beer', 'All beer styles', NULL),
  ('Snacks', 'Bar snacks', NULL),
  ('Cocktails', 'House cocktails', NULL),
  ('Spirits', 'Premium spirits', NULL),
  ('Lagers & Ales', 'Taps and bottles', NULL),
  ('Wine', 'Red, white and rosé', NULL),
  ('Small Plates', 'Shared bites', NULL),
  ('Pub Fare', 'Comfort food', NULL),
  ('Drafts', 'On tap', NULL);

-- ============= PRODUCTS ==============
-- Red Dragon Brewery
INSERT INTO products (name, description, price, category_id, merchant_id, is_alcohol, alcohol_content, available, image_url)
VALUES
  ('Craft IPA', 'Hoppy IPA with citrus notes', 8.99,
   (SELECT id FROM categories WHERE name='Beer' AND merchant_id=(SELECT id FROM merchants WHERE name='Red Dragon Brewery')),
   (SELECT id FROM merchants WHERE name='Red Dragon Brewery'), TRUE, 6.5, TRUE, 'https://images.unsplash.com/photo-1541558619105-5cf502ee0d52'),
  ('Imperial Stout', 'Rich dark beer with coffee notes', 9.99,
   (SELECT id FROM categories WHERE name='Beer' AND merchant_id=(SELECT id FROM merchants WHERE name='Red Dragon Brewery')),
   (SELECT id FROM merchants WHERE name='Red Dragon Brewery'), TRUE, 8.0, TRUE, 'https://images.unsplash.com/photo-1509223197845-458d87318791'),
  ('Pale Ale', 'Smooth and balanced ale', 7.99,
   (SELECT id FROM categories WHERE name='Beer' AND merchant_id=(SELECT id FROM merchants WHERE name='Red Dragon Brewery')),
   (SELECT id FROM merchants WHERE name='Red Dragon Brewery'), TRUE, 5.2, TRUE, 'https://images.unsplash.com/photo-1543255006-5a1a58236a6a'),
  ('Pretzel Bites', 'Warm pretzel with mustard', 5.49,
   (SELECT id FROM categories WHERE name='Snacks' AND merchant_id=(SELECT id FROM merchants WHERE name='Red Dragon Brewery')),
   (SELECT id FROM merchants WHERE name='Red Dragon Brewery'), FALSE, NULL, TRUE, 'https://images.unsplash.com/photo-1546549039-49d79ae77f88');

-- Black Label Bar
INSERT INTO products (name, description, price, category_id, merchant_id, is_alcohol, alcohol_content, available, image_url)
VALUES
  ('Whiskey Sour', 'Classic cocktail with bourbon', 12.99,
   (SELECT id FROM categories WHERE name='Cocktails' AND merchant_id=(SELECT id FROM merchants WHERE name='Black Label Bar')),
   (SELECT id FROM merchants WHERE name='Black Label Bar'), TRUE, 15.0, TRUE, 'https://images.unsplash.com/photo-1541976076758-347942db1978'),
  ('Negroni', 'Gin, Campari, vermouth', 11.49,
   (SELECT id FROM categories WHERE name='Cocktails' AND merchant_id=(SELECT id FROM merchants WHERE name='Black Label Bar')),
   (SELECT id FROM merchants WHERE name='Black Label Bar'), TRUE, 18.0, TRUE, 'https://images.unsplash.com/photo-1571116172975-3dcd39c9b7b4'),
  ('Single Malt', '12-year single malt scotch', 15.99,
   (SELECT id FROM categories WHERE name='Spirits' AND merchant_id=(SELECT id FROM merchants WHERE name='Black Label Bar')),
   (SELECT id FROM merchants WHERE name='Black Label Bar'), TRUE, 40.0, TRUE, 'https://images.unsplash.com/photo-1554825203-68321ddde262');

-- Crimson Tap House
INSERT INTO products (name, description, price, category_id, merchant_id, is_alcohol, alcohol_content, available, image_url)
VALUES
  ('Amber Lager', 'Crisp lager with caramel notes', 6.99,
   (SELECT id FROM categories WHERE name='Lagers & Ales' AND merchant_id=(SELECT id FROM merchants WHERE name='Crimson Tap House')),
   (SELECT id FROM merchants WHERE name='Crimson Tap House'), TRUE, 5.0, TRUE, 'https://images.unsplash.com/photo-1516455590571-18256e5bb9ff'),
  ('Hefeweizen', 'Wheat beer with banana-clove', 7.49,
   (SELECT id FROM categories WHERE name='Lagers & Ales' AND merchant_id=(SELECT id FROM merchants WHERE name='Crimson Tap House')),
   (SELECT id FROM merchants WHERE name='Crimson Tap House'), TRUE, 5.4, TRUE, 'https://images.unsplash.com/photo-1517093602195-b40af9688b53');

-- Scarlet Wine Bar
INSERT INTO products (name, description, price, category_id, merchant_id, is_alcohol, alcohol_content, available, image_url)
VALUES
  ('Pinot Noir', 'Light-bodied red wine', 13.99,
   (SELECT id FROM categories WHERE name='Wine' AND merchant_id=(SELECT id FROM merchants WHERE name='Scarlet Wine Bar')),
   (SELECT id FROM merchants WHERE name='Scarlet Wine Bar'), TRUE, 12.5, TRUE, 'https://images.unsplash.com/photo-1514362545857-3bc16c4c76a6'),
  ('Chardonnay', 'Buttery and oaked', 12.49,
   (SELECT id FROM categories WHERE name='Wine' AND merchant_id=(SELECT id FROM merchants WHERE name='Scarlet Wine Bar')),
   (SELECT id FROM merchants WHERE name='Scarlet Wine Bar'), TRUE, 13.0, TRUE, 'https://images.unsplash.com/photo-1510627498534-cf7e9002facc'),
  ('Bruschetta', 'Tomato basil on toasted baguette', 6.99,
   (SELECT id FROM categories WHERE name='Small Plates' AND merchant_id=(SELECT id FROM merchants WHERE name='Scarlet Wine Bar')),
   (SELECT id FROM merchants WHERE name='Scarlet Wine Bar'), FALSE, NULL, TRUE, 'https://images.unsplash.com/photo-1523986371872-9d3ba2e2f642');

-- Burgundy Pub
INSERT INTO products (name, description, price, category_id, merchant_id, is_alcohol, alcohol_content, available, image_url)
VALUES
  ('House Lager', 'Crisp pub lager', 5.99,
   (SELECT id FROM categories WHERE name='Drafts' AND merchant_id=(SELECT id FROM merchants WHERE name='Burgundy Pub')),
   (SELECT id FROM merchants WHERE name='Burgundy Pub'), TRUE, 4.8, TRUE, 'https://images.unsplash.com/photo-1441986300917-64674bd600d8'),
  ('Wings', 'Buffalo wings, 8pc', 9.99,
   (SELECT id FROM categories WHERE name='Pub Fare' AND merchant_id=(SELECT id FROM merchants WHERE name='Burgundy Pub')),
   (SELECT id FROM merchants WHERE name='Burgundy Pub'), FALSE, NULL, TRUE, 'https://images.unsplash.com/photo-1550547660-d9450f859349'),
  ('Nachos', 'Loaded nachos with cheese and jalapeños', 8.49,
   (SELECT id FROM categories WHERE name='Pub Fare' AND merchant_id=(SELECT id FROM merchants WHERE name='Burgundy Pub')),
   (SELECT id FROM merchants WHERE name='Burgundy Pub'), FALSE, NULL, TRUE, 'https://images.unsplash.com/photo-1550547660-d9450f859349');


