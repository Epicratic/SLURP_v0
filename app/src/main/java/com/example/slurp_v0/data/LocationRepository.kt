package com.example.slurp_v0.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocationData(
    val version: String,
    val lastUpdated: String,
    val governorates: List<Governorate>
)

data class Governorate(
    val id: String,
    val name: Names,
    val delegations: List<Delegation>
)

data class Delegation(
    val id: String,
    val name: Names
)

data class Names(
    val fr: String
)

class LocationRepository(private val context: Context) {
    private var locationData: LocationData? = null

    suspend fun getGovernorateNames(): List<String> = withContext(Dispatchers.IO) {
        loadLocationsIfNeeded()
        locationData?.governorates?.map { it.name.fr } ?: emptyList()
    }

    suspend fun getDelegationsForGovernorate(governorateName: String): List<String> = withContext(Dispatchers.IO) {
        loadLocationsIfNeeded()
        locationData?.governorates
            ?.find { it.name.fr == governorateName }
            ?.delegations
            ?.map { it.name.fr }
            ?: emptyList()
    }

    private suspend fun loadLocationsIfNeeded() {
        if (locationData == null) {
            withContext(Dispatchers.IO) {
                try {
                    val jsonString = context.assets.open("data/locations.json").bufferedReader().use { it.readText() }
                    locationData = Gson().fromJson(jsonString, LocationData::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // If loading fails, use empty data
                    locationData = LocationData("1.0", "2024", emptyList())
                }
            }
        }
    }
} 