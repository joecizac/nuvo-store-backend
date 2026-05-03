package com.nuvo.backend.features.admin.controller

import com.nuvo.backend.features.admin.dto.*
import com.nuvo.backend.features.admin.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @Operation(
        summary = "Create a chain",
        description = "Creates shared brand metadata for a chain such as Nike, McDonald's, Target, or BestBuy. Stores can optionally link to a chain.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [ExampleObject(
                    name = "Create chain",
                    value = """{"name":"Nike","description":"Global sportswear and footwear brand","logoUrl":"https://cdn.example.com/chains/nike-logo.png","bannerUrl":"https://cdn.example.com/chains/nike-banner.jpg"}"""
                )]
            )]
        )
    )
    fun createChain(@Valid @org.springframework.web.bind.annotation.RequestBody request: AdminChainRequest) = adminService.createChain(request)

    @PostMapping("/stores")
    @Operation(
        summary = "Create a store",
        description = "Creates a store location. chainId is optional; omit it or pass null for an independent store.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [
                    ExampleObject(
                        name = "Chain store",
                        value = """{"chainId":"018e6b12-1234-7c2a-8921-9d1e2f3a4b5c","name":"Nike Store - Cape Town V&A Waterfront","description":"Official Nike retail store","contactNumber":"+27210000000","logoUrl":"https://cdn.example.com/stores/nike-cpt-logo.png","bannerUrl":"https://cdn.example.com/stores/nike-cpt-banner.jpg","latitude":-33.9034,"longitude":18.4208,"address":"V&A Waterfront, Cape Town","isActive":true}"""
                    ),
                    ExampleObject(
                        name = "Independent store",
                        value = """{"chainId":null,"name":"Corner Groceries","description":"Independent neighbourhood grocery store","contactNumber":"+27219999999","logoUrl":null,"bannerUrl":null,"latitude":-33.9249,"longitude":18.4241,"address":"123 Main Street, Cape Town","isActive":true}"""
                    )
                ]
            )]
        )
    )
    fun createStore(@Valid @org.springframework.web.bind.annotation.RequestBody request: AdminStoreRequest) = adminService.createStore(request)

    @PostMapping("/stores/{storeId}/categories")
    @Operation(
        summary = "Create store category",
        description = "Adds a top-level category to a specific store. Categories are store-owned and are not global.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [ExampleObject(name = "Create category", value = """{"name":"Shoes","imageUrl":"https://cdn.example.com/categories/shoes.jpg"}""")]
            )]
        )
    )
    fun createCategory(@PathVariable storeId: UUID, @Valid @org.springframework.web.bind.annotation.RequestBody request: AdminCategoryRequest) = 
        adminService.createCategory(storeId, request)

    @PostMapping("/categories/{categoryId}/sub-categories")
    @Operation(
        summary = "Create sub-category",
        description = "Adds a sub-category under a store-owned category.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [ExampleObject(name = "Create sub-category", value = """{"name":"Running Shoes"}""")]
            )]
        )
    )
    fun createSubCategory(@PathVariable categoryId: UUID, @Valid @org.springframework.web.bind.annotation.RequestBody request: AdminSubCategoryRequest) = 
        adminService.createSubCategory(categoryId, request)

    @PostMapping("/stores/{storeId}/products")
    @Operation(
        summary = "Create draft product",
        description = "Creates a DRAFT product under a sub-category that must belong to the target store. Draft products are not returned from public product APIs until activated.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [ExampleObject(
                    name = "Create draft product",
                    value = """{"subCategoryId":"018e6b12-1234-7c2a-8921-9d1e2f3a4b5c","name":"Nike Air Max 270","description":"Lightweight lifestyle shoes with Max Air cushioning","imageUrl":"https://cdn.example.com/products/air-max-270.jpg","isAvailable":true}"""
                )]
            )]
        )
    )
    fun createProduct(@PathVariable storeId: UUID, @Valid @org.springframework.web.bind.annotation.RequestBody request: AdminProductRequest) = 
        adminService.createProduct(storeId, request)

    @PostMapping("/products/{productId}/skus")
    @Operation(
        summary = "Create product SKU",
        description = "Adds a sellable SKU to a product. For apparel/shoes, model the SKU as the concrete sellable variant, e.g. colorway + size.",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                examples = [ExampleObject(
                    name = "Create shoe SKU",
                    value = """{"name":"Black/White - US 9","imageUrl":"https://cdn.example.com/skus/air-max-270-black-us9.jpg","originalPrice":129.99,"discountedPrice":119.99,"isAvailable":true}"""
                )]
            )]
        )
    )
    fun createSku(@PathVariable productId: UUID, @Valid @org.springframework.web.bind.annotation.RequestBody request: AdminSkuRequest) = 
        adminService.createSku(productId, request)

    @PatchMapping("/products/{productId}/activate")
    @Operation(
        summary = "Activate product",
        description = "Publishes a draft product to the customer-facing catalog. Validation requires a consistent store/sub-category hierarchy, at least one SKU, at least one available SKU, and valid SKU prices.",
        responses = [
            ApiResponse(responseCode = "200", description = "Product activated"),
            ApiResponse(responseCode = "400", description = "Product is not valid for activation"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun activateProduct(@PathVariable productId: UUID) =
        adminService.activateProduct(productId)
}
