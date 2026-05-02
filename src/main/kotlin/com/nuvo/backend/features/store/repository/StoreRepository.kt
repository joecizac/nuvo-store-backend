package com.nuvo.backend.features.store.repository

import com.nuvo.backend.features.store.domain.Store
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface StoreRepository : JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {
    
    @Query(
        value = """
        SELECT * FROM stores s 
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius) 
        AND s.is_active = true 
        AND (:searchQuery IS NULL OR s.name ILIKE CONCAT('%', :searchQuery, '%'))
        """,
        countQuery = """
        SELECT count(*) FROM stores s 
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius) 
        AND s.is_active = true 
        AND (:searchQuery IS NULL OR s.name ILIKE CONCAT('%', :searchQuery, '%'))
        """,
        nativeQuery = true
    )
    fun findNearbyStores(
        @Param("point") point: Point,
        @Param("radius") radiusInMeters: Double,
        @Param("searchQuery") searchQuery: String?,
        pageable: Pageable
    ): Page<Store>

    @Query(
        value = """
        SELECT DISTINCT s.* FROM stores s
        LEFT JOIN products p ON s.id = p.store_id
        LEFT JOIN skus sk ON p.id = sk.product_id
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius)
        AND s.is_active = true
        AND (
            s.name ILIKE CONCAT('%', :query, '%') OR 
            s.description ILIKE CONCAT('%', :query, '%') OR
            p.name ILIKE CONCAT('%', :query, '%') OR
            sk.name ILIKE CONCAT('%', :query, '%')
        )
        """,
        countQuery = """
        SELECT count(DISTINCT s.id) FROM stores s
        LEFT JOIN products p ON s.id = p.store_id
        LEFT JOIN skus sk ON p.id = sk.product_id
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius)
        AND s.is_active = true
        AND (
            s.name ILIKE CONCAT('%', :query, '%') OR 
            s.description ILIKE CONCAT('%', :query, '%') OR
            p.name ILIKE CONCAT('%', :query, '%') OR
            sk.name ILIKE CONCAT('%', :query, '%')
        )
        """,
        nativeQuery = true
    )
    fun searchStores(
        @Param("point") point: Point,
        @Param("radius") radiusInMeters: Double,
        @Param("query") query: String,
        pageable: Pageable
    ): Page<Store>

    @Query(
        value = """
        SELECT * FROM stores s
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius)
        AND s.is_active = true
        AND (:cuisine IS NULL OR LOWER(s.cuisine) = LOWER(CAST(:cuisine AS text)))
        AND (:priceRange IS NULL OR s.price_range = :priceRange)
        AND (
            :openNow = false OR (
                s.open_at IS NOT NULL
                AND s.close_at IS NOT NULL
                AND (
                    (s.open_at <= s.close_at AND LOCALTIME BETWEEN s.open_at AND s.close_at)
                    OR (s.open_at > s.close_at AND (LOCALTIME >= s.open_at OR LOCALTIME <= s.close_at))
                )
            )
        )
        """,
        countQuery = """
        SELECT count(*) FROM stores s
        WHERE ST_DWithin(CAST(s.location AS geography), CAST(:point AS geography), :radius)
        AND s.is_active = true
        AND (:cuisine IS NULL OR LOWER(s.cuisine) = LOWER(CAST(:cuisine AS text)))
        AND (:priceRange IS NULL OR s.price_range = :priceRange)
        AND (
            :openNow = false OR (
                s.open_at IS NOT NULL
                AND s.close_at IS NOT NULL
                AND (
                    (s.open_at <= s.close_at AND LOCALTIME BETWEEN s.open_at AND s.close_at)
                    OR (s.open_at > s.close_at AND (LOCALTIME >= s.open_at OR LOCALTIME <= s.close_at))
                )
            )
        )
        """,
        nativeQuery = true
    )
    fun findNearbyStoresFiltered(
        @Param("point") point: Point,
        @Param("radius") radiusInMeters: Double,
        @Param("cuisine") cuisine: String?,
        @Param("priceRange") priceRange: Int?,
        @Param("openNow") openNow: Boolean,
        pageable: Pageable
    ): Page<Store>

    fun findAllByChainId(chainId: UUID): List<Store>
}
