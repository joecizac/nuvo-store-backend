package com.nuvo.backend.features.order.controller

import com.nuvo.backend.features.order.dto.CheckoutRequest
import com.nuvo.backend.features.order.dto.OrderDTO
import com.nuvo.backend.features.order.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Processing", description = "Endpoints for checkout and order history management")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @Operation(summary = "Place order (Checkout)", description = "Converts the user's current shopping cart into a finalized order")
    fun checkout(principal: Principal, @Valid @RequestBody request: CheckoutRequest): OrderDTO {
        return orderService.checkout(principal.name, request)
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieves a paginated list of all previous orders for the current user")
    fun getOrderHistory(principal: Principal, pageable: Pageable): Page<OrderDTO> {
        return orderService.getOrderHistory(principal.name, pageable)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details", description = "Retrieves comprehensive information for a specific order by its ID")
    fun getOrderDetails(principal: Principal, @PathVariable id: UUID): OrderDTO {
        return orderService.getOrderDetails(principal.name, id)
    }
}
