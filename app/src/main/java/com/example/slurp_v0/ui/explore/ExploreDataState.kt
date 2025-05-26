package com.example.slurp_v0.ui.explore

import com.example.slurp_v0.data.model.Rating

data class ExploreDataState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filters
    val selectedGovernorate: String = "",
    val selectedDelegation: String = "",
    val selectedMacroSector: String = "",
    val selectedMesoSector: String = "",
    val selectedIndicatorCategory: String = "",
    val selectedIndicatorType: String = "",
    val selectedTimeRange: TimeRange = TimeRange.ALL,
    val minRating: Float = 0f,
    val maxRating: Float = 5f,
    
    // Available options
    val availableGovernorates: List<String> = emptyList(),
    val availableDelegations: List<String> = emptyList(),
    val availableMacroSectors: List<String> = emptyList(),
    val availableMesoSectors: List<String> = emptyList(),
    val availableIndicatorCategories: List<String> = emptyList(),
    val availableIndicatorTypes: List<String> = emptyList(),
    
    // Data
    val delegationAverages: Map<String, Double> = emptyMap(),
    val mapData: Map<String, Double> = emptyMap(),
    val filteredRatings: List<Rating> = emptyList()
)

enum class TimeRange(val label: String, val days: Int?) {
    ALL("All Time", null),
    LAST_WEEK("Last Week", 7),
    LAST_MONTH("Last Month", 30),
    LAST_QUARTER("Last Quarter", 90),
    LAST_YEAR("Last Year", 365)
} 