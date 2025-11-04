# API Documentation

## Overview

The BoozeBuddies API is a RESTful API built with Spring Boot that provides endpoints for managing users, products, orders, deliveries, and more. All endpoints return JSON responses wrapped in an `ApiResponse` object and use standard HTTP status codes.

**Base URL**: `http://localhost:8080/api`

## Table of Contents

- [Response Format](#response-format)
- [Authentication](#authentication)
- [User Management](#user-management)
- [Category Management](#category-management)
- [Product Management](#product-management)
- [Merchant Management](#merchant-management)
- [Order Management](#order-management)
- [Delivery Management](#delivery-management)
- [Driver Management](#driver-management)
- [Payment Management](#payment-management)
- [Error Handling](#error-handling)

## Response Format

All API responses (except auth endpoints) are wrapped in an `ApiResponse` object:

**Success Response**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Authentication

### Register a New User

**Endpoint**: `POST /api/auth/register`

**Description**: Create a new user account.

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecureP@ssw0rd",
  "phone": "+1-555-123-4567",
  "dateOfBirth": "1990-05-15"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Registration successful"
}
```

### Login

**Endpoint**: `POST /api/auth/login`

**Description**: Authenticate a user and receive JWT tokens.

**Request Body**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecureP@ssw0rd"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

### Refresh Token

**Endpoint**: `POST /api/auth/refresh`

**Description**: Get a new access token using a refresh token.

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Token refreshed"
}
```

### Logout

**Endpoint**: `POST /api/auth/logout/{userId}`

**Description**: Invalidate the current refresh token.

**Response** (204 No Content)

---

## User Management

### Get Current User Profile

**Endpoint**: `GET /api/users/me`

**Authorization**: Required (any authenticated user)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your profile retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-123-4567",
    "dateOfBirth": "1990-05-15",
    "ageVerified": true,
    "isActive": true,
    "roles": ["USER"]
  }
}
```

### Get User by ID

**Endpoint**: `GET /api/users/{id}`

**Authorization**: Users can view their own profile, admins can view any profile

**Response** (200 OK):
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "roles": ["USER"],
    "isActive": true
  }
}
```

### Get All Users

**Endpoint**: `GET /api/users`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "roles": ["USER"]
    }
  ]
}
```

### Update User

**Endpoint**: `PUT /api/users/{id}`

**Authorization**: Users can update their own profile, admins can update any profile

**Request Body**:
```json
{
  "name": "John Smith",
  "phone": "+1-555-999-8888"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "name": "John Smith",
    "phone": "+1-555-999-8888"
  }
}
```

### Verify User Age

**Endpoint**: `POST /api/users/{id}/verify-age`

**Authorization**: Users can verify their own age, admins can verify any user

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Age verification successful",
  "data": {
    "id": 1,
    "ageVerified": true
  }
}
```

### Delete User

**Endpoint**: `DELETE /api/users/{id}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

### Assign Role to User

**Endpoint**: `POST /api/users/{id}/roles`

**Authorization**: Admin only

**Request Body**:
```json
{
  "role": "MERCHANT_ADMIN",
  "merchantId": 5
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Role assigned successfully",
  "data": {
    "id": 1,
    "roles": ["USER", "MERCHANT_ADMIN"],
    "merchantId": 5
  }
}
```

### Remove Role from User

**Endpoint**: `DELETE /api/users/{id}/roles/{role}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Role removed successfully",
  "data": {
    "id": 1,
    "roles": ["USER"]
  }
}
```

### Set User Roles

**Endpoint**: `PUT /api/users/{id}/roles`

**Authorization**: Admin only

**Request Body**:
```json
{
  "roles": ["USER", "DRIVER"]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Roles updated successfully",
  "data": {
    "id": 1,
    "roles": ["USER", "DRIVER"]
  }
}
```

### Assign Merchant to User

**Endpoint**: `POST /api/users/{id}/merchant`

**Authorization**: Admin only

**Request Body**:
```json
{
  "merchantId": 5
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant assigned successfully",
  "data": {
    "id": 1,
    "merchantId": 5
  }
}
```

---

## Category Management

### Get All Categories

**Endpoint**: `GET /api/categories`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Beer",
      "description": "Alcoholic beverages - Beer",
      "imageUrl": "https://example.com/images/beer.jpg"
    },
    {
      "id": 2,
      "name": "Spirits",
      "description": "Hard liquor and spirits",
      "imageUrl": "https://example.com/images/spirits.jpg"
    }
  ]
}
```

### Get Category by ID

**Endpoint**: `GET /api/categories/{id}`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": 1,
    "name": "Beer",
    "description": "Alcoholic beverages - Beer",
    "imageUrl": "https://example.com/images/beer.jpg"
  }
}
```

### Create Category

**Endpoint**: `POST /api/categories`

**Authorization**: Admin only

**Request Body**:
```json
{
  "name": "Wine",
  "description": "Red, white, and sparkling wines",
  "imageUrl": "https://example.com/images/wine.jpg"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    "id": 3,
    "name": "Wine",
    "description": "Red, white, and sparkling wines"
  }
}
```

---

## Product Management

### Get All Available Products

**Endpoint**: `GET /api/products`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Available products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Craft IPA 6-Pack",
      "description": "Local craft brewery IPA with citrus notes",
      "price": 14.99,
      "categoryId": 1,
      "merchantId": 5,
      "isAlcohol": true,
      "alcoholContent": 6.5,
      "stockQuantity": 50,
      "available": true,
      "imageUrl": "https://example.com/images/craft-ipa.jpg"
    }
  ]
}
```

