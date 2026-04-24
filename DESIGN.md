# DESIGN.md: Nuvo Delivery Platform (Compose Multiplatform)

## 1. Design Philosophy: "The Expressive Editorial"
Leveraging **Material 3 Expressive** guidelines to create a high-impact, industry-agnostic storefront. The UI transitions between "Editorial Hero" moments for discovery and "Utility Containers" for checkout.

## 2. Color System: Dynamic & High-Contrast
Utilizing the M3 Expressive color roles to sharpen hierarchy.

*   **Primary:** `Royal Indigo` (#4F46E5) - High energy for primary actions.
*   **Secondary:** `Calm Mint` (#6EE7B7) - Success, tracking, and trust.
*   **Tertiary:** `Terracotta` (#FB923C) - Dynamic highlight for promos/urgency.
*   **Surface Roles:**
    - `Surface Container Low` (#F9FAFB) - Screen background.
    - `Surface Container High` (#FFFFFF) - Active cards/sheets.
    - `Surface Bright` (#EEF2FF) - Emphasis backgrounds.

## 3. Typography: Editorial Scale
**Font:** `Plus Jakarta Sans` (Geometric Sans Serif)
*   **Display Large:** 57sp, Bold - *Promo/Hero numbers*
*   **Headline Medium:** 28sp, Semi-Bold - *Editorial screen headers*
*   **Title Large:** 22sp, Medium - *Store names / Feature titles*
*   **Body Large:** 16sp, Regular - *Standard info*
*   **Label Medium:** 12sp, Medium - *Badges / Metadata*

## 4. Layout & Motion (CMP Specific)
*   **Expressive App Bars:** Expanding/collapsing headers that shift from large editorial text to compact icons on scroll.
*   **Bento Grids (Adaptive):** Content grouped in high-radius containers (24dp) that reflow using **Compose Window Size Classes** (Compact, Medium, Expanded).
*   **Physics-Based Motion:** Utilizing `Spring` specs in `animateContentSize()` and `AnimatedVisibility` for organic, fluid transitions.
*   **Split Buttons & FABs:** Large, expressive Floating Action Buttons that morph into bottom sheets for Cart/Checkout actions.

## 5. Implementation Strategy: Jetpack Compose
*   **Material 3 Library:** `androidx.compose.material3:material3` (Expressive variant).
*   **Adaptive Layouts:** `androidx.compose.material3.adaptive`.
*   **Image Loading:** `Coil3` (CMP compatible) for async product images with BlurHash support.
*   **Design Tokens:** Single source of truth in `Theme.kt` mapping to `DESIGN.md` hex codes.
