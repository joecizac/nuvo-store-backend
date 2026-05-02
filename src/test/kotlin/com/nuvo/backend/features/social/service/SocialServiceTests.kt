package com.nuvo.backend.features.social.service

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.TestFixtures.any
import com.nuvo.backend.common.exception.UnauthorizedException
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.order.repository.OrderRepository
import com.nuvo.backend.features.social.domain.FavouriteProduct
import com.nuvo.backend.features.social.domain.FavouriteProductId
import com.nuvo.backend.features.social.domain.FavouriteStore
import com.nuvo.backend.features.social.domain.FavouriteStoreId
import com.nuvo.backend.features.social.domain.Review
import com.nuvo.backend.features.social.dto.SubmitReviewRequest
import com.nuvo.backend.features.social.repository.FavouriteProductRepository
import com.nuvo.backend.features.social.repository.FavouriteStoreRepository
import com.nuvo.backend.features.social.repository.ReviewRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class SocialServiceTests {
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var favouriteStoreRepository: FavouriteStoreRepository
    private lateinit var favouriteProductRepository: FavouriteProductRepository
    private lateinit var userRepository: UserRepository
    private lateinit var storeRepository: StoreRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var service: SocialService

    private val firebaseUid = "firebase-user"
    private val user = TestFixtures.user(firebaseUid = firebaseUid)
    private val store = TestFixtures.store()

    @BeforeEach
    fun setUp() {
        reviewRepository = Mockito.mock(ReviewRepository::class.java)
        favouriteStoreRepository = Mockito.mock(FavouriteStoreRepository::class.java)
        favouriteProductRepository = Mockito.mock(FavouriteProductRepository::class.java)
        userRepository = Mockito.mock(UserRepository::class.java)
        storeRepository = Mockito.mock(StoreRepository::class.java)
        productRepository = Mockito.mock(ProductRepository::class.java)
        orderRepository = Mockito.mock(OrderRepository::class.java)
        service = SocialService(
            reviewRepository,
            favouriteStoreRepository,
            favouriteProductRepository,
            userRepository,
            storeRepository,
            productRepository,
            orderRepository
        )
    }

    @Test
    fun `submitReview creates review and recalculates store average`() {
        val savedReview = Review(id = UUID.randomUUID(), user = user, store = store, rating = 5, comment = "Great")
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(storeRepository.findById(store.id!!)).thenReturn(Optional.of(store))
        `when`(reviewRepository.findByUserIdAndStoreId(user.id!!, store.id!!)).thenReturn(null)
        `when`(reviewRepository.save(any())).thenReturn(savedReview)
        `when`(reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(store.id!!)).thenReturn(
            listOf(savedReview, Review(id = UUID.randomUUID(), user = user, store = store, rating = 3))
        )
        `when`(storeRepository.save(any())).thenAnswer { it.arguments[0] }

        val dto = service.submitReview(firebaseUid, store.id!!, SubmitReviewRequest(5, "Great"))

        assertEquals(5, dto.rating)
        assertEquals(4.0, store.averageRating)
        verify(storeRepository).save(store)
    }

    @Test
    fun `submitOrderReview updates existing store review and links latest order`() {
        val order = TestFixtures.order(user = user, store = store)
        val existing = Review(id = UUID.randomUUID(), user = user, store = store, rating = 2, comment = "Old")
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(orderRepository.findById(order.id!!)).thenReturn(Optional.of(order))
        `when`(reviewRepository.findByUserIdAndStoreId(user.id!!, store.id!!)).thenReturn(existing)
        `when`(reviewRepository.save(existing)).thenReturn(existing)
        `when`(storeRepository.findById(store.id!!)).thenReturn(Optional.of(store))
        `when`(reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(store.id!!)).thenReturn(listOf(existing))

        val dto = service.submitOrderReview(firebaseUid, order.id!!, SubmitReviewRequest(4, "Updated"))

        assertEquals(4, dto.rating)
        assertEquals("Updated", dto.comment)
        assertEquals(order, existing.order)
    }

    @Test
    fun `submitOrderReview rejects another user's order`() {
        val order = TestFixtures.order(user = TestFixtures.user(firebaseUid = "other"), store = store)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(orderRepository.findById(order.id!!)).thenReturn(Optional.of(order))

        assertThrows<UnauthorizedException> {
            service.submitOrderReview(firebaseUid, order.id!!, SubmitReviewRequest(4, "Nope"))
        }
    }

    @Test
    fun `getOrderTracking returns coordinates for owner and rejects non-owner`() {
        val order = TestFixtures.order(user = user, store = store)
        order.currentLat = 1.2
        order.currentLng = 3.4
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(orderRepository.findById(order.id!!)).thenReturn(Optional.of(order))

        val result = service.getOrderTracking(firebaseUid, order.id!!)

        assertEquals(1.2, result["lat"])
        assertEquals(3.4, result["lng"])

        val otherOrder = TestFixtures.order(user = TestFixtures.user(firebaseUid = "other"), store = store)
        `when`(orderRepository.findById(otherOrder.id!!)).thenReturn(Optional.of(otherOrder))
        assertThrows<UnauthorizedException> {
            service.getOrderTracking(firebaseUid, otherOrder.id!!)
        }
    }

    @Test
    fun `toggleFavouriteStore creates and deletes favourite`() {
        val favouriteId = FavouriteStoreId(user.id!!, store.id!!)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(storeRepository.findById(store.id!!)).thenReturn(Optional.of(store))
        `when`(favouriteStoreRepository.existsById(favouriteId)).thenReturn(false, true)
        `when`(favouriteStoreRepository.save(any())).thenAnswer { it.arguments[0] }

        service.toggleFavouriteStore(firebaseUid, store.id!!)
        service.toggleFavouriteStore(firebaseUid, store.id!!)

        val captor = ArgumentCaptor.forClass(FavouriteStore::class.java)
        verify(favouriteStoreRepository).save(captor.capture())
        assertEquals(favouriteId, captor.value.id)
        verify(favouriteStoreRepository).deleteById(favouriteId)
    }

    @Test
    fun `toggleFavouriteProduct creates and deletes favourite`() {
        val product = TestFixtures.product(store = store)
        val favouriteId = FavouriteProductId(user.id!!, product.id!!)
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(productRepository.findById(product.id!!)).thenReturn(Optional.of(product))
        `when`(favouriteProductRepository.existsById(favouriteId)).thenReturn(false, true)
        `when`(favouriteProductRepository.save(any())).thenAnswer { it.arguments[0] }

        service.toggleFavouriteProduct(firebaseUid, product.id!!)
        service.toggleFavouriteProduct(firebaseUid, product.id!!)

        val captor = ArgumentCaptor.forClass(FavouriteProduct::class.java)
        verify(favouriteProductRepository).save(captor.capture())
        assertEquals(favouriteId, captor.value.id)
        verify(favouriteProductRepository).deleteById(favouriteId)
    }

    @Test
    fun `getStoreReviews returns paged review DTOs`() {
        val review = Review(id = UUID.randomUUID(), user = user, store = store, rating = 5, comment = "Good")
        val pageable = PageRequest.of(0, 10)
        `when`(reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(store.id!!, pageable))
            .thenReturn(PageImpl(listOf(review), pageable, 1))

        val page = service.getStoreReviews(store.id!!, pageable)

        assertEquals(1, page.totalElements)
        assertEquals("Good", page.content.single().comment)
    }
}
