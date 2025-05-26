package com.example.slurp_v0.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.AuthRepository
import com.example.slurp_v0.data.RatingRepository
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userProfile: UserProfile? = null,
    val userRatings: List<Rating> = emptyList()
)

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val ratingRepository = RatingRepository()
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )}
                    return@launch
                }

                val userId = currentUser.uid
                val userProfile = ratingRepository.getUserProfile(userId)
                val userRatings = ratingRepository.getUserRatings(userId)

                _state.update { it.copy(
                    isLoading = false,
                    userProfile = userProfile,
                    userRatings = userRatings
                )}
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while loading profile"
                )}
            }
        }
    }

    fun refresh() {
        loadUserProfile()
    }
} 