### Get Product by ID

**Endpoint**: `GET /api/products/{id}`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Craft IPA 6-Pack",
    "description": "Local craft brewery IPA with citrus notes",
    "price": 14.99,
    "categoryId": 1,
    "merchantId": 5,
    "isAlcohol": true,
    "alcoholContent": 6.5,
    "stockQuantity": 50,
    "available": true,
    "imageUrl": "https://example.com/images/craft-ipa.jpg"
  }
}
```

### Search Products

**Endpoint**: `GET /api/products/search?keyword={keyword}`

**Authorization**: Public (no authentication required)

**Query Parameters**:
- `keyword` (required): Search term

**Example**: `GET /api/products/search?keyword=IPA`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Products found successfully",
  "data": [
    {
      "id": 1,
      "name": "Craft IPA 6-Pack",
      "price": 14.99,
      "available": true
    }
  ]
}
```

### Get Products by Merchant

**Endpoint**: `GET /api/products/merchant/{merchantId}`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Craft IPA 6-Pack",
      "price": 14.99,
      "merchantId": 5
    }
  ]
}
```

### Check Product Availability

**Endpoint**: `GET /api/products/{id}/available`

**Authorization**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Product availability checked successfully",
  "data": true
}
```

### Get All Products (Including Unavailable)

**Endpoint**: `GET /api/products/all`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "All products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Craft IPA 6-Pack",
      "available": true
    },
    {
      "id": 2,
      "name": "Out of Stock Beer",
      "available": false
    }
  ]
}
```

### Get All Merchant Products (Including Unavailable)

**Endpoint**: `GET /api/products/merchant/{merchantId}/all`

**Authorization**: Admin or Merchant Admin (must own merchant)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Craft IPA 6-Pack",
      "merchantId": 5,
      "available": true
    }
  ]
}
```

### Add Product

**Endpoint**: `POST /api/products`

**Authorization**: Admin or Merchant Admin (can only add for own merchant)

**Request Body**:
```json
{
  "name": "Premium Whiskey",
  "description": "Aged 12 years, smooth finish",
  "price": 49.99,
  "categoryId": 3,
  "merchantId": 5,
  "isAlcohol": true,
  "alcoholContent": 40.0,
  "stockQuantity": 25,
  "available": true,
  "imageUrl": "https://example.com/images/whiskey.jpg"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Product added successfully",
  "data": {
    "id": 101,
    "name": "Premium Whiskey",
    "price": 49.99
  }
}
```

