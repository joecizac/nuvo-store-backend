package com.nuvo.backend.features.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "User profile information")
data class UserProfileDTO(
    @Schema(description = "Internal unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID?,

    @Schema(description = "External Firebase UID", example = "firebase-auth-uid-123")
    val firebaseUid: String,

    @Schema(description = "Email address", example = "user@example.com")
    val email: String,

    @Schema(description = "Full name", example = "John Doe")
    val name: String,

    @Schema(description = "Contact phone number", example = "+123456789")
    val phoneNumber: String?,

    @Schema(description = "URL to profile image", example = "https://example.com/avatar.jpg")
    val profileImageUrl: String?,

    @Schema(description = "Firebase Cloud Messaging token for push notifications")
    val fcmToken: String?
)

@Schema(description = "Request to sync user profile from Firebase")
data class SyncUserRequest(
    @Schema(description = "Full name", example = "John Doe")
    @field:NotBlank(message = "Name is required")
    val name: String,

    @Schema(description = "Email address", example = "john@example.com")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @Schema(description = "URL to profile image", example = "https://example.com/avatar.jpg")
    val profileImageUrl: String? = null
)

@Schema(description = "Address information for delivery")
data class AddressDTO(
    @Schema(description = "Unique identifier", example = "018e6b12-1234-7c2a-8921-9d1e2f3a4b5c")
    val id: UUID? = null,

    @Schema(description = "Title for the address", example = "Home")
    @field:NotBlank(message = "Title is required")
    val title: String,

    @Schema(description = "Full human-readable address", example = "123 Main St, Cape Town, 8001")
    @field:NotBlank(message = "Full address is required")
    val fullAddress: String,

    @Schema(description = "Latitude coordinate", example = "-33.9249")
    @field:NotNull(message = "Latitude is required")
    val latitude: Double,

    @Schema(description = "Longitude coordinate", example = "18.4241")
    @field:NotNull(message = "Longitude is required")
    val longitude: Double,

    @Schema(description = "Set as default delivery address", example = "true")
    val isDefault: Boolean = false
)
