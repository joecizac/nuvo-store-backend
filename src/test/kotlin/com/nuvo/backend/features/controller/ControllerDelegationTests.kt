package com.nuvo.backend.features.controller

import com.nuvo.backend.TestFixtures
import com.nuvo.backend.features.admin.controller.AdminController
import com.nuvo.backend.features.admin.dto.AdminCategoryRequest
import com.nuvo.backend.features.admin.dto.AdminChainRequest
import com.nuvo.backend.features.admin.dto.AdminProductRequest
import com.nuvo.backend.features.admin.dto.AdminSkuRequest
import com.nuvo.backend.features.admin.dto.AdminStoreRequest
import com.nuvo.backend.features.admin.dto.AdminSubCategoryRequest
import com.nuvo.backend.features.admin.service.AdminService
import com.nuvo.backend.features.catalog.controller.CatalogController
import com.nuvo.backend.features.catalog.dto.CategoryDTO
import com.nuvo.backend.features.catalog.dto.ProductDTO
import com.nuvo.backend.features.catalog.service.CatalogService
import com.nuvo.backend.features.order.controller.CartController
import com.nuvo.backend.features.order.controller.OrderController
import com.nuvo.backend.features.order.domain.OrderStatus
import com.nuvo.backend.features.order.dto.AddToCartRequest
import com.nuvo.backend.features.order.dto.CartDTO
import com.nuvo.backend.features.order.dto.CheckoutRequest
import com.nuvo.backend.features.order.dto.OrderDTO
import com.nuvo.backend.features.order.service.CartService
import com.nuvo.backend.features.order.service.OrderService
import com.nuvo.backend.features.social.controller.SocialController
import com.nuvo.backend.features.social.dto.ReviewDTO
import com.nuvo.backend.features.social.dto.SubmitReviewRequest
import com.nuvo.backend.features.social.service.SocialService
import com.nuvo.backend.features.store.controller.StoreController
import com.nuvo.backend.features.store.dto.ChainDTO
import com.nuvo.backend.features.store.dto.StoreDTO
import com.nuvo.backend.features.store.service.StoreService
import com.nuvo.backend.features.user.controller.UserController
import com.nuvo.backend.features.user.dto.AddressDTO
import com.nuvo.backend.features.user.dto.SyncUserRequest
import com.nuvo.backend.features.user.dto.UserProfileDTO
import com.nuvo.backend.features.user.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.security.Principal
import java.util.UUID
import kotlin.test.assertEquals

class ControllerDelegationTests {
    private val principal = Principal { "firebase-user" }

    @Test
    fun `UserController delegates profile and address calls`() {
        val service = Mockito.mock(UserService::class.java)
        val controller = UserController(service)
        val userDto = UserProfileDTO(UUID.randomUUID(), "firebase-user", "u@example.com", "User", null, null, null)
        val addressId = UUID.randomUUID()
        val address = AddressDTO(addressId, "Home", "Street", -33.9, 18.4, true)
        `when`(service.getUserProfile("firebase-user")).thenReturn(userDto)
        `when`(service.syncUser("firebase-user", SyncUserRequest("User", "u@example.com"))).thenReturn(userDto)
        `when`(service.getUserAddresses("firebase-user")).thenReturn(listOf(address))
        `when`(service.addAddress("firebase-user", address)).thenReturn(address)
        `when`(service.updateAddress("firebase-user", addressId, address)).thenReturn(address)
        `when`(service.setDefaultAddress("firebase-user", addressId)).thenReturn(address)

        assertEquals(userDto, controller.getMyProfile(principal))
        assertEquals(userDto, controller.syncProfile(principal, SyncUserRequest("User", "u@example.com")))
        controller.updateFcmToken(principal, "token")
        assertEquals(listOf(address), controller.getMyAddresses(principal))
        assertEquals(address, controller.addAddress(principal, address))
        assertEquals(address, controller.updateAddress(principal, addressId, address))
        controller.deleteAddress(principal, addressId)
        assertEquals(address, controller.setDefaultAddress(principal, addressId))
        verify(service).updateFcmToken("firebase-user", "token")
        verify(service).deleteAddress("firebase-user", addressId)
    }

