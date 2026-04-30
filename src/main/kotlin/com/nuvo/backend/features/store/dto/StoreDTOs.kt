package com.nuvo.backend.features.store.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Global store chain information")
data class ChainDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "Chain name", example = "Burger King")
    val name: String,

    @Schema(description = "General description", example = "Global fast food chain")
    val description: String?,

    @Schema(description = "URL to chain logo", example = "https://example.com/logo.png")
    val logoUrl: String?,

    @Schema(description = "URL to chain banner image", example = "https://example.com/banner.jpg")
    val bannerUrl: String?
)

@Schema(description = "Detailed store information")
data class StoreDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the parent chain (optional)", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val chainId: UUID?,

    @Schema(description = "Store name", example = "Burger King - Downtown")
    val name: String,

    @Schema(description = "Store-specific description", example = "The best burgers in the city")
    val description: String?,

    @Schema(description = "Contact phone number", example = "+123456789")
    val contactNumber: String?,

    @Schema(description = "URL to store-specific logo (overrides chain logo)", example = "https://example.com/store-logo.png")
    val logoUrl: String?,

    @Schema(description = "URL to store-specific banner image (overrides chain banner)", example = "https://example.com/store-banner.jpg")
    val bannerUrl: String?,

    @Schema(description = "Latitude coordinate", example = "-33.9249")
    val latitude: Double,

    @Schema(description = "Longitude coordinate", example = "18.4241")
    val longitude: Double,

    @Schema(description = "Human-readable address", example = "123 Main St, Cape Town")
    val address: String,

    @Schema(description = "Average customer rating (1.0 to 5.0)", example = "4.5")
    val averageRating: Double,

    @Schema(description = "Primary cuisine or industry type", example = "Burgers")
    val cuisine: String?,

    @Schema(description = "Price range indicator (1-4)", example = "2")
    val priceRange: Int,

    @Schema(description = "Standard delivery fee", example = "5.99")
    val deliveryFee: java.math.BigDecimal,

    @Schema(description = "Opening time", example = "09:00")
    val openAt: String?,

    @Schema(description = "Closing time", example = "22:00")
    val closeAt: String?
)