### Update Product

**Endpoint**: `PUT /api/products/{id}`

**Authorization**: Admin or Merchant Admin (can only update own merchant's products)

**Request Body**:
```json
{
  "price": 44.99,
  "stockQuantity": 30,
  "available": true
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": {
    "id": 101,
    "price": 44.99,
    "stockQuantity": 30
  }
}
```

### Delete Product

**Endpoint**: `DELETE /api/products/{id}`

**Authorization**: Admin or Merchant Admin (can only delete own merchant's products)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null
}
```

---

## Merchant Management

### Register Merchant

**Endpoint**: `POST /api/merchants/register`

**Authorization**: Admin only

**Request Body**:
```json
{
  "name": "Local Liquor Store",
  "description": "Your neighborhood beverage destination",
  "address": "123 Main St, Raleigh, NC 27601",
  "phone": "+1-555-111-2222",
  "email": "info@localliquor.com",
  "cuisineType": "Beverage",
  "openingTime": "10:00:00",
  "closingTime": "22:00:00"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Merchant registered successfully",
  "data": {
    "id": 5,
    "name": "Local Liquor Store",
    "isActive": false
  }
}
```

### Verify Merchant

**Endpoint**: `PUT /api/merchants/{id}/verify?verified={true|false}`

**Authorization**: Admin only

**Query Parameters**:
- `verified` (required): true or false

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant verified successfully",
  "data": {
    "id": 5,
    "name": "Local Liquor Store",
    "isActive": true
  }
}
```

### Get Merchant by ID

**Endpoint**: `GET /api/merchants/{id}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant retrieved successfully",
  "data": {
    "id": 5,
    "name": "Local Liquor Store",
    "address": "123 Main St, Raleigh, NC 27601",
    "phone": "+1-555-111-2222",
    "isActive": true,
    "rating": 4.7,
    "totalRatings": 156
  }
}
```

### Get Merchant by Name

**Endpoint**: `GET /api/merchants/name/{name}`

**Authorization**: Authenticated users

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant retrieved successfully",
  "data": {
    "id": 5,
    "name": "Local Liquor Store",
    "address": "123 Main St, Raleigh, NC 27601"
  }
}
```

### Get Merchants by Distance from User

**Endpoint**: `GET /api/merchants/by-distance`

**Authorization**: Authenticated users

**Description**: Returns merchants sorted by distance from authenticated user's location (requires user to have latitude/longitude set in profile)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchants sorted by distance from your location",
  "data": [
    {
      "id": 5,
      "name": "Local Liquor Store",
      "address": "123 Main St, Raleigh, NC 27601",
      "distance": 2.5
    }
  ]
}
```

### Get All Merchants

**Endpoint**: `GET /api/merchants`

**Authorization**: Authenticated users

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchants retrieved successfully",
  "data": [
    {
      "id": 5,
      "name": "Local Liquor Store",
      "isActive": true
    }
  ]
}
```

### Delete Merchant

**Endpoint**: `DELETE /api/merchants/{id}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant deleted successfully",
  "data": null
}
```

### Get Orders by Merchant

**Endpoint**: `GET /api/merchants/{id}/orders?page={page}&size={size}`

**Authorization**: Admin or Merchant Admin (must own merchant)

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Items per page (default: 10)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1001,
        "userId": 1,
        "status": "PENDING",
        "totalAmount": 36.07
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### Get My Merchant (Merchant Admin)

**Endpoint**: `GET /api/merchants/my-merchant`

**Authorization**: Merchant Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your merchant retrieved successfully",
  "data": {
    "id": 5,
    "name": "Local Liquor Store",
    "address": "123 Main St, Raleigh, NC 27601"
  }
}
```

