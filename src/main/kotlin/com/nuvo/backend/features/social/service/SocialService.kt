package com.nuvo.backend.features.social.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.order.repository.OrderRepository
import com.nuvo.backend.features.social.domain.*
import com.nuvo.backend.features.social.dto.ReviewDTO
import com.nuvo.backend.features.social.dto.SubmitReviewRequest
import com.nuvo.backend.features.social.repository.FavouriteProductRepository
import com.nuvo.backend.features.social.repository.FavouriteStoreRepository
import com.nuvo.backend.features.social.repository.ReviewRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class SocialService(
    private val reviewRepository: ReviewRepository,
    private val favouriteStoreRepository: FavouriteStoreRepository,
    private val favouriteProductRepository: FavouriteProductRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun submitReview(firebaseUid: String, storeId: UUID, request: SubmitReviewRequest): ReviewDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val store = storeRepository.findById(storeId).orElseThrow { ResourceNotFoundException("Store not found") }

        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        val review = reviewRepository.findByUserIdAndStoreId(userId, storeId)
            ?.apply {
                this.rating = request.rating
                this.comment = request.comment
            }
            ?: Review(user = user, store = store, rating = request.rating, comment = request.comment)

        val savedReview = reviewRepository.save(review)
        updateStoreAverageRating(storeId)

        return savedReview.toDTO()
    }

    @Transactional
    fun submitOrderReview(firebaseUid: String, orderId: UUID, request: SubmitReviewRequest): ReviewDTO {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val order = orderRepository.findById(orderId).orElseThrow { ResourceNotFoundException("Order not found") }

        if (order.user?.id != user.id) throw ResourceNotFoundException("Order not found for this user")
        val store = order.store ?: throw ResourceNotFoundException("Store not found for this order")

        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        val review = reviewRepository.findByUserIdAndOrderId(userId, orderId)
            ?.apply {
                this.rating = request.rating
                this.comment = request.comment
            }
            ?: Review(user = user, store = store, order = order, rating = request.rating, comment = request.comment)

        val savedReview = reviewRepository.save(review)
        updateStoreAverageRating(store.id!!)

        return savedReview.toDTO()
    }

    private fun updateStoreAverageRating(storeId: UUID) {
        val store = storeRepository.findById(storeId).orElseThrow { ResourceNotFoundException("Store not found") }
        val reviews = reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
        if (reviews.isNotEmpty()) {
            store.averageRating = reviews.map { it.rating }.average()
            storeRepository.save(store)
        }
    }

    @Transactional(readOnly = true)
    fun getStoreReviews(storeId: UUID, pageable: Pageable): Page<ReviewDTO> {
        return reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId, pageable).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getStoreReviews(storeId: UUID): List<ReviewDTO> {
        return reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getOrderTracking(firebaseUid: String, orderId: UUID): Map<String, Double?> {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val order = orderRepository.findById(orderId).orElseThrow { ResourceNotFoundException("Order not found") }
        
        if (order.user?.id != user.id) throw ResourceNotFoundException("Order not found for this user")

        return mapOf(
            "lat" to order.currentLat,
            "lng" to order.currentLng
        )
    }

    @Transactional
    fun toggleFavouriteStore(firebaseUid: String, storeId: UUID) {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val store = storeRepository.findById(storeId).orElseThrow { ResourceNotFoundException("Store not found") }
        
        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        val id = FavouriteStoreId(userId, storeId)

        if (favouriteStoreRepository.existsById(id)) {
            favouriteStoreRepository.deleteById(id)
        } else {
            favouriteStoreRepository.save(FavouriteStore(id, user, store))
        }
    }

    @Transactional
    fun toggleFavouriteProduct(firebaseUid: String, productId: UUID) {
        val user = userRepository.findByFirebaseUid(firebaseUid) ?: throw ResourceNotFoundException("User not found")
        val product = productRepository.findById(productId).orElseThrow { ResourceNotFoundException("Product not found") }
        
        val userId = user.id ?: throw ResourceNotFoundException("User ID missing")
        val id = FavouriteProductId(userId, productId)

        if (favouriteProductRepository.existsById(id)) {
            favouriteProductRepository.deleteById(id)
        } else {
            favouriteProductRepository.save(FavouriteProduct(id, user, product))
        }
    }

    private fun Review.toDTO() = ReviewDTO(
        id = id ?: UUID.randomUUID(),
        userId = user.id ?: UUID.randomUUID(),
        userName = user.name,
        rating = rating,
        comment = comment,
        createdAt = createdAt.toString()
    )
}
