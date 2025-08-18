package com.mindeaseai.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class DailyTipMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data["tip"]?.let { tip ->
            NotificationHelper.showDailyTip(applicationContext, tip)
        }
    }
}