### Get My Merchant Orders (Merchant Admin)

**Endpoint**: `GET /api/merchants/my-merchant/orders?page={page}&size={size}`

**Authorization**: Merchant Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your merchant orders retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1001,
        "status": "PENDING",
        "totalAmount": 36.07
      }
    ]
  }
}
```

---

## Order Management

### Create Order

**Endpoint**: `POST /api/orders`

**Authorization**: User only

**Request Body**:
```json
{
  "merchantId": 5,
  "orderType": "DELIVERY",
  "deliveryAddress": "456 Oak Ave, Raleigh, NC 27606",
  "specialInstructions": "Please ring doorbell",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "promoCode": "SAVE10"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1001,
    "userId": 1,
    "merchantId": 5,
    "status": "PENDING",
    "totalAmount": 36.07,
    "deliveryAddress": "456 Oak Ave, Raleigh, NC 27606"
  }
}
```

### Get Order by ID

**Endpoint**: `GET /api/orders/{orderId}`

**Authorization**: Users can view own orders, merchant admins can view orders for their merchant, drivers can view assigned orders, admins can view all

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1001,
    "userId": 1,
    "merchantId": 5,
    "driverId": 50,
    "status": "IN_TRANSIT",
    "totalAmount": 36.07,
    "items": [
      {
        "productId": 1,
        "name": "Craft IPA 6-Pack",
        "quantity": 2,
        "unitPrice": 14.99
      }
    ]
  }
}
```

### Get My Orders

**Endpoint**: `GET /api/orders/my-orders`

**Authorization**: User only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "status": "DELIVERED",
      "totalAmount": 36.07,
      "createdAt": "2025-11-03T13:00:00Z"
    }
  ]
}
```

### Get Orders by User ID

**Endpoint**: `GET /api/orders/user/{userId}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "User orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "status": "DELIVERED",
      "totalAmount": 36.07
    }
  ]
}
```

### Get All Orders

**Endpoint**: `GET /api/orders`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "All orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "userId": 1,
      "status": "DELIVERED"
    }
  ]
}
```

### Get My Merchant Orders

**Endpoint**: `GET /api/orders/merchant/my-orders`

**Authorization**: Merchant Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your merchant orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "status": "PENDING",
      "totalAmount": 36.07
    }
  ]
}
```

### Get Orders by Merchant

**Endpoint**: `GET /api/orders/merchant/{merchantId}`

**Authorization**: Admin or Merchant Admin (must own merchant)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Merchant orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "status": "PENDING",
      "totalAmount": 36.07
    }
  ]
}
```

### Get Driver's Assigned Orders

**Endpoint**: `GET /api/orders/driver/assigned`

**Authorization**: Driver only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your assigned orders retrieved successfully",
  "data": [
    {
      "id": 1001,
      "status": "IN_TRANSIT",
      "deliveryAddress": "456 Oak Ave, Raleigh, NC 27606"
    }
  ]
}
```

### Cancel Order

**Endpoint**: `POST /api/orders/{orderId}/cancel`

**Authorization**: User (can only cancel own orders)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order cancelled successfully",
  "data": {
    "id": 1001,
    "status": "CANCELLED"
  }
}
```

### Update Order Status

**Endpoint**: `PUT /api/orders/{orderId}/status?status={status}`

**Authorization**: Admin or Merchant Admin (if they own the merchant)

**Query Parameters**:
- `status` (required): New order status

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order status updated successfully",
  "data": {
    "id": 1001,
    "status": "PREPARING"
  }
}
```

---

## Delivery Management

### Assign Driver to Order

**Endpoint**: `POST /api/deliveries/assign?orderId={orderId}&driverId={driverId}`

**Authorization**: Admin only

**Query Parameters**:
- `orderId` (required): Order ID
- `driverId` (required): Driver ID

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Driver assigned successfully",
  "data": {
    "id": 2001,
    "orderId": 1001,
    "driverId": 50,
    "status": "ASSIGNED"
  }
}
```

