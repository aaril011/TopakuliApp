package com.project.topakuli.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.project.topakuli.util.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check user preference before showing notification
        val prefs = getSharedPreferences("topakuli_notif_prefs", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        
        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications are disabled by user. Skipping notification display.")
            return
        }

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Pemberitahuan"
            val message = remoteMessage.data["content"] ?: ""
            
            NotificationHelper.showSystemNotification(this, title, message)
        }

        // Handle notification payload (standard FCM notification)
        remoteMessage.notification?.let {
            val title = it.title ?: "Pemberitahuan"
            val body = it.body ?: ""
            if (remoteMessage.data.isEmpty()) {
                NotificationHelper.showSystemNotification(this, title, body)
            }
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}
