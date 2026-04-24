package com.nuvo.backend.features.order.dto

import com.nuvo.backend.features.order.domain.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "Shopping cart information")
data class CartDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the store associated with the cart", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val storeId: UUID?,

    @Schema(description = "List of items in the cart")
    val items: List<CartItemDTO>,

    @Schema(description = "Total cost of all items", example = "45.99")
    val totalAmount: BigDecimal
)

@Schema(description = "Individual item within a shopping cart")
data class CartItemDTO(
    @Schema(description = "Internal unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the specific product variant", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val skuId: UUID,

    @Schema(description = "Name of the variant", example = "2 Liters")
    val skuName: String,

    @Schema(description = "Quantity of this item", example = "2")
    val quantity: Int,

    @Schema(description = "Price per unit at the time of adding", example = "10.00")
    val price: BigDecimal,

    @Schema(description = "Total cost for this item (price * quantity)", example = "20.00")
    val subTotal: BigDecimal
)

@Schema(description = "Request to add an item to the shopping cart")
data class AddToCartRequest(
    @Schema(description = "ID of the specific product variant to add", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    @field:NotNull(message = "SKU ID is required")
    val skuId: UUID,

    @Schema(description = "Quantity of the variant to add", example = "1")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)

@Schema(description = "Finalized order information")
data class OrderDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID,

    @Schema(description = "ID of the store where the order was placed", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val storeId: UUID,

    @Schema(description = "Current status of the order", example = "PENDING")
    val status: OrderStatus,

    @Schema(description = "Total amount paid", example = "120.50")
    val totalAmount: BigDecimal,

    @Schema(description = "JSON snapshot of the delivery address at the time of order", example = "{\"fullAddress\": \"123 Main St\"}")
    val deliveryAddressSnapshot: String,

    @Schema(description = "List of items purchased")
    val items: List<OrderItemDTO>,

    @Schema(description = "Date and time the order was placed", example = "2024-04-23T14:30:00")
    val createdAt: String
)

@Schema(description = "Snapshotted information of an item in a completed order")
data class OrderItemDTO(
    @Schema(description = "ID of the variant (might be null if variant is deleted)", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val skuId: UUID?,

    @Schema(description = "Name of the variant at the time of order", example = "Whopper Double")
    val skuName: String?,

    @Schema(description = "Historical price paid for this item", example = "8.99")
    val snapshotPrice: BigDecimal,

    @Schema(description = "Quantity purchased", example = "1")
    val quantity: Int
)

@Schema(description = "Request to convert current cart into an order")
data class CheckoutRequest(
    @Schema(description = "ID of the user address to be used for delivery", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    @field:NotNull(message = "Address ID is required")
    val addressId: UUID
)
