package com.example.slurp_v0.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slurp_v0.data.model.Rating
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error!!, color = colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Global Average Rating Card
                item {
                    GlobalAverageCard(
                        average = state.globalAverage,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Territory Chart
                item {
                    ChartCard(
                        title = "Ratings by Territory",
                        data = state.territoryAverages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }

                // Sector Chart
                item {
                    ChartCard(
                        title = "Ratings by Sector",
                        data = state.sectorAverages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }

                // Top Actors
                item {
                    TopActorsCard(
                        actors = state.topActors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Recent Ratings
                item {
                    Text(
                        "Recent Ratings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(state.recentRatings) { rating ->
                    RatingCard(rating = rating)
                }
            }
        }
    }
}

@Composable
fun GlobalAverageCard(
    average: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Global Average Rating",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                String.format("%.1f", average),
                style = MaterialTheme.typography.displayLarge,
                color = colorScheme.primary
            )
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        legend.isEnabled = false
                        
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.valueFormatter = IndexAxisValueFormatter(data.keys.toList())
                        xAxis.labelRotationAngle = -45f
                        
                        axisRight.isEnabled = false
                        axisLeft.axisMinimum = 0f
                        axisLeft.axisMaximum = 5f
                        
                        setFitBars(true)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                update = { chart ->
                    val entries = data.values.mapIndexed { index, value ->
                        BarEntry(index.toFloat(), value.toFloat())
                    }
                    
                    val dataSet = BarDataSet(entries, "").apply {
                        color = android.graphics.Color.parseColor("#6650a4") // Primary color
                        valueTextSize = 10f
                    }
                    
                    chart.data = BarData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun TopActorsCard(
    actors: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Top Actors",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            actors.forEachIndexed { index, (name, rating) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = when(index) {
                                0 -> Color(0xFFFFD700) // Gold
                                1 -> Color(0xFFC0C0C0) // Silver
                                else -> Color(0xFFCD7F32) // Bronze
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${index + 1}",
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        String.format("%.1f", rating),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RatingCard(
    rating: Rating,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    rating.actorName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${rating.rating}/5",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "${rating.macroSector} - ${rating.mesoSector}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            
            Text(
                "${rating.governorate}, ${rating.delegation}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            
            if (rating.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    rating.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                dateFormat.format(rating.timestamp.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
} 