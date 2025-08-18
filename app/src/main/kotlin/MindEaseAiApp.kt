package com.mindeaseai

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import com.mindeaseai.notifications.NotificationHelper
import com.mindeaseai.notifications.DailyTipScheduler
import com.mindeaseai.ui.dailyTipsEnabledFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@HiltAndroidApp
class MindEaseAiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    // Create notification channel for daily tips and other notifications
    NotificationHelper.createChannel(this)
        // Respect user's preference for daily tips
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        appScope.launch {
            val enabled = dailyTipsEnabledFlow(this@MindEaseAiApp).first()
            if (enabled) DailyTipScheduler.enable(this@MindEaseAiApp) else DailyTipScheduler.disable(this@MindEaseAiApp)
        }
    }
}
