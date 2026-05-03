package com.nuvo.backend.features.admin.controller

import com.nuvo.backend.features.admin.dto.*
import com.nuvo.backend.features.admin.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Management", description = "Endpoints for platform administrators to manage stores and catalogs")
class AdminController(
    private val adminService: AdminService
) {

    @PostMapping("/chains")
    @Operation(summary = "Create a new chain", description = "Adds a global store chain to the platform")
    fun createChain(@Valid @RequestBody request: AdminChainRequest) = adminService.createChain(request)

    @PostMapping("/stores")
    @Operation(summary = "Create a new store", description = "Adds a new store location, optionally linked to a chain")
    fun createStore(@Valid @RequestBody request: AdminStoreRequest) = adminService.createStore(request)

    @PostMapping("/stores/{storeId}/categories")
    @Operation(summary = "Create store category", description = "Adds a new category to a specific store's hierarchy")
    fun createCategory(@PathVariable storeId: UUID, @Valid @RequestBody request: AdminCategoryRequest) = 
        adminService.createCategory(storeId, request)

    @PostMapping("/categories/{categoryId}/sub-categories")
    @Operation(summary = "Create sub-category", description = "Adds a new sub-category to a parent category")
    fun createSubCategory(@PathVariable categoryId: UUID, @Valid @RequestBody request: AdminSubCategoryRequest) = 
        adminService.createSubCategory(categoryId, request)

    @PostMapping("/stores/{storeId}/products")
    @Operation(summary = "Create store product", description = "Adds a new product to a store's catalog")
    fun createProduct(@PathVariable storeId: UUID, @Valid @RequestBody request: AdminProductRequest) = 
        adminService.createProduct(storeId, request)

    @PostMapping("/products/{productId}/skus")
    @Operation(summary = "Create product SKU", description = "Adds a new variant (SKU) to an existing product")
    fun createSku(@PathVariable productId: UUID, @Valid @RequestBody request: AdminSkuRequest) = 
        adminService.createSku(productId, request)

    @PatchMapping("/products/{productId}/activate")
    @Operation(summary = "Activate product", description = "Publishes a valid product to the customer-facing catalog")
    fun activateProduct(@PathVariable productId: UUID) =
        adminService.activateProduct(productId)
}