    @Test
    fun `CartController and OrderController delegate order flows`() {
        val cartService = Mockito.mock(CartService::class.java)
        val orderService = Mockito.mock(OrderService::class.java)
        val cartController = CartController(cartService)
        val orderController = OrderController(orderService)
        val skuId = UUID.randomUUID()
        val cart = CartDTO(UUID.randomUUID(), UUID.randomUUID(), emptyList(), BigDecimal.ZERO)
        val order = OrderDTO(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.PENDING, BigDecimal.ZERO, "{}", emptyList(), "now")
        val pageable = PageRequest.of(0, 10)
        val checkout = CheckoutRequest(UUID.randomUUID())
        `when`(cartService.getCartDTO("firebase-user")).thenReturn(cart)
        `when`(cartService.addItemToCart("firebase-user", AddToCartRequest(skuId, 1))).thenReturn(cart)
        `when`(cartService.updateItemQuantity("firebase-user", skuId, 2)).thenReturn(cart)
        `when`(orderService.checkout("firebase-user", checkout)).thenReturn(order)
        `when`(orderService.getOrderHistory("firebase-user", pageable)).thenReturn(PageImpl(listOf(order), pageable, 1))
        `when`(orderService.getOrderDetails("firebase-user", order.id)).thenReturn(order)

        assertEquals(cart, cartController.getCart(principal))
        assertEquals(cart, cartController.addItem(principal, AddToCartRequest(skuId, 1)))
        assertEquals(cart, cartController.updateQuantity(principal, skuId, 2))
        cartController.clearCart(principal)
        assertEquals(order, orderController.checkout(principal, checkout))
        assertEquals(1, orderController.getOrderHistory(principal, pageable).totalElements)
        assertEquals(order, orderController.getOrderDetails(principal, order.id))
        verify(cartService).clearCart("firebase-user")
    }

    @Test
    fun `StoreController and CatalogController delegate read APIs`() {
        val storeService = Mockito.mock(StoreService::class.java)
        val catalogService = Mockito.mock(CatalogService::class.java)
        val storeController = StoreController(storeService)
        val catalogController = CatalogController(catalogService)
        val pageable = PageRequest.of(0, 10)
        val storeDto = storeDto()
        val chainDto = ChainDTO(UUID.randomUUID(), "Chain", null, null, null)
        val categoryDto = CategoryDTO(UUID.randomUUID(), "Category", null)
        val productDto = ProductDTO(UUID.randomUUID(), storeDto.id, UUID.randomUUID(), "Product", null, null, true)
        `when`(storeService.searchStores(-33.9, 18.4, 1000.0, "q", pageable)).thenReturn(PageImpl(listOf(storeDto), pageable, 1))
        `when`(storeService.getNearbyStoresFiltered(-33.9, 18.4, 1000.0, null, null, false, pageable)).thenReturn(PageImpl(listOf(storeDto), pageable, 1))
        `when`(storeService.getStore(storeDto.id)).thenReturn(storeDto)
        `when`(storeService.getAllChains()).thenReturn(listOf(chainDto))
        `when`(storeService.getStoresByChain(chainDto.id)).thenReturn(listOf(storeDto))
        `when`(catalogService.getStoreCategories(storeDto.id)).thenReturn(listOf(categoryDto))
        `when`(catalogService.getStoreProducts(storeDto.id, null, pageable)).thenReturn(PageImpl(listOf(productDto), pageable, 1))
        `when`(catalogService.getProduct(productDto.id)).thenReturn(productDto)

        assertEquals(1, storeController.search(-33.9, 18.4, 1000.0, "q", pageable).totalElements)
        assertEquals(1, storeController.getStores(-33.9, 18.4, 1000.0, null, null, false, pageable).totalElements)
        assertEquals(storeDto, storeController.getStore(storeDto.id))
        assertEquals(listOf(chainDto), storeController.getChains())
        assertEquals(listOf(storeDto), storeController.getStoresByChain(chainDto.id))
        assertEquals(listOf(categoryDto), catalogController.getStoreCategories(storeDto.id))
        assertEquals(1, catalogController.getStoreProducts(storeDto.id, null, pageable).totalElements)
        assertEquals(productDto, catalogController.getProduct(productDto.id))
    }

