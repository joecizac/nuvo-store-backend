package com.nuvo.backend.features.social.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val firebaseMessaging: FirebaseMessaging?
) {

    fun sendNotification(token: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (firebaseMessaging == null) {
            println("Mock Mode: Skipping notification to \${token} - [\${title}: \${body}]")
            return
        }

        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()

        val message = Message.builder()
            .setToken(token)
            .setNotification(notification)
            .putAllData(data)
            .build()

        try {
            firebaseMessaging.send(message)
        } catch (e: Exception) {
            println("Failed to send notification: \${e.message}")
        }
    }
}