### Get Active Deliveries

**Endpoint**: `GET /api/deliveries/active`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Active deliveries retrieved successfully",
  "data": [
    {
      "id": 2001,
      "orderId": 1001,
      "driverId": 50,
      "status": "IN_TRANSIT"
    }
  ]
}
```

### Get All Deliveries

**Endpoint**: `GET /api/deliveries`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "All deliveries retrieved successfully",
  "data": [
    {
      "id": 2001,
      "orderId": 1001,
      "status": "DELIVERED"
    }
  ]
}
```

### Get Deliveries by Driver

**Endpoint**: `GET /api/deliveries/driver/{driverId}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Driver deliveries retrieved successfully",
  "data": [
    {
      "id": 2001,
      "orderId": 1001,
      "status": "DELIVERED"
    }
  ]
}
```

### Get Delivery by ID

**Endpoint**: `GET /api/deliveries/{deliveryId}`

**Authorization**: Drivers can view own deliveries, admins can view all

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Delivery retrieved successfully",
  "data": {
    "id": 2001,
    "orderId": 1001,
    "driverId": 50,
    "status": "IN_TRANSIT",
    "deliveryAddress": "456 Oak Ave, Raleigh, NC 27606"
  }
}
```

### Get My Deliveries

**Endpoint**: `GET /api/deliveries/driver/my-deliveries`

**Authorization**: Driver only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your deliveries retrieved successfully",
  "data": [
    {
      "id": 2001,
      "orderId": 1001,
      "status": "IN_TRANSIT"
    }
  ]
}
```

### Update Delivery Status

**Endpoint**: `PUT /api/deliveries/{deliveryId}/status?status={status}`

**Authorization**: Driver (own deliveries) or Admin

**Query Parameters**:
- `status` (required): New delivery status (ENUM: ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Delivery status updated successfully",
  "data": {
    "id": 2001,
    "status": "IN_TRANSIT"
  }
}
```

### Mark as Picked Up

**Endpoint**: `POST /api/deliveries/{deliveryId}/pickup`

**Authorization**: Driver only (own deliveries)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order marked as picked up successfully",
  "data": {
    "id": 2001,
    "status": "PICKED_UP"
  }
}
```

### Mark as Delivered

**Endpoint**: `POST /api/deliveries/{deliveryId}/deliver`

**Authorization**: Driver only (own deliveries)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Order marked as delivered successfully",
  "data": {
    "id": 2001,
    "status": "DELIVERED"
  }
}
```

### Verify Customer Age

**Endpoint**: `POST /api/deliveries/{deliveryId}/verify-age`

**Authorization**: Driver only (own deliveries)

**Query Parameters**:
- `ageVerified` (required): true or false
- `idType` (optional): Type of ID used
- `idNumber` (optional): ID number (last 4 digits)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Customer age verified successfully",
  "data": {
    "id": 2001,
    "ageVerified": true
  }
}
```

### Cancel Delivery

**Endpoint**: `POST /api/deliveries/{deliveryId}/cancel?reason={reason}`

**Authorization**: Driver only (own deliveries)

**Query Parameters**:
- `reason` (required): Cancellation reason

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Delivery cancelled successfully",
  "data": {
    "id": 2001,
    "status": "CANCELLED",
    "cancellationReason": "Customer not available"
  }
}
```

### Update Delivery Location

**Endpoint**: `PUT /api/deliveries/{deliveryId}/location?latitude={lat}&longitude={lon}`

**Authorization**: Driver only (own deliveries)

**Query Parameters**:
- `latitude` (required): Current latitude
- `longitude` (required): Current longitude

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Location updated successfully",
  "data": null
}
```

---

## Driver Management

### Register Driver

**Endpoint**: `POST /api/drivers/register`

**Authorization**: Admin only

