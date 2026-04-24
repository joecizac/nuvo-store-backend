package com.nuvo.backend.features.order.domain

import java.util.UUID

data class OrderStatusChangedEvent(
    val orderId: UUID,
    val userId: UUID,
    val newStatus: OrderStatus,
    val fcmToken: String?
)
