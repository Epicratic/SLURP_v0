package com.example.slurp_v0.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.slurp_v0.data.model.Rating

@Composable
fun ProfileDataScreen(
    state: ProfileDataState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserInfoCard(email = state.email, registrationDate = state.registrationDate)
        }

        item {
            RatingActivityCard(totalRatings = state.totalRatings, rank = state.userRank)
        }

        item {
            RatingSummaryCard(
                average = state.averageRating,
                bestActor = state.bestActor,
                worstActor = state.worstActor
            )
        }

        item {
            SectorRatingsCard(sectorAverages = state.averagePerSector)
        }

        item {
            RatingsListCard(ratings = state.sharedRatings)
        }

        item {
            BadgesCard(badges = state.badges)
        }
    }
}

@Composable
fun UserInfoCard(email: String, registrationDate: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User Info", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Email: $email")
            Text("Registered on: $registrationDate")
        }
    }
}

@Composable
fun RatingActivityCard(totalRatings: Int, rank: Int) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Activity", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total ratings shared: $totalRatings")
            Text("Rank: #$rank among users")
        }
    }
}

@Composable
fun RatingSummaryCard(average: Float, bestActor: String, worstActor: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rating Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Average rating: %.2f ★".format(average))
            Text("Best-rated actor: $bestActor")
            Text("Worst-rated actor: $worstActor")
        }
    }
}

@Composable
fun SectorRatingsCard(sectorAverages: Map<String, Float>) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ratings by Sector", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            sectorAverages.forEach { (sector, avg) ->
                Text("$sector: %.2f ★".format(avg))
            }
        }
    }
}

@Composable
fun RatingsListCard(ratings: List<Rating>) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Ratings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (ratings.isEmpty()) {
                Text("No ratings shared yet.")
            } else {
                ratings.forEach { rating ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Actor: ${rating.actorName}")
                        Text("Sector: ${rating.macroSector}")
                        Text("Rating: ${rating.rating} ★")
                        Text("Date: ${rating.timestamp}") // Adapt if timestamp is a Date or Long
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesCard(badges: List<String>) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Badges", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (badges.isEmpty()) {
                Text("No badges earned yet.")
            } else {
                badges.forEach { badge ->
                    Text("\uD83C\uDFC5 $badge")
                }
            }
        }
    }
}
