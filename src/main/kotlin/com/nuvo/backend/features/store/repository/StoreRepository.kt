package com.nuvo.backend.features.store.repository

import com.nuvo.backend.features.store.domain.Store
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface StoreRepository : JpaRepository<Store, UUID> {
    
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

    fun findAllByChainId(chainId: UUID): List<Store>
}
