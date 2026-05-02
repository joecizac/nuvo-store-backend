package com.nuvo.backend.features.social.domain

import com.nuvo.backend.features.catalog.domain.Product
import com.nuvo.backend.features.order.domain.Order
import com.nuvo.backend.features.store.domain.Store
import com.nuvo.backend.features.user.domain.User
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener::class)
class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null,

    @Column(nullable = false)
    var rating: Int,

    @Column(nullable = true)
    var comment: String? = null,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

@Embeddable
data class FavouriteStoreId(
    @Column(name = "user_id")
    val userId: UUID,
    @Column(name = "store_id")
    val storeId: UUID
) : Serializable

@Entity
@Table(name = "favourite_stores")
@EntityListeners(AuditingEntityListener::class)
class FavouriteStore(
    @EmbeddedId
    val id: FavouriteStoreId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    val store: Store,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
)

@Embeddable
data class FavouriteProductId(
    @Column(name = "user_id")
    val userId: UUID,
    @Column(name = "product_id")
    val productId: UUID
) : Serializable

@Entity
@Table(name = "favourite_products")
@EntityListeners(AuditingEntityListener::class)
class FavouriteProduct(
    @EmbeddedId
    val id: FavouriteProductId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    val product: Product,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
)
