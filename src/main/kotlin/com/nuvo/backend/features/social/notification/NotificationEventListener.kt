package com.nuvo.backend.features.social.notification

import com.nuvo.backend.features.order.domain.OrderStatusChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NotificationEventListener(
    private val notificationService: NotificationService
) {

    @Async
    @EventListener
    fun handleOrderStatusChanged(event: OrderStatusChangedEvent) {
        event.fcmToken?.let { token ->
            val title = "Order Update"
            val body = "Your order #\${event.orderId} is now \${event.newStatus}"
            
            notificationService.sendNotification(
                token = token,
                title = title,
                body = body,
                data = mapOf("orderId" to event.orderId.toString())
            )
        }
    }
}
