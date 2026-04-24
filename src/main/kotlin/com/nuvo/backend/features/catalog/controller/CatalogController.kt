package com.nuvo.backend.features.catalog.controller

import com.nuvo.backend.features.catalog.dto.CategoryDTO
import com.nuvo.backend.features.catalog.dto.ProductDTO
import com.nuvo.backend.features.catalog.dto.SubCategoryDTO
import com.nuvo.backend.features.catalog.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
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
    @Operation(summary = "List store products", description = "Returns a paginated list of products for a store, optionally filtered by sub-category")
    fun getStoreProducts(
        @PathVariable storeId: UUID,
        @RequestParam(required = false) subCategoryId: UUID?,
        pageable: Pageable
    ): Page<ProductDTO> {
        return catalogService.getStoreProducts(storeId, subCategoryId, pageable)
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product details", description = "Retrieves comprehensive information for a specific product, including all available SKUs")
    fun getProduct(@PathVariable productId: UUID): ProductDTO {
        return catalogService.getProduct(productId)
    }
}
