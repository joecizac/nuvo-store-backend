package com.nuvo.backend.features.store.repository

import com.nuvo.backend.features.store.domain.Store
import jakarta.persistence.criteria.Predicate
import org.locationtech.jts.geom.Point
import org.springframework.data.jpa.domain.Specification
import java.time.LocalTime

object StoreSpecifications {

    fun isNearby(point: Point, radiusInMeters: Double): Specification<Store> {
        return Specification { root, _, cb ->
            cb.isTrue(
                cb.function(
                    "ST_DWithin",
                    Boolean::class.java,
                    cb.function("CAST", Any::class.java, root.get<Any>("location"), cb.literal("geography")),
                    cb.function("CAST", Any::class.java, cb.literal(point), cb.literal("geography")),
                    cb.literal(radiusInMeters)
                )
            )
        }
    }

    fun hasCuisine(cuisine: String?): Specification<Store> {
        return Specification { root, _, cb ->
            cuisine?.let { cb.equal(cb.lower(root.get("cuisine")), it.lowercase()) }
        }
    }

    fun hasPriceRange(priceRange: Int?): Specification<Store> {
        return Specification { root, _, cb ->
            priceRange?.let { cb.equal(root.get<Int>("priceRange"), it) }
        }
    }

    fun isOpenNow(): Specification<Store> {
        return Specification { root, _, cb ->
            val now = LocalTime.now()
            cb.and(
                cb.lessThanOrEqualTo(root.get("openAt"), now),
                cb.greaterThanOrEqualTo(root.get("closeAt"), now)
            )
        }
    }

    fun isActive(): Specification<Store> {
        return Specification { root, _, cb ->
            cb.isTrue(root.get("isActive"))
        }
    }
}
