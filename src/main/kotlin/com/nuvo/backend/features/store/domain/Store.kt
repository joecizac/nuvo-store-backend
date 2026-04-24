package com.nuvo.backend.features.store.domain

import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "stores")
@EntityListeners(AuditingEntityListener::class)
class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id")
    var chain: Chain? = null,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(name = "contact_number")
    var contactNumber: String? = null,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    @Column(name = "banner_url")
    var bannerUrl: String? = null,

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    var location: Point,

    @Column(nullable = false)
    var address: String,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "average_rating")
    var averageRating: Double = 0.0,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
