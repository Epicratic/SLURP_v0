package com.example.slurp_v0.ui.profile

import com.example.slurp_v0.data.model.Rating

data class ProfileDataState(
    val email: String = "",
    val registrationDate: String = "",
    val totalRatings: Int = 0,
    val userRank: Int = 0,
    val averageRating: Float = 0f,
    val bestActor: String = "",
    val worstActor: String = "",
    val averagePerSector: Map<String, Float> = emptyMap(),
    val sharedRatings: List<Rating> = emptyList(),
    val badges: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)