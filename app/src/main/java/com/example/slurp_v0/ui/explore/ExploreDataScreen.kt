package com.example.slurp_v0.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slurp_v0.R
import com.example.slurp_v0.data.model.Rating
import com.example.slurp_v0.data.model.SectorConstants
import com.example.slurp_v0.ui.components.SelectorField
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreDataScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreDataViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Explore Data") },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
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
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Filters
                if (showFilters) {
                    item {
                        FiltersSection(
                            state = state,
                            onGovernorateSelected = viewModel::onGovernorateSelected,
                            onDelegationSelected = viewModel::onDelegationSelected,
                            onMacroSectorSelected = viewModel::onMacroSectorSelected,
                            onMesoSectorSelected = viewModel::onMesoSectorSelected,
                            onIndicatorCategorySelected = viewModel::onIndicatorCategorySelected,
                            onIndicatorTypeSelected = viewModel::onIndicatorTypeSelected,
                            onTimeRangeSelected = viewModel::onTimeRangeSelected,
                            onRatingRangeChanged = viewModel::onRatingRangeChanged
                        )
                    }
                }

                // Table View
                item {
                    DelegationTableCard(
                        delegationAverages = state.delegationAverages,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Chart View
                item {
                    ChartCard(
                        title = "Rating Distribution",
                        data = state.delegationAverages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }

                // Map View
                item {
                    MapCard(
                        mapData = state.mapData,
                        selectedGovernorate = state.selectedGovernorate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FiltersSection(
    state: ExploreDataState,
    onGovernorateSelected: (String) -> Unit,
    onDelegationSelected: (String) -> Unit,
    onMacroSectorSelected: (String) -> Unit,
    onMesoSectorSelected: (String) -> Unit,
    onIndicatorCategorySelected: (String) -> Unit,
    onIndicatorTypeSelected: (String) -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onRatingRangeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Time Range
            SelectorField(
                label = "Time Range",
                value = state.selectedTimeRange.label,
                options = TimeRange.values().map { it.label },
                onValueChange = { label ->
                    TimeRange.values()
                        .find { it.label == label }
                        ?.let(onTimeRangeSelected)
                }
            )

            // Location
            SelectorField(
                label = "Governorate",
                value = state.selectedGovernorate,
                options = state.availableGovernorates,
                onValueChange = onGovernorateSelected
            )

            SelectorField(
                label = "Delegation",
                value = state.selectedDelegation,
                options = state.availableDelegations,
                onValueChange = onDelegationSelected,
                enabled = state.selectedGovernorate.isNotEmpty()
            )

            // Sector
            SelectorField(
                label = "Macro Sector",
                value = state.selectedMacroSector,
                options = state.availableMacroSectors,
                onValueChange = onMacroSectorSelected
            )

            SelectorField(
                label = "Meso Sector",
                value = state.selectedMesoSector,
                options = state.availableMesoSectors,
                onValueChange = onMesoSectorSelected,
                enabled = state.selectedMacroSector.isNotEmpty()
            )

            // Indicator
            SelectorField(
                label = "Indicator Category",
                value = state.selectedIndicatorCategory,
                options = state.availableIndicatorCategories,
                onValueChange = onIndicatorCategorySelected
            )

            SelectorField(
                label = "Indicator Type",
                value = state.selectedIndicatorType,
                options = state.availableIndicatorTypes,
                onValueChange = onIndicatorTypeSelected,
                enabled = state.selectedIndicatorCategory.isNotEmpty()
            )

            // Rating Range
            Column {
                Text(
                    "Rating Range",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${state.minRating.toInt()}")
                    RangeSlider(
                        value = state.minRating..state.maxRating,
                        onValueChange = { range ->
                            onRatingRangeChanged(range.start, range.endInclusive)
                        },
                        valueRange = 0f..5f,
                        steps = 4,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("${state.maxRating.toInt()}")
                }
            }
        }
    }
}

@Composable
fun DelegationTableCard(
    delegationAverages: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Delegation Ratings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Delegation",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Average Rating",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Table Content
            delegationAverages.forEach { (delegation, average) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        delegation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        String.format("%.1f", average),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MapCard(
    mapData: Map<String, Double>,
    selectedGovernorate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Rating Distribution Map",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Base map image with labels
                Image(
                    painter = painterResource(id = R.drawable.tunisia_map_with_labels),
                    contentDescription = "Tunisia Map",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // Color overlays and ratings for each governorate
                mapData.forEach { (governorate, rating) ->
                    val color = when {
                        rating < 1.0 -> Color.Red
                        rating < 2.0 -> Color(0xFFFF8C00) // Dark Orange
                        rating < 3.0 -> Color.Yellow
                        rating < 4.0 -> Color(0xFF90EE90) // Light Green
                        else -> Color(0xFF006400) // Dark Green
                    }
                    
                    // Get predefined position for this governorate
                    val position = getGovernoratePosition(governorate)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(position)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = color.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Text(
                                text = String.format("%.1f", rating),
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Legend
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        "Rating Scale",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    listOf(
                        "< 1.0" to Color.Red,
                        "1.0 - 1.9" to Color(0xFFFF8C00),
                        "2.0 - 2.9" to Color.Yellow,
                        "3.0 - 3.9" to Color(0xFF90EE90),
                        "4.0 - 5.0" to Color(0xFF006400)
                    ).forEach { (range, color) ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(range, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun getGovernoratePosition(governorate: String): PaddingValues {
    return when (governorate) {
        "Tunis" -> PaddingValues(top = 95.dp, start = 175.dp)
        "Ariana" -> PaddingValues(top = 75.dp, start = 155.dp)
        "Ben Arous" -> PaddingValues(top = 105.dp, start = 190.dp)
        "Manouba" -> PaddingValues(top = 85.dp, start = 135.dp)
        "Bizerte" -> PaddingValues(top = 55.dp, start = 155.dp)
        "Béja" -> PaddingValues(top = 95.dp, start = 115.dp)
        "Jendouba" -> PaddingValues(top = 75.dp, start = 75.dp)
        "Kef" -> PaddingValues(top = 135.dp, start = 95.dp)
        "Siliana" -> PaddingValues(top = 155.dp, start = 135.dp)
        "Zaghouan" -> PaddingValues(top = 135.dp, start = 175.dp)
        "Nabeul" -> PaddingValues(top = 115.dp, start = 215.dp)
        "Sousse" -> PaddingValues(top = 175.dp, start = 215.dp)
        "Monastir" -> PaddingValues(top = 195.dp, start = 235.dp)
        "Mahdia" -> PaddingValues(top = 215.dp, start = 215.dp)
        "Kairouan" -> PaddingValues(top = 195.dp, start = 175.dp)
        "Kasserine" -> PaddingValues(top = 215.dp, start = 115.dp)
        "Sidi Bouzid" -> PaddingValues(top = 235.dp, start = 155.dp)
        "Sfax" -> PaddingValues(top = 255.dp, start = 195.dp)
        "Gafsa" -> PaddingValues(top = 275.dp, start = 115.dp)
        "Tozeur" -> PaddingValues(top = 295.dp, start = 75.dp)
        "Kebili" -> PaddingValues(top = 315.dp, start = 135.dp)
        "Gabes" -> PaddingValues(top = 315.dp, start = 175.dp)
        "Medenine" -> PaddingValues(top = 335.dp, start = 195.dp)
        "Tataouine" -> PaddingValues(top = 355.dp, start = 175.dp)
        else -> PaddingValues(0.dp)
    }
}

@Composable
fun ChartCard(
    title: String,
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
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

            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
            val context = LocalContext.current

            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        setPinchZoom(false)
                        setScaleEnabled(true)
                        legend.isEnabled = true
                        
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(SectorConstants.MACRO_SECTORS.keys.toList())
                            labelRotationAngle = -45f
                        }
                        
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 5f
                            granularity = 1f
                            setDrawGridLines(true)
                        }
                        
                        axisRight.isEnabled = false
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { chart ->
                    // Group data by governorate and create a line for each
                    val dataSets = data.entries.groupBy { it.key.split(" - ")[0] }
                        .map { (governorate, entries) ->
                            val dataPoints = entries.mapIndexed { index, entry ->
                                Entry(index.toFloat(), entry.value.toFloat())
                            }
                            
                            LineDataSet(dataPoints, governorate).apply {
                                color = Color(Random().nextInt()).toArgb()
                                setDrawValues(false)
                                setDrawCircles(true)
                                lineWidth = 2f
                                setMode(LineDataSet.Mode.LINEAR)
                            }
                        }
                    
                    chart.data = LineData(dataSets)
                    chart.invalidate()
                }
            )
        }
    }
} 