package com.nuvo.backend.features.order.repository

import com.nuvo.backend.features.order.domain.Cart
import com.nuvo.backend.features.order.domain.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CartRepository : JpaRepository<Cart, UUID> {
    @EntityGraph(attributePaths = ["items", "items.sku"])
    fun findByUserId(userId: UUID): Cart?
}

interface OrderRepository : JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = ["items", "items.sku", "store"])
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Order>

    @EntityGraph(attributePaths = ["items", "items.sku", "store"])
    override fun findById(id: UUID): Optional<Order>
}
