package com.nuvo.backend.features.catalog.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Product category")
data class CategoryDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "Category name", example = "Groceries")
    val name: String,

    @Schema(description = "URL to category image", example = "https://example.com/grocery.jpg")
    val imageUrl: String?
)

@Schema(description = "Product sub-category")
data class SubCategoryDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the parent category", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val categoryId: UUID,

    @Schema(description = "Sub-category name", example = "Dairy & Eggs")
    val name: String
)

@Schema(
    description = "SKU-derived product price summary. Values are integer minor units even though field names omit the cents suffix.",
    example = """{"minPrice":11999,"maxPrice":12999,"displayPrice":11999,"hasPriceRange":true,"currency":"USD"}"""
)
data class ProductPriceSummaryDTO(
    @Schema(description = "Lowest available SKU price in minor units", example = "399")
    val minPrice: Long,

    @Schema(description = "Highest available SKU price in minor units", example = "599")
    val maxPrice: Long,

    @Schema(description = "Product card display price in minor units", example = "399")
    val displayPrice: Long,

    @Schema(description = "Whether available SKUs span multiple prices", example = "true")
    val hasPriceRange: Boolean,

    @Schema(description = "ISO currency code", example = "USD")
    val currency: String = "USD"
)

@Schema(description = "Customer-visible product. Public APIs only return ACTIVE products with at least one available SKU.")
data class ProductDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the store this product belongs to", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val storeId: UUID,

    @Schema(description = "ID of the associated sub-category", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val subCategoryId: UUID,

    @Schema(description = "ID of the associated parent category", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val categoryId: UUID?,

    @Schema(description = "Product name", example = "Organic Whole Milk")
    val name: String,

    @Schema(description = "Detailed product description", example = "Fresh organic whole milk from local farms")
    val description: String?,

    @Schema(description = "SKU-derived product price summary using available SKUs only")
    val priceSummary: ProductPriceSummaryDTO,

    @Schema(description = "URL to product image", example = "https://example.com/milk.jpg")
    val imageUrl: String?,

    @Schema(description = "Global availability flag for the product", example = "true")
    @get:JsonProperty("isAvailable")
    val isAvailable: Boolean,

    @Schema(description = "Average customer rating (1.0 to 5.0)", example = "4.5")
    val rating: Double = 0.0,

    @Schema(description = "Whether the authenticated user has favourited this product", example = "false")
    @get:JsonProperty("isFavourite")
    val isFavourite: Boolean = false,

    @Schema(description = "Available public SKUs for this product")
    val skus: List<SkuDTO> = emptyList()
)

@Schema(description = "Product variant (Stock Keeping Unit). This is the concrete sellable unit.")
data class SkuDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the parent product", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val productId: UUID,

    @Schema(description = "Variant name", example = "Black/White - US 9")
    val name: String,

    @Schema(description = "URL to variant-specific image", example = "https://example.com/milk-2l.jpg")
    val imageUrl: String?,

    @Schema(description = "Original listing price in major units", example = "129.99")
    val originalPrice: BigDecimal,

    @Schema(description = "Discounted price in major units (optional)", example = "119.99")
    val discountedPrice: BigDecimal?,

    @Schema(description = "Availability flag for this specific variant", example = "true")
    @get:JsonProperty("isAvailable")
    val isAvailable: Boolean
)
