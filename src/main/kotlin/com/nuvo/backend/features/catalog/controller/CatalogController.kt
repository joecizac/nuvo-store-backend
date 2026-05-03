package com.nuvo.backend.features.catalog.controller

import com.nuvo.backend.features.catalog.dto.CategoryDTO
import com.nuvo.backend.features.catalog.dto.ProductDTO
import com.nuvo.backend.features.catalog.dto.SubCategoryDTO
import com.nuvo.backend.features.catalog.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catalog Management", description = "Endpoints for browsing store-specific categories and products")
class CatalogController(
    private val catalogService: CatalogService
) {

    @GetMapping("/stores/{storeId}/categories")
    @Operation(summary = "Get store categories", description = "Retrieves the high-level categories defined by a specific store")
    fun getStoreCategories(@PathVariable storeId: UUID): List<CategoryDTO> {
        return catalogService.getStoreCategories(storeId)
    }

    @GetMapping("/categories/{categoryId}/sub-categories")
    @Operation(summary = "Get sub-categories", description = "Lists all sub-categories belonging to a parent category")
    fun getSubCategories(@PathVariable categoryId: UUID): List<SubCategoryDTO> {
        return catalogService.getSubCategories(categoryId)
    }

    @GetMapping("/stores/{storeId}/products")
    @Operation(
        summary = "List public store products",
        description = "Returns customer-visible products for a store. Only ACTIVE products with product availability enabled and at least one available SKU are returned. Product pricing is provided through priceSummary and is derived from available SKUs only."
    )
    fun getStoreProducts(
        @PathVariable storeId: UUID,
        @RequestParam(required = false) subCategoryId: UUID?,
        pageable: Pageable
    ): List<ProductDTO> {
        return catalogService.getStoreProducts(storeId, subCategoryId, pageable).content
    }

    @GetMapping("/products/{productId}")
    @Operation(
        summary = "Get public product details",
        description = "Retrieves a customer-visible ACTIVE product with available SKUs. Draft, archived, unavailable, or SKU-less products are treated as not found for public APIs."
    )
    fun getProduct(@PathVariable productId: UUID): ProductDTO {
        return catalogService.getProduct(productId)
    }
}
