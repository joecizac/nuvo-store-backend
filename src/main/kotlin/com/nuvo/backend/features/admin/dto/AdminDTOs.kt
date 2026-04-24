package com.nuvo.backend.features.admin.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.*

@Schema(description = "Request to create or update a chain")
data class AdminChainRequest(
    @field:NotBlank val name: String,
    val description: String?,
    val logoUrl: String?,
    val bannerUrl: String?
)

@Schema(description = "Request to create or update a store")
data class AdminStoreRequest(
    val chainId: UUID?,
    @field:NotBlank val name: String,
    val description: String?,
    val contactNumber: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    @field:NotNull val latitude: Double,
    @field:NotNull val longitude: Double,
    @field:NotBlank val address: String,
    val isActive: Boolean = true
)

@Schema(description = "Request to create or update a category")
data class AdminCategoryRequest(
    @field:NotBlank val name: String,
    val imageUrl: String?
)

@Schema(description = "Request to create or update a sub-category")
data class AdminSubCategoryRequest(
    @field:NotBlank val name: String
)

@Schema(description = "Request to create or update a product")
data class AdminProductRequest(
    @field:NotNull val subCategoryId: UUID,
    @field:NotBlank val name: String,
    val description: String?,
    val imageUrl: String?,
    val isAvailable: Boolean = true
)

@Schema(description = "Request to create or update a SKU")
data class AdminSkuRequest(
    @field:NotBlank val name: String,
    val imageUrl: String?,
    @field:NotNull val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal?,
    val isAvailable: Boolean = true
)
