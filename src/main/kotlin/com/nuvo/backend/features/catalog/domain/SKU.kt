package com.nuvo.backend.features.catalog.domain

import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "skus")
@EntityListeners(AuditingEntityListener::class)
class SKU(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    var name: String,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "original_price", nullable = false)
    var originalPrice: BigDecimal,

    @Column(name = "discounted_price")
    var discountedPrice: BigDecimal? = null,

    @Column(name = "is_available")
    var isAvailable: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
