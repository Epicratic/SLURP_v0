package com.example.slurp_v0.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.RatingRepository
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.constants.SectorConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val globalAverage: Double = 0.0,
    val territoryAverages: Map<String, Double> = emptyMap(),
    val sectorAverages: Map<String, Double> = emptyMap(),
    val indicatorAverages: Map<String, Double> = emptyMap(),
    val topActors: List<Pair<String, Double>> = emptyList(),
    val recentRatings: List<Rating> = emptyList()
)

class DashboardViewModel : ViewModel() {
    private val repository = RatingRepository()
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Load all data concurrently
                val globalAverage = repository.getGlobalAverageRating()
                
                // Sort governorates alphabetically, with special handling for "Béja"
                val territoryAverages = repository.getAverageByDimension("governorate")
                    .toList()
                    .sortedBy { (governorate, _) ->
                        // Special handling for "Béja" to ensure it comes after "Ariana"
                        if (governorate == "Béja") "Beja" else governorate
                    }
                    .toMap()
                
                // Get sectors in the specified order
                val orderedSectors = listOf(
                    "Governance",
                    "Security",
                    "Healthcare",
                    "Education",
                    "Food",
                    "Housing",
                    "Transport",
                    "Communication"
                )
                val sectorAverages = repository.getAverageByDimension("macroSector")
                    .toList()
                    .sortedBy { (sector, _) -> orderedSectors.indexOf(sector) }
                    .toMap()
                
                val indicatorAverages = repository.getAverageByDimension("indicatorCategory")
                val topActors = repository.getTopActors()
                val recentRatings = repository.getRecentRatings()

                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        globalAverage = globalAverage,
                        territoryAverages = territoryAverages,
                        sectorAverages = sectorAverages,
                        indicatorAverages = indicatorAverages,
                        topActors = topActors,
                        recentRatings = recentRatings
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${e.message}"
                ) }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
} 