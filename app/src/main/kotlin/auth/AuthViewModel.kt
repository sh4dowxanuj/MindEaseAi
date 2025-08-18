package com.mindeaseai.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    fun getUserEmailOrName(): String? {
        val user = auth.currentUser
        return user?.displayName ?: user?.email ?: "User"
    }
    private val _authState = MutableStateFlow<Boolean>(auth.currentUser != null)
    val authState: StateFlow<Boolean> = _authState
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _loading = MutableStateFlow<Boolean>(false)
    val loading: StateFlow<Boolean> = _loading

    fun login(email: String, password: String) {
        if (_loading.value) return
        _error.value = null
        if (!isValidEmail(email)) {
            _error.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = parseAuthException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun signup(email: String, password: String) {
        if (_loading.value) return
        _error.value = null
        if (!isValidEmail(email)) {
            _error.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = parseAuthException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    // Sign in using Google ID token (from Google Sign-In flow)
    fun loginWithGoogle(idToken: String) {
        if (_loading.value) return
        _error.value = null
        viewModelScope.launch {
            _loading.value = true
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _authState.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = parseAuthException(e)
                android.util.Log.e("AuthViewModel", "Google login error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // Allow hosts to set an error message (e.g. from external sign-in flows)
    fun setError(message: String) {
        _error.value = message
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun parseAuthException(e: Exception): String {
        // Use FirebaseAuthException error codes if possible
        return when (e) {
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found with this email."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Email is already in use."
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak."
            else -> {
                val msg = e.message ?: "Unknown error occurred."
                if (msg.contains("network", true)) {
                    "Network error. Please check your connection."
                } else {
                    "Authentication failed. Please try again."
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = false
    }
}
