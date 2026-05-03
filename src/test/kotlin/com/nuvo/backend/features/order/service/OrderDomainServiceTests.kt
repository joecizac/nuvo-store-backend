package com.nuvo.backend.features.order.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.exception.UnauthorizedException
import com.nuvo.backend.common.exception.ValidationException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.SKU
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.catalog.repository.SKURepository
import com.nuvo.backend.features.order.domain.Cart
import com.nuvo.backend.features.order.domain.CartItem
import com.nuvo.backend.features.order.domain.Order
import com.nuvo.backend.features.order.domain.OrderItem
import com.nuvo.backend.features.order.domain.OrderStatus
import com.nuvo.backend.features.order.domain.OrderStatusChangedEvent
import com.nuvo.backend.features.order.dto.AddToCartRequest
import com.nuvo.backend.features.order.dto.CheckoutRequest
import com.nuvo.backend.features.order.repository.CartRepository
import com.nuvo.backend.features.order.repository.OrderRepository
import com.nuvo.backend.features.store.domain.Store
import com.nuvo.backend.features.user.domain.User
import com.nuvo.backend.features.user.domain.UserAddress
import com.nuvo.backend.features.user.repository.UserAddressRepository
import com.nuvo.backend.features.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OrderDomainServiceTests {

    private lateinit var cartRepository: CartRepository
    private lateinit var userRepository: UserRepository
    private lateinit var skuRepository: SKURepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var addressRepository: UserAddressRepository
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var cartService: CartService
    private lateinit var orderService: OrderService

    private val firebaseUid = "firebase-user"
    private val user = user()
    private val storeA = store(name = "Store A")
    private val storeB = store(name = "Store B")
    private val skuA = sku(store = storeA, name = "SKU A", originalPrice = "10.00", discountedPrice = "8.50")
    private val skuB = sku(store = storeB, name = "SKU B", originalPrice = "12.00")

    @BeforeEach
    fun setUp() {
        cartRepository = Mockito.mock(CartRepository::class.java)
        userRepository = Mockito.mock(UserRepository::class.java)
        skuRepository = Mockito.mock(SKURepository::class.java)
        orderRepository = Mockito.mock(OrderRepository::class.java)
        addressRepository = Mockito.mock(UserAddressRepository::class.java)
        eventPublisher = Mockito.mock(ApplicationEventPublisher::class.java)

        cartService = CartService(cartRepository, userRepository, skuRepository)
        orderService = OrderService(
            cartService = cartService,
            orderRepository = orderRepository,
            userRepository = userRepository,
            addressRepository = addressRepository,
            objectMapper = ObjectMapper(),
            eventPublisher = eventPublisher
        )
    }

    @Test
    fun `getOrCreateCart creates a cart when user has none`() {
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(null)
        `when`(cartRepository.save(any())).thenAnswer { it.arguments[0] }

        val cart = cartService.getOrCreateCart(firebaseUid)

        assertEquals(user, cart.user)
        assertNull(cart.store)
        assertTrue(cart.items.isEmpty())
        verify(cartRepository).save(any())
    }

    @Test
    fun `getOrCreateCart fails when user does not exist`() {
        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(null)

        assertThrows<ResourceNotFoundException> {
            cartService.getOrCreateCart(firebaseUid)
        }

        verify(cartRepository, never()).save(any())
    }

    @Test
    fun `addItemToCart increments an existing SKU and totals discounted price`() {
        val cart = Cart(id = UUID.randomUUID(), user = user, store = storeA)
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = skuA, quantity = 1))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(skuRepository.findById(skuA.id!!)).thenReturn(Optional.of(skuA))
        `when`(cartRepository.save(any())).thenAnswer { it.arguments[0] }

        val dto = cartService.addItemToCart(firebaseUid, AddToCartRequest(skuA.id!!, 2))

        assertEquals(storeA.id, dto.storeId)
        assertEquals(1, dto.items.size)
        assertEquals(3, dto.items.single().quantity)
        assertEquals(BigDecimal("25.50"), dto.totalAmount)
    }

    @Test
    fun `addItemToCart enforces single-store rule by clearing old items`() {
        val cart = Cart(id = UUID.randomUUID(), user = user, store = storeA)
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = skuA, quantity = 2))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(skuRepository.findById(skuB.id!!)).thenReturn(Optional.of(skuB))
        `when`(cartRepository.save(any())).thenAnswer {
            val input = it.arguments[0] as Cart
            val persisted = Cart(id = input.id ?: UUID.randomUUID(), user = input.user, store = input.store)
            input.items.forEach { item ->
                persisted.items.add(
                    CartItem(
                        id = item.id ?: UUID.randomUUID(),
                        cart = persisted,
                        sku = item.sku,
                        quantity = item.quantity
                    )
                )
            }
            persisted
        }

        val dto = cartService.addItemToCart(firebaseUid, AddToCartRequest(skuB.id!!, 1))

        assertEquals(storeB.id, dto.storeId)
        assertEquals(1, dto.items.size)
        assertEquals(skuB.id, dto.items.single().skuId)
        assertEquals(BigDecimal("12.00"), dto.totalAmount)
    }

    @Test
    fun `updateItemQuantity removes item and clears store when quantity is zero`() {
        val itemId = UUID.randomUUID()
        val cart = Cart(id = UUID.randomUUID(), user = user, store = storeA)
        cart.items.add(CartItem(id = itemId, cart = cart, sku = skuA, quantity = 2))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(cartRepository.save(any())).thenAnswer { it.arguments[0] }

        val dto = cartService.updateItemQuantity(firebaseUid, itemId, 0)

        assertNull(dto.storeId)
        assertTrue(dto.items.isEmpty())
        assertEquals(BigDecimal.ZERO, dto.totalAmount)
    }

    @Test
    fun `clearCart removes all items and store association`() {
        val cart = Cart(user = user, store = storeA)
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = skuA, quantity = 2))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(cartRepository.save(any())).thenAnswer { it.arguments[0] }

        cartService.clearCart(firebaseUid)

        assertNull(cart.store)
        assertTrue(cart.items.isEmpty())
        verify(cartRepository).save(cart)
    }

    @Test
    fun `checkout creates an order with totals snapshots items clears cart and publishes event`() {
        val address = address(user)
        val cart = Cart(id = UUID.randomUUID(), user = user, store = storeA)
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = skuA, quantity = 2))
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = sku(storeA, originalPrice = "5.00"), quantity = 3))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(cartRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(addressRepository.findById(address.id!!)).thenReturn(Optional.of(address))
        `when`(orderRepository.save(any())).thenAnswer {
            val input = it.arguments[0] as Order
            val saved = Order(
                id = UUID.randomUUID(),
                user = input.user,
                store = input.store,
                status = input.status,
                totalAmount = input.totalAmount,
                deliveryAddressSnapshot = input.deliveryAddressSnapshot
            )
            input.items.forEach { item ->
                saved.items.add(
                    OrderItem(
                        id = UUID.randomUUID(),
                        order = saved,
                        sku = item.sku,
                        snapshotPrice = item.snapshotPrice,
                        quantity = item.quantity
                    )
                )
            }
            saved
        }

        val dto = orderService.checkout(firebaseUid, CheckoutRequest(address.id!!))

        assertEquals(OrderStatus.PENDING, dto.status)
        assertEquals(BigDecimal("32.00"), dto.totalAmount)
        assertEquals(2, dto.items.size)
        assertTrue(dto.deliveryAddressSnapshot.contains("Home"))
        assertTrue(dto.deliveryAddressSnapshot.contains("123 Test Street"))
        assertTrue(cart.items.isEmpty())
        assertNull(cart.store)

        val eventCaptor = ArgumentCaptor.forClass(OrderStatusChangedEvent::class.java)
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals(OrderStatus.PENDING, eventCaptor.value.newStatus)
        assertEquals(user.id, eventCaptor.value.userId)
    }

    @Test
    fun `checkout rejects an empty cart`() {
        val cart = Cart(user = user)

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)

        assertThrows<ValidationException> {
            orderService.checkout(firebaseUid, CheckoutRequest(UUID.randomUUID()))
        }

        verify(orderRepository, never()).save(any())
        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `checkout rejects addresses owned by another user`() {
        val otherUser = user(firebaseUid = "other")
        val otherAddress = address(otherUser)
        val cart = Cart(user = user, store = storeA)
        cart.items.add(CartItem(id = UUID.randomUUID(), cart = cart, sku = skuA, quantity = 1))

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(cartRepository.findByUserId(user.id!!)).thenReturn(cart)
        `when`(addressRepository.findById(otherAddress.id!!)).thenReturn(Optional.of(otherAddress))

        assertThrows<ResourceNotFoundException> {
            orderService.checkout(firebaseUid, CheckoutRequest(otherAddress.id!!))
        }

        verify(orderRepository, never()).save(any())
    }

    @Test
    fun `getOrderDetails rejects orders owned by another user`() {
        val otherUser = user(firebaseUid = "other")
        val order = order(user = otherUser, store = storeA)

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(orderRepository.findById(order.id!!)).thenReturn(Optional.of(order))

        assertThrows<UnauthorizedException> {
            orderService.getOrderDetails(firebaseUid, order.id!!)
        }
    }

    @Test
    fun `getOrderHistory returns mapped orders for current user`() {
        val order = order(user = user, store = storeA)
        order.items.add(OrderItem(order = order, sku = skuA, snapshotPrice = BigDecimal("8.50"), quantity = 2))
        val pageable = PageRequest.of(0, 10)

        `when`(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(user)
        `when`(orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.id!!, pageable))
            .thenReturn(PageImpl(listOf(order), pageable, 1))

        val page = orderService.getOrderHistory(firebaseUid, pageable)

        assertEquals(1, page.totalElements)
        assertEquals(order.id, page.content.single().id)
        assertEquals(storeA.id, page.content.single().storeId)
        assertEquals(1, page.content.single().items.size)
    }

    private fun user(id: UUID = UUID.randomUUID(), firebaseUid: String = "firebase-user") = User(
        id = id,
        firebaseUid = firebaseUid,
        email = "$firebaseUid@example.com",
        name = "Test User",
        fcmToken = "fcm-token"
    )

    private fun store(id: UUID = UUID.randomUUID(), name: String = "Store") = Store(
        id = id,
        name = name,
        location = GeometryUtil.createPoint(-33.9, 18.4),
        address = "Store Address"
    )

    private fun category(store: Store) = Category(
        id = UUID.randomUUID(),
        store = store,
        name = "Category"
    )

    private fun subCategory(store: Store) = SubCategory(
        id = UUID.randomUUID(),
        category = category(store),
        name = "Sub Category"
    )

    private fun product(store: Store) = Product(
        id = UUID.randomUUID(),
        store = store,
        subCategory = subCategory(store),
        name = "Product"
    )

    private fun sku(
        store: Store,
        name: String = "SKU",
        originalPrice: String = "10.00",
        discountedPrice: String? = null
    ) = SKU(
        id = UUID.randomUUID(),
        product = product(store),
        name = name,
        originalPrice = BigDecimal(originalPrice),
        discountedPrice = discountedPrice?.let(::BigDecimal)
    )

    private fun address(user: User) = UserAddress(
        id = UUID.randomUUID(),
        user = user,
        title = "Home",
        fullAddress = "123 Test Street",
        location = GeometryUtil.createPoint(-33.9, 18.4),
        isDefault = true
    )

    private fun order(user: User, store: Store) = Order(
        id = UUID.randomUUID(),
        user = user,
        store = store,
        totalAmount = BigDecimal("17.00"),
        deliveryAddressSnapshot = "{}"
    )

    private fun <T> any(): T = Mockito.any<T>()
}
