package com.nuvo.backend.features.store.service

import com.nuvo.backend.common.exception.ResourceNotFoundException
import com.nuvo.backend.common.util.GeometryUtil
import com.nuvo.backend.features.store.domain.Chain
import com.nuvo.backend.features.store.domain.Store
import com.nuvo.backend.features.store.dto.ChainDTO
import com.nuvo.backend.features.store.dto.StoreDTO
import com.nuvo.backend.features.store.repository.ChainRepository
import com.nuvo.backend.features.store.repository.StoreRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val chainRepository: ChainRepository
) {

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
        averageRating = averageRating
    )

    private fun Chain.toDTO() = ChainDTO(
        id = id ?: UUID.randomUUID(),
        name = name,
        description = description,
        logoUrl = logoUrl,
        bannerUrl = bannerUrl
    )
}
