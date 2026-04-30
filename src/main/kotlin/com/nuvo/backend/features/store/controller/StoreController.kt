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

    @GetMapping("/stores/search")
    @Operation(summary = "Global search", description = "Searches for stores, products, or descriptions across all nearby stores")
    fun search(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(defaultValue = "10000") radius: Double,
        @RequestParam q: String,
        pageable: Pageable
    ): Page<StoreDTO> {
        return storeService.searchStores(lat, lng, radius, q, pageable)
    }

    @GetMapping("/stores")
    @Operation(summary = "Discover nearby stores", description = "Finds stores within a specified radius using geographic coordinates with optional filters")
    fun getStores(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(defaultValue = "10000") radius: Double,
        @RequestParam(required = false) cuisine: String?,
        @RequestParam(required = false) priceRange: Int?,
        @RequestParam(defaultValue = "false") openNow: Boolean,
        pageable: Pageable
    ): Page<StoreDTO> {
        return storeService.getNearbyStoresFiltered(lat, lng, radius, cuisine, priceRange, openNow, pageable)
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