**Request Body**:
```json
{
  "name": "Jane Driver",
  "email": "jane.driver@example.com",
  "phone": "+1-555-777-8888",
  "vehicleType": "Sedan",
  "licensePlate": "ABC-1234",
  "certificationNumber": "CERT123456",
  "certificationType": "Alcohol Delivery",
  "issueDate": "2024-01-01",
  "expiryDate": "2026-01-01"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Driver registered successfully",
  "data": {
    "id": 50,
    "name": "Jane Driver",
    "certificationStatus": "PENDING"
  }
}
```

### Update Certification Status

**Endpoint**: `PUT /api/drivers/{driverId}/certification?status={status}`

**Authorization**: Admin only

**Query Parameters**:
- `status` (required): PENDING, APPROVED, REJECTED, EXPIRED

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Certification status updated successfully",
  "data": {
    "id": 50,
    "certificationStatus": "APPROVED"
  }
}
```

### Get Available Drivers

**Endpoint**: `GET /api/drivers/available`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Available drivers retrieved successfully",
  "data": [
    {
      "id": 50,
      "name": "Jane Driver",
      "isAvailable": true,
      "vehicleType": "Sedan"
    }
  ]
}
```

### Get Driver by ID

**Endpoint**: `GET /api/drivers/{driverId}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Driver retrieved successfully",
  "data": {
    "id": 50,
    "name": "Jane Driver",
    "email": "jane.driver@example.com",
    "isAvailable": true,
    "rating": 4.9,
    "totalDeliveries": 234
  }
}
```

### Get All Drivers

**Endpoint**: `GET /api/drivers`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "All drivers retrieved successfully",
  "data": [
    {
      "id": 50,
      "name": "Jane Driver",
      "isAvailable": true
    }
  ]
}
```

### Get My Driver Profile

**Endpoint**: `GET /api/drivers/my-profile`

**Authorization**: Driver only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your profile retrieved successfully",
  "data": {
    "id": 50,
    "name": "Jane Driver",
    "isAvailable": true,
    "rating": 4.9,
    "totalDeliveries": 234
  }
}
```

### Update My Availability

**Endpoint**: `PUT /api/drivers/my-profile/availability?available={true|false}`

**Authorization**: Driver only

**Query Parameters**:
- `available` (required): true or false

**Response** (200 OK):
```json
{
  "success": true,
  "message": "You are now available for deliveries",
  "data": {
    "id": 50,
    "isAvailable": true
  }
}
```

### Update My Location

**Endpoint**: `PUT /api/drivers/my-profile/location?latitude={lat}&longitude={lon}`

**Authorization**: Driver only

**Query Parameters**:
- `latitude` (required): Current latitude
- `longitude` (required): Current longitude

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Location updated successfully",
  "data": {
    "id": 50,
    "currentLatitude": 35.7796,
    "currentLongitude": -78.6382
  }
}
```

---

## Payment Management

### Process Payment

**Endpoint**: `POST /api/payments/process?orderId={orderId}&paymentMethod={method}`

**Authorization**: User only (own orders)

**Query Parameters**:
- `orderId` (required): Order ID
- `paymentMethod` (required): Payment method (e.g., "CREDIT_CARD", "DEBIT_CARD")

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": 3001,
    "orderId": 1001,
    "amount": 36.07,
    "status": "COMPLETED",
    "paymentMethod": "CREDIT_CARD"
  }
}
```

### Refund Payment

**Endpoint**: `POST /api/payments/refund?orderId={orderId}&reason={reason}`

**Authorization**: Admin only

