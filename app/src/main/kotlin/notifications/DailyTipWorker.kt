package com.mindeaseai.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

class DailyTipWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val tip = pickRandomTip(applicationContext)
            NotificationHelper.showDailyTip(applicationContext, tip)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun pickRandomTip(context: Context): String {
        val input = context.assets.open("daily_tips.json")
        val text = input.bufferedReader().use(BufferedReader::readText)
        val items = Regex("\"(.*?)\"").findAll(text).map { it.groupValues[1] }.toList()
        return if (items.isNotEmpty()) items.random() else "Take a deep breath and relax."
    }
}
