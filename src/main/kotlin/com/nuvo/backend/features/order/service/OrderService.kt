package com.nuvo.backend.features.order.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.exception.UnauthorizedException
import com.nuvo.backend.common.exception.ValidationException
import com.nuvo.backend.features.order.domain.Order
import com.nuvo.backend.features.order.domain.OrderItem
import com.nuvo.backend.features.order.domain.OrderStatus
import com.nuvo.backend.features.order.domain.OrderStatusChangedEvent
import com.nuvo.backend.features.order.dto.CheckoutRequest
import com.nuvo.backend.features.order.dto.OrderDTO
import com.nuvo.backend.features.order.dto.OrderItemDTO
import com.nuvo.backend.features.order.repository.OrderRepository
import com.nuvo.backend.features.user.repository.UserAddressRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderService(
    private val cartService: CartService,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val addressRepository: UserAddressRepository,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher
) {
    private fun UUID?.required(fieldName: String): UUID =
        this ?: throw IllegalStateException("Missing UUID for $fieldName")


    @Transactional
    fun checkout(firebaseUid: String, request: CheckoutRequest): OrderDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val cart = cartService.getOrCreateCart(firebaseUid)
        
        if (cart.items.isEmpty()) throw ValidationException("Cart is empty")
        val store = cart.store ?: throw ValidationException("Cart has no store assigned")
        
        val address = addressRepository.findById(request.addressId)
            .filter { it.user.id == user.id }
            .orElseThrow { ResourceNotFoundException("Address not found") }

        // Snapshot address to JSON
        val addressSnapshot = objectMapper.writeValueAsString(
            mapOf(
                "title" to address.title,
                "fullAddress" to address.fullAddress,
                "latitude" to address.location.y,
                "longitude" to address.location.x
            )
        )

        val totalAmount = cart.items.sumOf { 
            (it.sku.discountedPrice ?: it.sku.originalPrice).multiply(BigDecimal(it.quantity)) 
        }

        val order = Order(
            user = user,
            store = store,
            totalAmount = totalAmount,
            deliveryAddressSnapshot = addressSnapshot
        )

        cart.items.forEach { cartItem ->
            val price = cartItem.sku.discountedPrice ?: cartItem.sku.originalPrice
            order.items.add(
                OrderItem(
                    order = order,
                    sku = cartItem.sku,
                    snapshotPrice = price,
                    quantity = cartItem.quantity
                )
            )
        }

        val savedOrder = orderRepository.save(order)
        cartService.clearCart(firebaseUid)

        // Publish Asynchronous Event for Notifications
        eventPublisher.publishEvent(
            OrderStatusChangedEvent(
                orderId = savedOrder.id.required("order.id"),
                userId = user.id.required("user.id"),
                newStatus = savedOrder.status,
                fcmToken = user.fcmToken
            )
        )

        return savedOrder.toDTO()
    }

    @Transactional(readOnly = true)
    fun getOrderHistory(firebaseUid: String, pageable: Pageable): Page<OrderDTO> {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val userId = user.id ?: throw ResourceNotFoundException("User ID is missing")
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getOrderDetails(firebaseUid: String, orderId: UUID): OrderDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val order = orderRepository.findById(orderId).orElseThrow { ResourceNotFoundException("Order not found") }
        if (order.user?.id != user.id) throw UnauthorizedException("You are not authorized to view this order")
        return order.toDTO()
    }

    private fun Order.toDTO() = OrderDTO(
        id = id.required("order.id"),
        storeId = store?.id.required("order.store.id"),
        status = status,
        totalAmount = totalAmount,
        deliveryAddressSnapshot = deliveryAddressSnapshot,
        items = items.map { it.toDTO() },
        createdAt = createdAt.toString()
    )

    private fun OrderItem.toDTO() = OrderItemDTO(
        skuId = sku?.id,
        skuName = sku?.name,
        snapshotPrice = snapshotPrice,
        quantity = quantity
    )
}
