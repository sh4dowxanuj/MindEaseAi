package com.mindeaseai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mindeaseai.ui.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import com.mindeaseai.ui.permissions.NotificationPermissionGate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationPermissionGate {
                AppNavigation()
            }
        }
    }
}
