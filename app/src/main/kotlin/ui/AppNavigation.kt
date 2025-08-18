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
        if (isAuthenticated && currentRoute != "dashboard") {
            navController.navigate("dashboard") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Setup Google Sign-In launcher
    val context = LocalContext.current
    val defaultWebClientId = context.resources.getString(com.mindeaseai.R.string.default_web_client_id)
    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
    ).requestIdToken(defaultWebClientId).requestEmail().build()
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
                // Surface a helpful error message to the UI
                authViewModel.setError("Google sign-in did not return an ID token.")
            }
        } catch (e: Exception) {
            android.util.Log.e("AppNavigation", "Google sign-in failed", e)
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
            DashboardScreen(
                onNavigate = { navController.navigate(it) },
                errorMessage = error
            )
        }
        composable("ai_chat") {
            val authVm: com.mindeaseai.auth.AuthViewModel = hiltViewModel()
            val userName = remember { authVm.getUserEmailOrName() }
            AIChatFlow(
                userName = userName,
                onLogout = { authVm.logout() }
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
