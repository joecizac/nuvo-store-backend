# Product Requirements Document (PRD): Industry-Agnostic Delivery Platform

## 1. Introduction
### 1.1 Purpose
This document outlines the business and technical requirements for the backend server of an industry-agnostic delivery application (suitable for food, grocery, cosmetics, etc.).

### 1.2 Objective
To build a scalable, secure, and robust backend using Kotlin and Spring Boot that handles user authentication, geolocation-based store discovery, product catalog management, order processing, and push notifications.

## 2. Target Audience & Personas
*   **Customer:** The end-user browsing stores, adding items to their cart, and placing orders via the mobile application.
*   **Store Owner / Manager (Future Scope):** Users managing their store profile, product catalog, and fulfilling orders.

## 3. Core Features & Requirements

### 3.1 User Authentication & Management
*   **Requirement:** Users must be able to log in via Email/Password or Social Providers (Google, Apple).
*   **Implementation:** The mobile app handles authentication via Firebase Auth. The backend verifies Firebase JWTs to authorize API requests and maintain a synchronized `User` record.
*   **Data Stored:** Firebase UID, Email, Name, Phone Number, Profile Image URL, FCM Token.
*   **Addresses:** Users can maintain multiple delivery addresses with geolocation. Full CRUD (Create, Read, Update, Delete) operations are supported.

### 3.2 Store Discovery & Geolocation
*   **Requirement:** Users must be able to discover stores near their current location using Latitude/Longitude coordinates.
*   **Implementation:** PostgreSQL with PostGIS extension for spatial queries.
*   **Features:** Search by name, filter by location (radius search), and filter by category.
*   **Hierarchy:** Stores can optionally belong to a **Chain** (e.g., McDonald's, Target) for unified branding but individual inventory management.

### 3.3 Product Catalog (Industry-Agnostic)
*   **Requirement:** Each store has its own unique, fully flexible hierarchy of Categories and Sub-Categories. This allows the platform to support diverse industries (e.g., Food, Grocery, Pharma) simultaneously by letting each store define its own structure.
*   **Features:** View products by store, filter by sub-category, and individual product details.
*   **SKUs:** Products can have multiple SKUs (e.g., Sugar: 200g, 500g, 1kg). Each SKU maintains its own price and `imageUrl` to reflect different packaging or sizes.

### 3.4 Cart Management
*   **Requirement:** Persistent shopping cart stored in the database.
*   **Rules:** A cart is restricted to products from a single store at a time.
*   **Actions:** Add/Update SKU quantities, remove items, or clear cart.

### 3.5 Order Processing
*   **Requirement:** Convert cart to order with historical snapshots.
*   **Snapshots:** `deliveryAddressSnapshot` and `OrderItem` price snapshots are required for historical accuracy.
*   **Statuses:** `PENDING`, `PREPARING`, `DISPATCHED`, `DELIVERED`, `CANCELLED`.

### 3.6 Reviews & Ratings
*   **Requirement:** Users can leave ratings (1-5) and text reviews for stores.

### 3.7 Favourites
*   **Requirement:** Users can "favourite" stores and individual products to quickly access them later.

### 3.8 Push Notifications
*   **Requirement:** Real-time order status updates via Firebase Cloud Messaging (FCM).

### 3.9 Advanced Discovery & Search
*   **Requirement:** Users must be able to perform global searches and apply granular filters/sorting.
*   **Global Search:** Single query string to find stores or products across all nearby locations.
*   **Sorting/Filtering:** Sort by `rating`, `distance`, or `delivery_fee`. Filter by `cuisine`, `price_range`, or `open_now`.

### 3.10 Real-Time Tracking
*   **Requirement:** Active orders must provide real-time courier coordinates (lat/lng) for live map tracking in the mobile client.

## 4. Technical Constraints
*   **Stack:** Kotlin, Spring Boot 3.x, PostgreSQL + PostGIS, Flyway.
*   **Security:** Stateless Firebase JWT validation.
*   **API:** RESTful with JSON and Spring Data `Pageable` for pagination.