**Query Parameters**:
- `orderId` (required): Order ID
- `reason` (required): Refund reason

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Refund processed successfully",
  "data": {
    "id": 3001,
    "status": "REFUNDED",
    "refundReason": "Order cancelled"
  }
}
```

### Get My Payments

**Endpoint**: `GET /api/payments/my-payments?page={page}&size={size}`

**Authorization**: User only

**Query Parameters**:
- `page` (optional): Page number
- `size` (optional): Items per page

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Your payments retrieved successfully",
  "data": {
    "content": [
      {
        "id": 3001,
        "orderId": 1001,
        "amount": 36.07,
        "status": "COMPLETED"
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

### Get Payments by User

**Endpoint**: `GET /api/payments/user/{userId}?page={page}&size={size}`

**Authorization**: Users can view own payments, admins can view any

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Payments retrieved successfully",
  "data": {
    "content": [
      {
        "id": 3001,
        "amount": 36.07,
        "status": "COMPLETED"
      }
    ]
  }
}
```

### Get Payment by Order

**Endpoint**: `GET /api/payments/order/{orderId}`

**Authorization**: Users can view own order payments, admins can view any

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "id": 3001,
    "orderId": 1001,
    "amount": 36.07,
    "status": "COMPLETED",
    "paymentDate": "2025-11-03T13:05:00Z"
  }
}
```

### Calculate Revenue

**Endpoint**: `GET /api/payments/revenue?startDate={start}&endDate={end}`

**Authorization**: Admin only

**Query Parameters**:
- `startDate` (required): Start date (ISO format: 2025-01-01T00:00:00Z)
- `endDate` (required): End date (ISO format)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Revenue calculated successfully",
  "data": 15432.50
}
```

### Get All Payments

**Endpoint**: `GET /api/payments?page={page}&size={size}`

**Authorization**: Admin only

**Response** (200 OK):
```json
{
  "success": true,
  "message": "All payments retrieved successfully",
  "data": {
    "content": [
      {
        "id": 3001,
        "orderId": 1001,
        "amount": 36.07,
        "status": "COMPLETED"
      }
    ]
  }
}
```

### Validate Payment Method

**Endpoint**: `POST /api/payments/validate?userId={userId}&paymentMethod={method}`

**Authorization**: Users can validate own payment methods, admins can validate for any user

**Query Parameters**:
- `userId` (required): User ID
- `paymentMethod` (required): Payment method to validate

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment method validated successfully",
  "data": true
}
```

---

## Error Handling

### HTTP Status Codes

- **200 OK**: Request succeeded
- **201 Created**: Resource created successfully
- **204 No Content**: Request succeeded with no response body
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Common Error Scenarios

#### Authentication Errors

```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null
}
```

#### Authorization Errors

```json
{
  "success": false,
  "message": "You don't have permission to access this resource",
  "data": null
}
```

#### Validation Errors

```json
{
  "success": false,
  "message": "Invalid user ID",
  "data": null
}
```

#### Age Verification Errors

```json
{
  "success": false,
  "message": "Age verification required for alcohol purchases",
  "data": null
}
```

---

## Role-Based Access Control

### Available Roles

- **USER**: Regular customer
- **MERCHANT_ADMIN**: Merchant/store owner
- **DRIVER**: Delivery driver
- **ADMIN**: Platform administrator

### Role Permissions Summary

| Endpoint Category | USER | MERCHANT_ADMIN | DRIVER | ADMIN |
|-------------------|------|----------------|--------|-------|
| View Products | ✓ | ✓ | ✓ | ✓ |
| Create Products | ✗ | ✓ (own merchant) | ✗ | ✓ |
| Create Orders | ✓ | ✗ | ✗ | ✓ |
| Update Order Status | ✗ | ✓ (own merchant) | ✗ | ✓ |
| View Own Orders | ✓ | ✗ | ✗ | ✓ |
| View Merchant Orders | ✗ | ✓ (own merchant) | ✗ | ✓ |
| View Assigned Orders | ✗ | ✗ | ✓ | ✓ |
| Manage Deliveries | ✗ | ✗ | ✓ (own deliveries) | ✓ |
| Manage Drivers | ✗ | ✗ | ✗ | ✓ |
| Process Payments | ✓ (own orders) | ✗ | ✗ | ✓ |
| View All Users | ✗ | ✗ | ✗ | ✓ |


For questions or support, please open an issue on GitHub.
