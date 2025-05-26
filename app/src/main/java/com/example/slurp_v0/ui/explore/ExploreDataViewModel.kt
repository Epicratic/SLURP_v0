package com.example.slurp_v0.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.repository.RatingRepository
import com.example.slurp_v0.data.constants.SectorConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class ExploreDataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RatingRepository()
    private val _state = MutableStateFlow(ExploreDataState())
    val state: StateFlow<ExploreDataState> = _state

    private var allRatings: List<Rating> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                allRatings = repository.getRatings()
                updateFilteredData()
                
                // Update available options
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        availableGovernorates = allRatings.map { it.governorate }.distinct().sorted(),
                        availableMacroSectors = SectorConstants.MACRO_SECTORS.keys.toList(),
                        availableIndicatorTypes = SectorConstants.INDICATORS.keys.toList()
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun updateFilteredData() {
        val currentState = _state.value
        
        // Apply filters
        var filteredRatings = allRatings
        
        if (currentState.selectedGovernorate.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.governorate == currentState.selectedGovernorate }
        }
        
        if (currentState.selectedDelegation.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.delegation == currentState.selectedDelegation }
        }
        
        if (currentState.selectedMacroSector.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.macroSector == currentState.selectedMacroSector }
        }
        
        if (currentState.selectedMesoSector.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.mesoSector == currentState.selectedMesoSector }
        }
        
        if (currentState.selectedIndicatorCategory.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.indicatorCategory == currentState.selectedIndicatorCategory }
        }
        
        if (currentState.selectedIndicatorType.isNotEmpty()) {
            filteredRatings = filteredRatings.filter { it.indicatorType == currentState.selectedIndicatorType }
        }
        
        // Apply time range filter
        if (currentState.selectedTimeRange != TimeRange.ALL) {
            val cutoffDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -(currentState.selectedTimeRange.days ?: 0))
            }.time
            filteredRatings = filteredRatings.filter { it.timestamp.toDate() > cutoffDate }
        }
        
        // Apply rating range filter
        filteredRatings = filteredRatings.filter { 
            it.rating >= currentState.minRating && it.rating <= currentState.maxRating 
        }

        // Calculate averages
        val delegationAverages = filteredRatings
            .groupBy { it.delegation }
            .mapValues { (_, ratings) -> ratings.map { it.rating.toDouble() }.average() }
            .toSortedMap()

        val mapData = filteredRatings
            .groupBy { it.governorate }
            .mapValues { (_, ratings) -> ratings.map { it.rating.toDouble() }.average() }
            .toSortedMap()

        // Update state with filtered data
        _state.update { it.copy(
            delegationAverages = delegationAverages,
            mapData = mapData
        ) }
    }

    fun onGovernorateSelected(governorate: String) {
        _state.update { it.copy(
            selectedGovernorate = governorate,
            selectedDelegation = "", // Reset delegation when governorate changes
            availableDelegations = allRatings
                .filter { it.governorate == governorate }
                .map { it.delegation }
                .distinct()
                .sorted()
        ) }
        updateFilteredData()
    }

    fun onDelegationSelected(delegation: String) {
        _state.update { it.copy(selectedDelegation = delegation) }
        updateFilteredData()
    }

    fun onMacroSectorSelected(sector: String) {
        _state.update { it.copy(
            selectedMacroSector = sector,
            selectedMesoSector = "", // Reset meso sector when macro changes
            availableMesoSectors = SectorConstants.MACRO_SECTORS[sector] ?: emptyList()
        ) }
        updateFilteredData()
    }

    fun onMesoSectorSelected(sector: String) {
        _state.update { it.copy(selectedMesoSector = sector) }
        updateFilteredData()
    }

    fun onIndicatorCategorySelected(category: String) {
        _state.update { it.copy(
            selectedIndicatorCategory = category,
            selectedIndicatorType = "", // Reset indicator type when category changes
            availableIndicatorTypes = SectorConstants.INDICATORS[category] ?: emptyList()
        ) }
        updateFilteredData()
    }

    fun onIndicatorTypeSelected(type: String) {
        _state.update { it.copy(selectedIndicatorType = type) }
        updateFilteredData()
    }

    fun onTimeRangeSelected(timeRange: TimeRange) {
        _state.update { it.copy(selectedTimeRange = timeRange) }
        updateFilteredData()
    }

    fun onRatingRangeChanged(min: Float, max: Float) {
        _state.update { it.copy(minRating = min, maxRating = max) }
        updateFilteredData()
    }
} 