package com.nuvo.backend.features.store.controller

import com.nuvo.backend.features.store.dto.ChainDTO
import com.nuvo.backend.features.store.dto.StoreDTO
import com.nuvo.backend.features.store.service.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Store Discovery", description = "Endpoints for finding and viewing store and chain information")
class StoreController(
    private val storeService: StoreService
) {

    @GetMapping("/stores")
    @Operation(summary = "Discover nearby stores", description = "Finds stores within a specified radius using geographic coordinates")
    fun getStores(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(defaultValue = "10000") radius: Double,
        @RequestParam(required = false) search: String?,
        pageable: Pageable
    ): Page<StoreDTO> {
        return storeService.getNearbyStores(lat, lng, radius, search, pageable)
    }

    @GetMapping("/stores/{id}")
    @Operation(summary = "Get store details", description = "Retrieves full information for a specific store by its ID")
    fun getStore(@PathVariable id: UUID): StoreDTO {
        return storeService.getStore(id)
    }

    @GetMapping("/chains")
    @Operation(summary = "List all chains", description = "Returns a list of all global store chains")
    fun getChains(): List<ChainDTO> {
        return storeService.getAllChains()
    }

    @GetMapping("/chains/{id}/stores")
    @Operation(summary = "List stores in a chain", description = "Returns all individual stores belonging to a specific chain")
    fun getStoresByChain(@PathVariable id: UUID): List<StoreDTO> {
        return storeService.getStoresByChain(id)
    }
}
