package com.nuvo.backend.features.store.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.catalog.repository.ProductRepository
import com.nuvo.backend.features.order.repository.OrderRepository
import com.nuvo.backend.features.store.domain.Chain
import com.nuvo.backend.features.store.domain.Store
import com.nuvo.backend.features.store.dto.ChainDTO
import com.nuvo.backend.features.store.dto.StoreDTO
import com.nuvo.backend.features.store.repository.ChainRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import com.nuvo.backend.features.store.repository.StoreSpecifications
import com.nuvo.backend.features.user.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val chainRepository: ChainRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) {

    @Transactional(readOnly = true)
    fun getNearbyStoresFiltered(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double,
        cuisine: String?,
        priceRange: Int?,
        openNow: Boolean,
        pageable: Pageable
    ): Page<StoreDTO> {
        val point = GeometryUtil.createPoint(latitude, longitude)
        var spec = StoreSpecifications.isNearby(point, radiusInMeters)
            .and(StoreSpecifications.isActive())

        if (cuisine != null) spec = spec.and(StoreSpecifications.hasCuisine(cuisine))
        if (priceRange != null) spec = spec.and(StoreSpecifications.hasPriceRange(priceRange))
        if (openNow) spec = spec.and(StoreSpecifications.isOpenNow())

        return storeRepository.findAll(spec, pageable).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["nearby_stores"], key = "#latitude + '-' + #longitude + '-' + #radiusInMeters + '-' + #searchQuery + '-' + #pageable.pageNumber")
    fun getNearbyStores(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double,
        searchQuery: String?,
        pageable: Pageable
    ): Page<StoreDTO> {
        val point = GeometryUtil.createPoint(latitude, longitude)
        return storeRepository.findNearbyStores(point, radiusInMeters, searchQuery, pageable)
            .map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["store_search"], key = "#latitude + '-' + #longitude + '-' + #radiusInMeters + '-' + #query + '-' + #pageable.pageNumber")
    fun searchStores(
        latitude: Double,
        longitude: Double,
        radiusInMeters: Double,
        query: String,
        pageable: Pageable
    ): Page<StoreDTO> {
        val point = GeometryUtil.createPoint(latitude, longitude)
        return storeRepository.searchStores(point, radiusInMeters, query, pageable)
            .map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["store_details"], key = "#id")
    fun getStore(id: UUID): StoreDTO {
        return storeRepository.findById(id).orElseThrow { ResourceNotFoundException("Store not found") }.toDTO()
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["global_chains"])
    fun getAllChains(): List<ChainDTO> {
        return chainRepository.findAll().map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["chain_stores"], key = "#chainId")
    fun getStoresByChain(chainId: UUID): List<StoreDTO> {
        return storeRepository.findAllByChainId(chainId).map { it.toDTO() }
    }

    private fun Store.toDTO() = StoreDTO(
        id = id ?: UUID.randomUUID(),
        chainId = chain?.id,
        name = name,
        description = description,
        contactNumber = contactNumber,
        logoUrl = logoUrl,
        bannerUrl = bannerUrl,
        latitude = location.y,
        longitude = location.x,
        address = address,
        averageRating = averageRating,
        cuisine = cuisine,
        priceRange = priceRange,
        deliveryFee = deliveryFee,
        openAt = openAt?.toString(),
        closeAt = closeAt?.toString()
    )

    private fun Chain.toDTO() = ChainDTO(
        id = id ?: UUID.randomUUID(),
        name = name,
        description = description,
        logoUrl = logoUrl,
        bannerUrl = bannerUrl
    )
}
