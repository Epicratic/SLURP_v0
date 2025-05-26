package com.example.slurp_v0.data.model

object SectorConstants {
    val MACRO_SECTORS = mapOf(
        "Governance" to listOf("Parliament", "Government", "Justice"),
        "Security-Defense" to listOf("Police", "Firefighters", "Army"),
        "Health-Hygiene" to listOf("Hospital", "Pharmacy", "Waste management", "Sanitation", "Infrastructure"),
        "Instruction-Culture" to listOf("Education", "Training", "Arts", "Sports", "Religion"),
        "Food-Finance" to listOf("Retail", "Restaurants", "Banking", "Insurance"),
        "Housing-Tourism" to listOf("Design", "Real Estate", "Resorts", "Campings"),
        "Transport-Telco" to listOf("Bus", "Taxi", "Metro", "Ferries", "Airlines", "Cellular", "Broadband")
    )

    val INDICATORS = mapOf(
        "Facility" to listOf("Style", "Cleanliness"),
        "Staff" to listOf("Efficiency", "Courtesy"),
        "Programs" to listOf("Diversity", "Pricing")
    )
} 