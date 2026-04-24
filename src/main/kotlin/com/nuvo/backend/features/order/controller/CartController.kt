package com.nuvo.backend.features.order.controller

import com.nuvo.backend.features.order.dto.AddToCartRequest
import com.nuvo.backend.features.order.dto.CartDTO
import com.nuvo.backend.features.order.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart Management", description = "Endpoints for managing the user's persistent shopping cart")
class CartController(
    private val cartService: CartService
) {

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieves the current shopping cart for the authenticated user")
    fun getCart(principal: Principal): CartDTO {
        return cartService.getCartDTO(principal.name)
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a specific product variant (SKU) to the cart, enforcing the single-store rule")
    fun addItem(principal: Principal, @Valid @RequestBody request: AddToCartRequest): CartDTO {
        return cartService.addItemToCart(principal.name, request)
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of an existing item in the cart, or removes it if quantity is 0")
    fun updateQuantity(
        principal: Principal,
        @PathVariable itemId: UUID,
        @RequestParam quantity: Int
    ): CartDTO {
        return cartService.updateItemQuantity(principal.name, itemId, quantity)
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the current user's shopping cart")
    fun clearCart(principal: Principal) {
        cartService.clearCart(principal.name)
    }
}

