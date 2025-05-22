package com.example.slurp_v0.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.AuthRepository
import com.example.slurp_v0.data.AuthResult
import com.example.slurp_v0.data.ResetPasswordResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isPasswordResetSent: Boolean = false,
    val rememberMe: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        repository.getCurrentUser()?.let { user ->
            _state.update { it.copy(
                isAuthenticated = true,
                isEmailVerified = user.isEmailVerified
            )}
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.signIn(email, password, state.value.rememberMe)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isEmailVerified = result.user.isEmailVerified,
                        error = null
                    )}
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.signUp(email, password)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isEmailVerified = false,
                        error = null
                    )}
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.sendPasswordResetEmail(email)) {
                is ResetPasswordResult.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        isPasswordResetSent = true,
                        error = null
                    )}
                }
                is ResetPasswordResult.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    fun toggleRememberMe() {
        _state.update { it.copy(rememberMe = !it.rememberMe) }
    }

    fun signOut() {
        repository.signOut()
        _state.value = AuthState()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearPasswordResetSent() {
        _state.update { it.copy(isPasswordResetSent = false) }
    }
} 