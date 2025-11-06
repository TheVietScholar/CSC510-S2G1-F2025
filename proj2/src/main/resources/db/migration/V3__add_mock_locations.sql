-- ===================================================================
-- V3__add_mock_locations.sql
-- Add mock latitude/longitude coordinates for merchants and users
-- Coordinates are centered around Raleigh, NC area for testing distance-based features
-- ===================================================================

-- ============= UPDATE MERCHANTS WITH LOCATIONS ==============
-- Set coordinates for merchants in Raleigh, NC area
-- Using realistic coordinates spread around downtown Raleigh

UPDATE merchants 
SET latitude = 35.7789, longitude = -78.6392
WHERE name = 'Red Dragon Brewery';

UPDATE merchants 
SET latitude = 35.7801, longitude = -78.6375
WHERE name = 'Black Label Bar';

UPDATE merchants 
SET latitude = 35.7792, longitude = -78.6405
WHERE name = 'Crimson Tap House';

UPDATE merchants 
SET latitude = 35.7775, longitude = -78.6388
WHERE name = 'Scarlet Wine Bar';

UPDATE merchants 
SET latitude = 35.7810, longitude = -78.6362
WHERE name = 'Burgundy Pub';

-- ============= UPDATE TEST USERS WITH LOCATIONS ==============
-- Set coordinates for test users (created by DataInitializer)
-- Place users in different locations around Raleigh to test distance sorting

-- User (user@boozebuddies.com) - slightly north of downtown
UPDATE users 
SET latitude = 35.7830, longitude = -78.6380
WHERE email = 'user@boozebuddies.com';

-- Admin (admin@boozebuddies.com) - central downtown location
UPDATE users 
SET latitude = 35.7796, longitude = -78.6382
WHERE email = 'admin@boozebuddies.com';

-- Merchant Admin (merchant1@boozebuddies.com) - near merchant location
UPDATE users 
SET latitude = 35.7789, longitude = -78.6392
WHERE email = 'merchant1@boozebuddies.com';

-- ============= SET DEFAULT LOCATION FOR EXISTING USERS WITHOUT LOCATION ==============
-- Set a default location for any users that don't have coordinates yet
-- This ensures newly registered users can also test the distance feature
-- Default to downtown Raleigh area

UPDATE users 
SET latitude = 35.7796, longitude = -78.6382
WHERE latitude IS NULL OR longitude IS NULL;

-- Note: For production, users should update their location through the UserSettings
-- This migration provides mock/test data for development and testing purposes

