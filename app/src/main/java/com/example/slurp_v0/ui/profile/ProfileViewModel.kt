package com.example.slurp_v0.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.AuthRepository
import com.example.slurp_v0.data.RatingRepository
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    // New exposed mapped state for UI
    val profileDataState: StateFlow<ProfileDataState> = state
        .map { mapToProfileDataState(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, ProfileDataState(isLoading = true))

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

    private fun mapToProfileDataState(profileState: ProfileState): ProfileDataState {
        val userProfile = profileState.userProfile
        val ratings = profileState.userRatings

        if (profileState.isLoading) {
            return ProfileDataState(isLoading = true)
        }

        if (profileState.error != null) {
            return ProfileDataState(error = profileState.error)
        }

        // Format registration date from timestamp (assuming lastActive)
        val registrationDate = userProfile?.lastActive?.let {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(it))
        } ?: ""

        // Calculate average rating
        val averageRating = ratings.map { it.rating }.average().toFloat()

        // Calculate total ratings
        val totalRatings = ratings.size

        // Calculate userRank - placeholder 0 (you can implement your own logic later)
        val userRank = 0

        // Calculate bestActor and worstActor based on average ratings per actor
        val ratingsByActor = ratings.groupBy { it.actorName }
        val averageByActor = ratingsByActor.mapValues { entry ->
            entry.value.map { it.rating }.average()
        }
        val bestActor = averageByActor.maxByOrNull { it.value }?.key ?: ""
        val worstActor = averageByActor.minByOrNull { it.value }?.key ?: ""

        // Calculate average per sector (macroSector)
        val averagePerSector = ratings.groupBy { it.macroSector }
            .mapValues { entry ->
                entry.value.map { it.rating }.average().toFloat()
            }

        // Shared ratings = user ratings for now
        val sharedRatings = ratings

        // Badges - placeholder empty list, add your own badge logic here
        val badges = emptyList<String>()

        return ProfileDataState(
            email = userProfile?.email ?: "",
            registrationDate = registrationDate,
            totalRatings = totalRatings,
            userRank = userRank,
            averageRating = averageRating,
            bestActor = bestActor,
            worstActor = worstActor,
            averagePerSector = averagePerSector,
            sharedRatings = sharedRatings,
            badges = badges,
            isLoading = false,
            error = null
        )
    }
}
