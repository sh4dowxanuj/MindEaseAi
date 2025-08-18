package com.mindeaseai.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.mindeaseai.R

class DailyTipReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tip = intent.getStringExtra("tip") ?: "Take a deep breath and relax."
        NotificationHelper.showDailyTip(context, tip)
    }

    companion object {
    @Deprecated("Use WorkManager PeriodicWork (DailyTipWorker) instead for reliability on modern Android")
    fun scheduleDailyTip(@Suppress("UNUSED_PARAMETER") context: Context, @Suppress("UNUSED_PARAMETER") tip: String) { /* no-op */ }
    }
}
