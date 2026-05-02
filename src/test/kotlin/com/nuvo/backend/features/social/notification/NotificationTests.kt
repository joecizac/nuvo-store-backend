package com.nuvo.backend.features.social.notification

import com.nuvo.backend.features.order.domain.OrderStatus
import com.nuvo.backend.features.order.domain.OrderStatusChangedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.util.UUID

class NotificationTests {

    @Test
    fun `NotificationService no-ops in mock mode`() {
        NotificationService(null).sendNotification("token", "Title", "Body", mapOf("id" to "1"))
    }

    @Test
    fun `NotificationEventListener sends notification when token exists`() {
        val notificationService = Mockito.mock(NotificationService::class.java)
        val listener = NotificationEventListener(notificationService)
        val orderId = UUID.randomUUID()

        listener.handleOrderStatusChanged(
            OrderStatusChangedEvent(orderId, UUID.randomUUID(), OrderStatus.DISPATCHED, "token")
        )

        verify(notificationService).sendNotification(
            "token",
            "Order Update",
            "Your order #\${event.orderId} is now \${event.newStatus}",
            mapOf("orderId" to orderId.toString())
        )
    }

    @Test
    fun `NotificationEventListener skips notification when token is absent`() {
        val notificationService = Mockito.mock(NotificationService::class.java)
        val listener = NotificationEventListener(notificationService)

        listener.handleOrderStatusChanged(
            OrderStatusChangedEvent(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.PENDING, null)
        )

        verifyNoInteractions(notificationService)
    }
}
