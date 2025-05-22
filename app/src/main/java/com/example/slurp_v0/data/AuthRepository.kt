package com.example.slurp_v0.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "AuthRepository"

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class ResetPasswordResult {
    data object Success : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        Log.d(TAG, "Initializing Firebase Auth: ${auth.app.name}")
    }

    suspend fun signIn(email: String, password: String, rememberMe: Boolean = false): AuthResult {
        return try {
            Log.d(TAG, "Attempting to sign in user: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Sign in successful for user: ${it.uid}")
                if (!it.isEmailVerified) {
                    Log.d(TAG, "Email not verified, sending verification email")
                    sendEmailVerification(it)
                }
                AuthResult.Success(it)
            } ?: run {
                Log.e(TAG, "Sign in failed: No user returned")
                AuthResult.Error("Unknown error occurred")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign in failed", e)
            AuthResult.Error(e.message ?: "Authentication failed")
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Attempting to create user: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "User created successfully: ${it.uid}")
                sendEmailVerification(it)
                AuthResult.Success(it)
            } ?: run {
                Log.e(TAG, "Sign up failed: No user returned")
                AuthResult.Error("Unknown error occurred")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign up failed", e)
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    private suspend fun sendEmailVerification(user: FirebaseUser) {
        try {
            user.sendEmailVerification().await()
            Log.d(TAG, "Verification email sent to ${user.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send verification email", e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): ResetPasswordResult {
        return try {
            Log.d(TAG, "Sending password reset email to: $email")
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent successfully")
            ResetPasswordResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email", e)
            ResetPasswordResult.Error(e.message ?: "Failed to send password reset email")
        }
    }

    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isEmailVerified(): Boolean = getCurrentUser()?.isEmailVerified ?: false
} 