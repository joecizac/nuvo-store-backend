package com.nuvo.backend

import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.catalog.domain.Category
import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.catalog.domain.SKU
import com.nuvo.backend.features.catalog.domain.SubCategory
import com.nuvo.backend.features.order.domain.Order
import com.nuvo.backend.features.store.domain.Chain
import com.nuvo.backend.features.store.domain.Store
import com.nuvo.backend.features.user.domain.User
import com.nuvo.backend.features.user.domain.UserAddress
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.UUID

object TestFixtures {
    fun user(id: UUID = UUID.randomUUID(), firebaseUid: String = "firebase-user") = User(
        id = id,
        firebaseUid = firebaseUid,
        email = "$firebaseUid@example.com",
        name = "Test User",
        fcmToken = "fcm-token"
    )

    fun chain(id: UUID = UUID.randomUUID()) = Chain(
        id = id,
        name = "Chain",
        description = "Description",
        logoUrl = "logo.png",
        bannerUrl = "banner.png"
    )

    fun store(id: UUID = UUID.randomUUID(), name: String = "Store", chain: Chain? = null) = Store(
        id = id,
        chain = chain,
        name = name,
        description = "Store description",
        contactNumber = "+123456789",
        logoUrl = "store-logo.png",
        bannerUrl = "store-banner.png",
        location = GeometryUtil.createPoint(-33.9, 18.4),
        address = "Store Address",
        averageRating = 4.0.toDouble(),
        cuisine = "grocery",
        priceRange = 2,
        deliveryFee = BigDecimal("3.50")
    )

    fun category(id: UUID = UUID.randomUUID(), store: Store = store()) = Category(
        id = id,
        store = store,
        name = "Category",
        imageUrl = "category.png"
    )

    fun subCategory(id: UUID = UUID.randomUUID(), category: Category = category()) = SubCategory(
        id = id,
        category = category,
        name = "Sub Category"
    )

    fun product(id: UUID = UUID.randomUUID(), store: Store = store(), subCategory: SubCategory = subCategory()) = Product(
        id = id,
        store = store,
        subCategory = subCategory,
        name = "Product",
        description = "Product description",
        imageUrl = "product.png"
    )

    fun sku(
        id: UUID = UUID.randomUUID(),
        product: Product = product(),
        name: String = "SKU",
        originalPrice: String = "10.00",
        discountedPrice: String? = null
    ) = SKU(
        id = id,
        product = product,
        name = name,
        imageUrl = "sku.png",
        originalPrice = BigDecimal(originalPrice),
        discountedPrice = discountedPrice?.let(::BigDecimal)
    )

    fun address(id: UUID = UUID.randomUUID(), user: User = user(), isDefault: Boolean = false) = UserAddress(
        id = id,
        user = user,
        title = "Home",
        fullAddress = "123 Test Street",
        location = GeometryUtil.createPoint(-33.9, 18.4),
        isDefault = isDefault
    )

    fun order(id: UUID = UUID.randomUUID(), user: User = user(), store: Store = store()) = Order(
        id = id,
        user = user,
        store = store,
        totalAmount = BigDecimal("17.00"),
        deliveryAddressSnapshot = "{}"
    )

    fun <T> any(): T = Mockito.any<T>()
}
