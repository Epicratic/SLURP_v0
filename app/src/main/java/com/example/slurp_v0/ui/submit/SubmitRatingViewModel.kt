package com.example.slurp_v0.ui.submit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.LocationRepository
import com.example.slurp_v0.data.repository.RatingRepository
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.model.SectorConstants
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlinx.coroutines.delay

data class SubmitRatingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    // Location
    val selectedGovernorate: String = "",
    val selectedDelegation: String = "",
    val availableGovernorates: List<String> = emptyList(),
    val availableDelegations: List<String> = emptyList(),
    // Sector
    val selectedMacroSector: String = "",
    val selectedMesoSector: String = "",
    val availableMesoSectors: List<String> = emptyList(),
    // Actor
    val actorName: String = "",
    val isNewActor: Boolean = false,
    val availableActors: List<String> = emptyList(),
    // Rating
    val selectedIndicatorCategory: String = "",
    val selectedIndicatorType: String = "",
    val availableIndicatorTypes: List<String> = emptyList(),
    val rating: Float = 0f,
    // Additional info
    val comment: String = "",
    val selectedPhoto: Uri? = null,
    val isSubmitted: Boolean = false
)

class SubmitRatingViewModel(application: Application) : AndroidViewModel(application) {
    private val ratingRepository = RatingRepository()
    private val locationRepository = LocationRepository(application)
    private val _state = MutableStateFlow(SubmitRatingState())
    val state: StateFlow<SubmitRatingState> = _state.asStateFlow()

    init {
        loadGovernorates()
    }

    private fun loadGovernorates() {
        viewModelScope.launch {
            try {
                val governorates = locationRepository.getGovernorateNames()
                _state.update { it.copy(availableGovernorates = governorates) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onGovernorateSelected(governorate: String) {
        _state.update { it.copy(
            selectedGovernorate = governorate,
            selectedDelegation = "", // Reset delegation when governorate changes
            availableDelegations = emptyList() // Clear delegations while loading
        )}
        loadDelegations(governorate)
    }

    private fun loadDelegations(governorate: String) {
        viewModelScope.launch {
            try {
                val delegations = locationRepository.getDelegationsForGovernorate(governorate)
                _state.update { it.copy(availableDelegations = delegations) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDelegationSelected(delegation: String) {
        _state.update { it.copy(selectedDelegation = delegation) }
    }

    fun onMacroSectorSelected(macroSector: String) {
        val mesoSectors = SectorConstants.MACRO_SECTORS[macroSector] ?: emptyList()
        _state.update { it.copy(
            selectedMacroSector = macroSector,
            selectedMesoSector = "",
            availableMesoSectors = mesoSectors
        )}
    }

    fun onMesoSectorSelected(mesoSector: String) {
        _state.update { it.copy(selectedMesoSector = mesoSector) }
        loadActorsForSector(mesoSector)
    }

    private fun loadActorsForSector(mesoSector: String) {
        viewModelScope.launch {
            try {
                val actors = ratingRepository.getActorsForSector(mesoSector)
                _state.update { it.copy(availableActors = actors) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onActorNameChanged(name: String) {
        _state.update { it.copy(actorName = name) }
    }

    fun onNewActorToggled(isNew: Boolean) {
        _state.update { it.copy(isNewActor = isNew) }
    }

    fun onIndicatorCategorySelected(category: String) {
        val types = SectorConstants.INDICATORS[category] ?: emptyList()
        _state.update { it.copy(
            selectedIndicatorCategory = category,
            selectedIndicatorType = "",
            availableIndicatorTypes = types
        )}
    }

    fun onIndicatorTypeSelected(type: String) {
        _state.update { it.copy(selectedIndicatorType = type) }
    }

    fun onRatingChanged(rating: Float) {
        _state.update { it.copy(rating = rating) }
    }

    fun onCommentChanged(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    fun onPhotoSelected(uri: Uri) {
        _state.update { it.copy(selectedPhoto = uri) }
    }

    fun submitRating() {
        val currentState = _state.value
        
        val rating = Rating(
            id = UUID.randomUUID().toString(),
            actorName = currentState.actorName,
            governorate = currentState.selectedGovernorate,
            delegation = currentState.selectedDelegation,
            macroSector = currentState.selectedMacroSector,
            mesoSector = currentState.selectedMesoSector,
            indicatorCategory = currentState.selectedIndicatorCategory,
            indicatorType = currentState.selectedIndicatorType,
            rating = currentState.rating,
            comment = currentState.comment,
            timestamp = Timestamp.now()
        )

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                ratingRepository.submitRating(rating)
                _state.update { it.copy(
                    isLoading = false,
                    success = true
                ) }
                // Show success message for 3 seconds before navigation
                delay(3000)
                _state.update { it.copy(isSubmitted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Failed to submit rating: ${e.message}"
                ) }
            }
        }
    }

    fun updateActorName(name: String) {
        _state.update { it.copy(actorName = name) }
    }

    fun updateGovernorate(governorate: String) {
        _state.update { it.copy(selectedGovernorate = governorate) }
    }

    fun updateDelegation(delegation: String) {
        _state.update { it.copy(selectedDelegation = delegation) }
    }

    fun updateMacroSector(sector: String) {
        _state.update { it.copy(selectedMacroSector = sector) }
    }

    fun updateMesoSector(sector: String) {
        _state.update { it.copy(selectedMesoSector = sector) }
    }

    fun updateIndicatorCategory(category: String) {
        _state.update { it.copy(selectedIndicatorCategory = category) }
    }

    fun updateIndicatorType(type: String) {
        _state.update { it.copy(selectedIndicatorType = type) }
    }

    fun updateRating(rating: Float) {
        _state.update { it.copy(rating = rating) }
    }

    fun updateComment(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    fun resetSuccess() {
        _state.update { it.copy(success = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 