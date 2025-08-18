package com.mindeaseai.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindeaseai.auth.AuthViewModel
import com.mindeaseai.ui.auth.LoginScreen
import com.mindeaseai.ui.auth.SignupScreen

@Composable
fun AuthFlow(
    onAuthSuccess: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val isAuthenticated by authViewModel.authState.collectAsState()
    val error by authViewModel.error.collectAsState()
    val loading by authViewModel.loading.collectAsState()
    var showSignup by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onAuthSuccess()
        }
    }

    if (showSignup) {
        SignupScreen(
            onSignup = { email, password -> authViewModel.signup(email, password) },
            onBack = { showSignup = false },
            errorMessage = error,
            loading = loading
        )
    } else {
        LoginScreen(
            onLogin = { email, password -> authViewModel.login(email, password) },
            onSignup = { showSignup = true },
            onGoogleSignIn = { /* host will handle launcher and then call viewModel.loginWithGoogle(idToken) */ },
            errorMessage = error,
            loading = loading
        )
    }
}
