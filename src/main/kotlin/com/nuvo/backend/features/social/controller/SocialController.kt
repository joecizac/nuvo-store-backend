package com.nuvo.backend.features.social.controller

import com.nuvo.backend.features.social.dto.ReviewDTO
import com.nuvo.backend.features.social.dto.SubmitReviewRequest
import com.nuvo.backend.features.social.service.SocialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Social & Interactions", description = "Endpoints for user reviews, ratings, and favourites")
class SocialController(
    private val socialService: SocialService
) {

    @GetMapping("/stores/{storeId}/reviews")
    @Operation(summary = "Get store reviews", description = "Retrieves all customer reviews and ratings for a specific store")
    fun getReviews(@PathVariable storeId: UUID, pageable: Pageable): Page<ReviewDTO> {
        return socialService.getStoreReviews(storeId, pageable)
    }

    @PostMapping("/stores/{storeId}/reviews")
    @Operation(summary = "Submit store review", description = "Allows the user to leave a rating and text review for a store (Legacy: use order review if possible)")
    fun submitReview(
        principal: Principal,
        @PathVariable storeId: UUID,
        @RequestBody request: SubmitReviewRequest
    ): ReviewDTO {
        return socialService.submitReview(principal.name, storeId, request)
    }

    @PostMapping("/orders/{orderId}/reviews")
    @Operation(summary = "Submit order review", description = "Allows the user to leave a rating and text review for a completed order")
    fun submitOrderReview(
        principal: Principal,
        @PathVariable orderId: UUID,
        @RequestBody request: SubmitReviewRequest
    ): ReviewDTO {
        return socialService.submitOrderReview(principal.name, orderId, request)
    }

    @GetMapping("/orders/{orderId}/tracking")
    @Operation(summary = "Track order", description = "Fetches the real-time coordinates of the courier for an active order")
    fun getOrderTracking(principal: Principal, @PathVariable orderId: UUID): Map<String, Double?> {
        return socialService.getOrderTracking(principal.name, orderId)
    }

    @PostMapping("/stores/{storeId}/favourite")
    @Operation(summary = "Toggle favourite store", description = "Adds or removes a store from the user's favourite list")
    fun toggleFavouriteStore(principal: Principal, @PathVariable storeId: UUID) {
        socialService.toggleFavouriteStore(principal.name, storeId)
    }

    @PostMapping("/products/{productId}/favourite")
    @Operation(summary = "Toggle favourite product", description = "Adds or removes a product from the user's favourite list")
    fun toggleFavouriteProduct(principal: Principal, @PathVariable productId: UUID) {
        socialService.toggleFavouriteProduct(principal.name, productId)
    }
}
