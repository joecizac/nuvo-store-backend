package com.nuvo.backend.features.social.repository

import com.nuvo.backend.features.social.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReviewRepository : JpaRepository<Review, UUID> {
    fun findAllByStoreIdOrderByCreatedAtDesc(storeId: UUID, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<Review>
    fun findAllByStoreIdOrderByCreatedAtDesc(storeId: UUID): List<Review>
    fun findByUserIdAndOrderId(userId: UUID, orderId: UUID): Review?
    fun findByUserIdAndStoreId(userId: UUID, storeId: UUID): Review?
    fun countByStoreId(storeId: UUID): Long
}

interface FavouriteStoreRepository : JpaRepository<FavouriteStore, FavouriteStoreId> {
    fun findAllByUserId(userId: UUID): List<FavouriteStore>
}

interface FavouriteProductRepository : JpaRepository<FavouriteProduct, FavouriteProductId> {
    fun findAllByUserId(userId: UUID): List<FavouriteProduct>
}
