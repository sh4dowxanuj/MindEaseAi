package com.mindeaseai.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.mindeaseai.ui.auth.LoginScreen
import com.mindeaseai.ui.auth.SignupScreen
import com.mindeaseai.ui.DashboardScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.ui.ai.AIChatFlow

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // coroutineScope was unused — removed to silence warnings
    val authViewModel: com.mindeaseai.auth.AuthViewModel = hiltViewModel()
    val isAuthenticated by authViewModel.authState.collectAsState()
    val error by authViewModel.error.collectAsState()
    val loading by authViewModel.loading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isAuthenticated) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (isAuthenticated) {
            if (currentRoute != "dashboard") {
                navController.navigate("dashboard") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            if (currentRoute != "login") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Setup Google Sign-In launcher
    val context = LocalContext.current
    val defaultWebClientId = context.resources.getString(com.mindeaseai.R.string.default_web_client_id)
    // Build Google Sign-In options requesting the ID token for Firebase auth.
    // Note: default_web_client_id must correspond to the Web client (OAuth type 3) in google-services.json
    val gso = remember(defaultWebClientId) {
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(defaultWebClientId).requestEmail().build()
    }
    val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                authViewModel.loginWithGoogle(idToken)
            } else {
                authViewModel.setError("Google sign-in returned no ID token. (Check that you used the WEB client ID)")
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            val code = e.statusCode
            val msg = when (code) {
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Google sign-in was cancelled."
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during Google sign-in. Check your connection."
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.DEVELOPER_ERROR -> "Google sign-in configuration error (SHA-1 / OAuth client mismatch or provider not enabled)."
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Google sign-in failed. Try again."
                12500 /* Common DEVELOPER_ERROR for Firebase */ -> "Configuration error (often missing SHA-1 or not using web client ID)."
                else -> "Google sign-in error code $code."
            }
            android.util.Log.e("AppNavigation", "Google sign-in failed (code=$code)", e)
            authViewModel.setError(msg)
        } catch (e: Exception) {
            android.util.Log.e("AppNavigation", "Google sign-in failed (unexpected)", e)
            authViewModel.setError("Google sign-in failed: ${e.message ?: "Unknown error"}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (!isAuthenticated) "login" else "dashboard"
    ) {
        composable("login") {
            LoginScreen(
                onLogin = { email, password -> authViewModel.login(email, password) },
                onSignup = { navController.navigate("signup") },
                onGoogleSignIn = { googleLauncher.launch(googleSignInClient.signInIntent) },
                errorMessage = error,
                loading = loading
            )
        }
        composable("signup") {
            SignupScreen(
                onSignup = { email, password -> authViewModel.signup(email, password) },
                onBack = { navController.popBackStack() },
                errorMessage = error,
                loading = loading
            )
        }
        composable("dashboard") {
            val authVm: com.mindeaseai.auth.AuthViewModel = hiltViewModel()
            DashboardScreen(
                onNavigate = { navController.navigate(it) },
                errorMessage = error,
                onLogout = {
                    // Sign out from Firebase
                    authVm.logout()
                    try {
                        // Also sign out Google client to prevent silent auto-login
                        googleSignInClient.signOut()
                    } catch (e: Exception) {
                        android.util.Log.w("AppNavigation", "Google signOut failed: ${e.message}")
                    }
                }
            )
        }
        composable("ai_chat") {
            val authVm: com.mindeaseai.auth.AuthViewModel = hiltViewModel()
            val userName = remember { authVm.getUserEmailOrName() }
            AIChatFlow(
                userName = userName
            )
        }
        composable("daily_tips") {
            com.mindeaseai.ui.tips.DailyTipsFlow()
        }
        composable("mood_tracker") {
            com.mindeaseai.ui.mood.MoodTrackerFlow()
        }
        composable("journal") {
            com.mindeaseai.ui.journal.JournalFlow()
        }
    }
}
