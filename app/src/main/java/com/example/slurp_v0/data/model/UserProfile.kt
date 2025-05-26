package com.example.slurp_v0.data.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val totalRatings: Int = 0,
    val averageRating: Double = 0.0,
    val sectorAverages: Map<String, Double> = emptyMap(),
    val lastActive: Long = System.currentTimeMillis(),
    val isEmailVerified: Boolean = false
) 