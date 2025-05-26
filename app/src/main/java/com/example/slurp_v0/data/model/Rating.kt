package com.example.slurp_v0.data.model

import com.google.firebase.Timestamp

data class Rating(
    val id: String = "",
    val actorName: String = "",
    val governorate: String = "",
    val delegation: String = "",
    val macroSector: String = "",
    val mesoSector: String = "",
    val indicatorCategory: String = "",
    val indicatorType: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
) 