    @Test
    fun `SocialController delegates social APIs`() {
        val service = Mockito.mock(SocialService::class.java)
        val controller = SocialController(service)
        val storeId = UUID.randomUUID()
        val orderId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)
        val review = ReviewDTO(UUID.randomUUID(), UUID.randomUUID(), "User", orderId, 5, "Great", "now")
        `when`(service.getStoreReviews(storeId, pageable)).thenReturn(PageImpl(listOf(review), pageable, 1))
        `when`(service.submitReview("firebase-user", storeId, SubmitReviewRequest(5, "Great"))).thenReturn(review)
        `when`(service.submitOrderReview("firebase-user", orderId, SubmitReviewRequest(5, "Great"))).thenReturn(review)
        `when`(service.getOrderTracking("firebase-user", orderId)).thenReturn(mapOf("lat" to 1.0, "lng" to 2.0))

        assertEquals(1, controller.getReviews(storeId, pageable).totalElements)
        assertEquals(review, controller.submitReview(principal, storeId, SubmitReviewRequest(5, "Great")))
        assertEquals(review, controller.submitOrderReview(principal, orderId, SubmitReviewRequest(5, "Great")))
        assertEquals(1.0, controller.getOrderTracking(principal, orderId)["lat"])
        controller.toggleFavouriteStore(principal, storeId)
        controller.toggleFavouriteProduct(principal, productId)
        verify(service).toggleFavouriteStore("firebase-user", storeId)
        verify(service).toggleFavouriteProduct("firebase-user", productId)
    }

    @Test
    fun `AdminController delegates all create APIs`() {
        val service = Mockito.mock(AdminService::class.java)
        val controller = AdminController(service)
        val store = TestFixtures.store()
        val chain = TestFixtures.chain()
        val category = TestFixtures.category(store = store)
        val subCategory = TestFixtures.subCategory(category = category)
        val product = TestFixtures.product(store = store, subCategory = subCategory)
        val sku = TestFixtures.sku(product = product)
        `when`(service.createChain(AdminChainRequest("Chain", null, null, null))).thenReturn(chain)
        `when`(service.createStore(AdminStoreRequest(null, "Store", null, null, null, null, -33.9, 18.4, "Address"))).thenReturn(store)
        `when`(service.createCategory(store.id!!, AdminCategoryRequest("Category", null))).thenReturn(category)
        `when`(service.createSubCategory(category.id!!, AdminSubCategoryRequest("Sub"))).thenReturn(subCategory)
        `when`(service.createProduct(store.id!!, AdminProductRequest(subCategory.id!!, "Product", null, null))).thenReturn(product)
        `when`(service.createSku(product.id!!, AdminSkuRequest("SKU", null, BigDecimal.TEN, null))).thenReturn(sku)

        assertEquals(chain, controller.createChain(AdminChainRequest("Chain", null, null, null)))
        assertEquals(store, controller.createStore(AdminStoreRequest(null, "Store", null, null, null, null, -33.9, 18.4, "Address")))
        assertEquals(category, controller.createCategory(store.id!!, AdminCategoryRequest("Category", null)))
        assertEquals(subCategory, controller.createSubCategory(category.id!!, AdminSubCategoryRequest("Sub")))
        assertEquals(product, controller.createProduct(store.id!!, AdminProductRequest(subCategory.id!!, "Product", null, null)))
        assertEquals(sku, controller.createSku(product.id!!, AdminSkuRequest("SKU", null, BigDecimal.TEN, null)))
    }

    private fun storeDto() = StoreDTO(
        id = UUID.randomUUID(),
        chainId = null,
        name = "Store",
        description = null,
        contactNumber = null,
        logoUrl = null,
        bannerUrl = null,
        latitude = -33.9,
        longitude = 18.4,
        address = "Address",
        averageRating = 4.0,
        cuisine = null,
        priceRange = 1,
        deliveryFee = BigDecimal.ZERO,
        openAt = null,
        closeAt = null
    )
}
