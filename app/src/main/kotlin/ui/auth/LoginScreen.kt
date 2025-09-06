package com.mindeaseai.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onSignup: () -> Unit,
    onGoogleSignIn: () -> Unit,
    errorMessage: String?,
    loading: Boolean,
    lastEmail: String? = null,
    onEmailInput: (String) -> Unit = {},
    onClearError: () -> Unit = {}
) {
    var email by remember { mutableStateOf(lastEmail ?: "") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) snackbarHostState.showSnackbar(errorMessage)
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Login", style = MaterialTheme.typography.headlineSmall) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            onEmailInput(it)
                            onClearError()
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (!errorMessage.isNullOrBlank()) onClearError()
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val canSubmit = !loading && email.isNotBlank() && password.length >= 6
                    Button(
                        onClick = { if (canSubmit) onLogin(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit,
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSignup,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Sign Up")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }
        }
    }
}
