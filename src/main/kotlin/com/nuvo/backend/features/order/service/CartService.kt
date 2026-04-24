package com.nuvo.backend.features.order.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.catalog.repository.SKURepository
import com.nuvo.backend.features.order.domain.Cart
import com.nuvo.backend.features.order.domain.CartItem
import com.nuvo.backend.features.order.dto.AddToCartRequest
import com.nuvo.backend.features.order.dto.CartDTO
import com.nuvo.backend.features.order.dto.CartItemDTO
import com.nuvo.backend.features.order.repository.CartRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val skuRepository: SKURepository
) {

    @Transactional
    fun getOrCreateCart(firebaseUid: String): Cart {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val userId = user.id ?: throw ResourceNotFoundException("User ID is missing")
        return cartRepository.findByUserId(userId) ?: cartRepository.save(Cart(user = user))
    }

    @Transactional
    fun addItemToCart(firebaseUid: String, request: AddToCartRequest): CartDTO {
        val cart = getOrCreateCart(firebaseUid)
        val sku = skuRepository.findById(request.skuId).orElseThrow { ResourceNotFoundException("SKU not found") }
        val store = sku.product.store

        // Single Store Rule
        if (cart.store != null && cart.store?.id != store.id) {
            cart.items.clear()
        }
        cart.store = store

        val existingItem = cart.items.find { it.sku.id == sku.id }
        if (existingItem != null) {
            existingItem.quantity += request.quantity
        } else {
            cart.items.add(CartItem(cart = cart, sku = sku, quantity = request.quantity))
        }

        return cartRepository.save(cart).toDTO()
    }

    @Transactional
    fun updateItemQuantity(firebaseUid: String, itemId: UUID, quantity: Int): CartDTO {
        val cart = getOrCreateCart(firebaseUid)
        val item = cart.items.find { it.id == itemId } ?: throw ResourceNotFoundException("Item not found in cart")
        
        if (quantity <= 0) {
            cart.items.remove(item)
        } else {
            item.quantity = quantity
        }

        if (cart.items.isEmpty()) {
            cart.store = null
        }

        return cartRepository.save(cart).toDTO()
    }

    @Transactional
    fun clearCart(firebaseUid: String) {
        val cart = getOrCreateCart(firebaseUid)
        cart.items.clear()
        cart.store = null
        cartRepository.save(cart)
    }

    @Transactional(readOnly = true)
    fun getCartDTO(firebaseUid: String): CartDTO {
        return getOrCreateCart(firebaseUid).toDTO()
    }

    private fun Cart.toDTO(): CartDTO {
        val itemDTOs = items.map { it.toDTO() }
        val total = itemDTOs.sumOf { it.subTotal }
        return CartDTO(id ?: UUID.randomUUID(), store?.id, itemDTOs, total)
    }

    private fun CartItem.toDTO(): CartItemDTO {
        val price = sku.discountedPrice ?: sku.originalPrice
        return CartItemDTO(
            id = id ?: UUID.randomUUID(),
            skuId = sku.id ?: UUID.randomUUID(),
            skuName = sku.name,
            quantity = quantity,
            price = price,
            subTotal = price.multiply(BigDecimal(quantity))
        )
    }
}
