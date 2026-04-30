package com.nuvo.backend.features.user.controller

import com.nuvo.backend.features.user.dto.AddressDTO
import com.nuvo.backend.features.user.dto.SyncUserRequest
import com.nuvo.backend.features.user.dto.UserProfileDTO
import com.nuvo.backend.features.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing user profiles and delivery addresses")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the authenticated user")
    fun getMyProfile(principal: Principal): UserProfileDTO {
        return userService.getUserProfile(principal.name)
    }

    @PutMapping("/me")
    @Operation(summary = "Sync user profile", description = "Creates or updates the user profile during Firebase login sync")
    fun syncProfile(principal: Principal, @Valid @RequestBody request: SyncUserRequest): UserProfileDTO {
        return userService.syncUser(principal.name, request)
    }

    @PutMapping("/me/fcm-token")
    @Operation(summary = "Update FCM token", description = "Registers or updates the push notification token for the user's device")
    fun updateFcmToken(principal: Principal, @RequestBody token: String) {
        userService.updateFcmToken(principal.name, token)
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "List user addresses", description = "Returns all delivery addresses associated with the current user")
    fun getMyAddresses(principal: Principal): List<AddressDTO> {
        return userService.getUserAddresses(principal.name)
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add new address", description = "Adds a new delivery address for the current user")
    fun addAddress(principal: Principal, @Valid @RequestBody dto: AddressDTO): AddressDTO {
        return userService.addAddress(principal.name, dto)
    }

    @PutMapping("/me/addresses/{id}")
    @Operation(summary = "Update address", description = "Edits an existing delivery address")
    fun updateAddress(
        principal: Principal,
        @PathVariable id: UUID,
        @Valid @RequestBody dto: AddressDTO
    ): AddressDTO {
        return userService.updateAddress(principal.name, id, dto)
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Remove address", description = "Deletes a delivery address from the user profile")
    fun deleteAddress(principal: Principal, @PathVariable id: UUID) {
        userService.deleteAddress(principal.name, id)
    }

    @PatchMapping("/me/addresses/{id}/default")
    @Operation(summary = "Set default address", description = "Sets a specific address as the primary delivery location")
    fun setDefaultAddress(principal: Principal, @PathVariable id: UUID): AddressDTO {
        return userService.setDefaultAddress(principal.name, id)
    }
}
