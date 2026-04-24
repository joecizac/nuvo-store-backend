package com.nuvo.backend.features.social.dto

import java.util.UUID

data class ReviewDTO(
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class SubmitReviewRequest(
    val rating: Int,
    val comment: String?
)
