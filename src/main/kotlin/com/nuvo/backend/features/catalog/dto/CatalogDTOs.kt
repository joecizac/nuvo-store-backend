package com.nuvo.backend.features.catalog.dto

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

@Schema(description = "Base product information")
data class ProductDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the store this product belongs to", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val storeId: UUID,

    @Schema(description = "ID of the associated sub-category", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val subCategoryId: UUID,

    @Schema(description = "Product name", example = "Organic Whole Milk")
    val name: String,

    @Schema(description = "Detailed product description", example = "Fresh organic whole milk from local farms")
    val description: String?,

    @Schema(description = "URL to product image", example = "https://example.com/milk.jpg")
    val imageUrl: String?,

    @Schema(description = "Global availability flag for the product", example = "true")
    val isAvailable: Boolean,

    @Schema(description = "List of specific variants (SKUs) for this product")
    val skus: List<SkuDTO> = emptyList()
)

@Schema(description = "Product variant (Stock Keeping Unit)")
data class SkuDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the parent product", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val productId: UUID,

    @Schema(description = "Variant name", example = "2 Liters")
    val name: String,

    @Schema(description = "URL to variant-specific image", example = "https://example.com/milk-2l.jpg")
    val imageUrl: String?,

    @Schema(description = "Original listing price", example = "4.50")
    val originalPrice: BigDecimal,

    @Schema(description = "Discounted price (optional)", example = "3.99")
    val discountedPrice: BigDecimal?,

    @Schema(description = "Availability flag for this specific variant", example = "true")
    val isAvailable: Boolean
)
