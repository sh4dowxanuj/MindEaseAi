package com.mindeaseai.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object PersistentKeys {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val DAILY_TIPS_ENABLED = booleanPreferencesKey("daily_tips_enabled")
}

suspend fun setOnboardingComplete(context: Context, complete: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[PersistentKeys.ONBOARDING_COMPLETE] = complete
    }
}

fun onboardingCompleteFlow(context: Context): Flow<Boolean> =
    context.dataStore.data.map { prefs ->
        prefs[PersistentKeys.ONBOARDING_COMPLETE] ?: false
    }

suspend fun setDailyTipsEnabled(context: Context, enabled: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[PersistentKeys.DAILY_TIPS_ENABLED] = enabled
    }
}

fun dailyTipsEnabledFlow(context: Context): Flow<Boolean> =
    context.dataStore.data.map { prefs ->
        prefs[PersistentKeys.DAILY_TIPS_ENABLED] ?: true // default ON
    }
