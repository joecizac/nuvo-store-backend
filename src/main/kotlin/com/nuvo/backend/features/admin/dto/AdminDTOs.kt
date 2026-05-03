package com.nuvo.backend.features.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*

@Schema(description = "Request to create or update a chain")
data class AdminChainRequest(
    @Schema(description = "Chain or brand name", example = "Nike")
    @field:NotBlank val name: String,

    @Schema(description = "Optional chain description", example = "Global sportswear and footwear brand")
    val description: String?,

    @Schema(description = "Logo image URL", example = "https://cdn.example.com/chains/nike-logo.png")
    val logoUrl: String?,

    @Schema(description = "Banner image URL", example = "https://cdn.example.com/chains/nike-banner.jpg")
    val bannerUrl: String?
)

@Schema(description = "Request to create a store. chainId is optional for independent stores.")
data class AdminStoreRequest(
    @Schema(description = "Optional parent chain ID. Omit/null for independent stores.", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c", nullable = true)
    val chainId: UUID?,

    @Schema(description = "Store location name", example = "Nike Store - Cape Town V&A Waterfront")
    @field:NotBlank val name: String,

    @Schema(description = "Store description", example = "Official Nike retail store for footwear, apparel, and accessories")
    val description: String?,

    @Schema(description = "Store contact number", example = "+27210000000")
    val contactNumber: String?,

    @Schema(description = "Store-specific logo URL", example = "https://cdn.example.com/stores/nike-cpt-logo.png")
    val logoUrl: String?,

    @Schema(description = "Store-specific banner URL", example = "https://cdn.example.com/stores/nike-cpt-banner.jpg")
    val bannerUrl: String?,

    @Schema(description = "Latitude", example = "-33.9034")
    @field:NotNull val latitude: Double,

    @Schema(description = "Longitude", example = "18.4208")
    @field:NotNull val longitude: Double,

    @Schema(description = "Human-readable store address", example = "V&A Waterfront, Cape Town")
    @field:NotBlank val address: String,

    @Schema(description = "Whether the store is available for discovery", example = "true")
    val isActive: Boolean = true
)

@Schema(description = "Request to create a store-owned category")
data class AdminCategoryRequest(
    @Schema(description = "Category name", example = "Shoes")
    @field:NotBlank val name: String,

    @Schema(description = "Category image URL", example = "https://cdn.example.com/categories/shoes.jpg")
    val imageUrl: String?
)

@Schema(description = "Request to create a sub-category under a category")
data class AdminSubCategoryRequest(
    @Schema(description = "Sub-category name", example = "Running Shoes")
    @field:NotBlank val name: String
)

@Schema(description = "Request to create a draft product. SKUs are added separately, then the product is activated.")
data class AdminProductRequest(
    @Schema(description = "Sub-category ID. It must belong to the target store.", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    @field:NotNull val subCategoryId: UUID,

    @Schema(description = "Product display name", example = "Nike Air Max 270")
    @field:NotBlank val name: String,

    @Schema(description = "Product description", example = "Lightweight lifestyle shoes with Max Air cushioning")
    val description: String?,

    @Schema(description = "Product image URL", example = "https://cdn.example.com/products/air-max-270.jpg")
    val imageUrl: String?,

    @Schema(description = "Admin availability flag. Product is still hidden from public APIs until activated.", example = "true")
    val isAvailable: Boolean = true
)

@Schema(description = "Request to create a SKU. For shoes/apparel, each SKU should represent a sellable variant such as color + size.")
data class AdminSkuRequest(
    @Schema(description = "SKU variant name", example = "Black/White - US 9")
    @field:NotBlank val name: String,

    @Schema(description = "SKU-specific image URL", example = "https://cdn.example.com/skus/air-max-270-black-us9.jpg")
    val imageUrl: String?,

    @Schema(description = "Original price in major units", example = "129.99")
    @field:NotNull val originalPrice: BigDecimal,

    @Schema(description = "Optional discounted price in major units. Must be greater than zero and not greater than originalPrice.", example = "119.99")
    val discountedPrice: BigDecimal?,

    @Schema(description = "Whether this SKU is sellable and public-visible after product activation", example = "true")
    val isAvailable: Boolean = true
)
