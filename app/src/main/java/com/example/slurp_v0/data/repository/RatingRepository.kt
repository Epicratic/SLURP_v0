package com.example.slurp_v0.data.repository

import com.example.slurp_v0.data.model.Rating
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val db = Firebase.firestore
    private val ratingsCollection = db.collection("ratings")

    suspend fun getRatings(): List<Rating> {
        return try {
            ratingsCollection.get().await().toObjects(Rating::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to load ratings: ${e.message}")
        }
    }

    suspend fun submitRating(rating: Rating) {
        try {
            ratingsCollection.document(rating.id).set(rating).await()
        } catch (e: Exception) {
            throw Exception("Failed to submit rating: ${e.message}")
        }
    }

    suspend fun getActorsForSector(mesoSector: String): List<String> {
        return try {
            val ratings = ratingsCollection
                .whereEqualTo("mesoSector", mesoSector)
                .get()
                .await()
                .toObjects(Rating::class.java)
            ratings.map { it.actorName }.distinct().sorted()
        } catch (e: Exception) {
            throw Exception("Failed to load actors: ${e.message}")
        }
    }
} 