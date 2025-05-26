package com.example.slurp_v0.data

import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection("ratings")
    private val usersCollection = db.collection("users")

    suspend fun getAllRatings(): List<Rating> {
        return ratingsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Rating::class.java)
    }

    suspend fun getGlobalAverageRating(): Double {
        val snapshot = ratingsCollection.get().await()
        if (snapshot.isEmpty) return 0.0
        val sum = snapshot.documents.sumOf { it.getLong("rating")?.toDouble() ?: 0.0 }
        return sum / snapshot.size()
    }

    suspend fun getAverageByDimension(dimension: String): Map<String, Double> {
        val snapshot = ratingsCollection.get().await()
        return snapshot.documents
            .groupBy { it.getString(dimension) ?: "Unknown" }
            .mapValues { (_, ratings) ->
                val sum = ratings.sumOf { it.getLong("rating")?.toDouble() ?: 0.0 }
                sum / ratings.size
            }
    }

    suspend fun getTopActors(limit: Int = 3): List<Pair<String, Double>> {
        return ratingsCollection
            .get()
            .await()
            .documents
            .groupBy { it.getString("actorName") ?: "Unknown" }
            .mapValues { (_, ratings) ->
                val sum = ratings.sumOf { it.getLong("rating")?.toDouble() ?: 0.0 }
                sum / ratings.size
            }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
    }

    suspend fun getRecentRatings(limit: Int = 10): List<Rating> {
        return ratingsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(Rating::class.java)
    }

    // User Profile methods
    suspend fun getUserProfile(userId: String): UserProfile? {
        return usersCollection
            .document(userId)
            .get()
            .await()
            .toObject(UserProfile::class.java)
    }

    suspend fun getUserRatings(userId: String): List<Rating> {
        return ratingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Rating::class.java)
    }

    suspend fun getUserAverageRating(userId: String): Double {
        val ratings = getUserRatings(userId)
        if (ratings.isEmpty()) return 0.0
        return ratings.map { it.rating.toDouble() }.average()
    }

    suspend fun getUserSectorAverages(userId: String): Map<String, Double> {
        val ratings = getUserRatings(userId)
        return ratings
            .groupBy { it.macroSector }
            .mapValues { (_, sectorRatings) ->
                sectorRatings.map { it.rating.toDouble() }.average()
            }
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        usersCollection.document(userProfile.userId)
            .set(userProfile)
            .await()
    }

    suspend fun submitRating(rating: Rating) {
        ratingsCollection.add(rating).await()
    }

    suspend fun getActorsForSector(mesoSector: String): List<String> {
        return ratingsCollection
            .whereEqualTo("mesoSector", mesoSector)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("actorName") }
            .distinct()
    }

    // New function to get all user profiles
    suspend fun getAllUserProfiles(): List<UserProfile> {
        return usersCollection
            .get()
            .await()
            .toObjects(UserProfile::class.java)
    }
}