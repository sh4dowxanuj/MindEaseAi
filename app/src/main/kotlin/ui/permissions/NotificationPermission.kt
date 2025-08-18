package com.mindeaseai.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        content()
        return
    }

    val permission = Manifest.permission.POST_NOTIFICATIONS
    val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    val showRationale = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Regardless of result, proceed to content to avoid blocking UX */ }
    )

    LaunchedEffect(Unit) {
        if (!isGranted) {
            showRationale.value = true
        }
    }

    if (!isGranted && showRationale.value) {
        AlertDialog(
            onDismissRequest = { showRationale.value = false },
            title = { Text("Allow notifications?") },
            text = { Text("We use notifications to send gentle daily tips. You can change this anytime in Settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale.value = false
                    launcher.launch(permission)
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale.value = false }) { Text("Not now") }
            }
        )
    }

    content()
}
