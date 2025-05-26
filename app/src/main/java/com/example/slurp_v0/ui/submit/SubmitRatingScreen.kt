package com.example.slurp_v0.ui.submit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slurp_v0.data.model.SectorConstants
import com.example.slurp_v0.ui.components.CommentField
import com.example.slurp_v0.ui.components.RadioSelector
import com.example.slurp_v0.ui.components.RatingBar
import com.example.slurp_v0.ui.components.SelectorField
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitRatingScreen(
    onRatingSubmitted: () -> Unit = {},
    viewModel: SubmitRatingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    if (state.isSubmitted) {
        LaunchedEffect(Unit) {
            onRatingSubmitted()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Submit Rating") }
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
        } else if (state.success) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Rating Submitted Successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Redirecting to dashboard...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Location",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        SelectorField(
                            label = "Governorate",
                            value = state.selectedGovernorate,
                            options = state.availableGovernorates,
                            onValueChange = viewModel::onGovernorateSelected
                        )
                        
                        SelectorField(
                            label = "Delegation",
                            value = state.selectedDelegation,
                            options = state.availableDelegations,
                            onValueChange = viewModel::onDelegationSelected,
                            enabled = state.selectedGovernorate.isNotBlank()
                        )
                    }
                }

                // Sector Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Sector",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        SelectorField(
                            label = "Macro Sector",
                            value = state.selectedMacroSector,
                            options = SectorConstants.MACRO_SECTORS.keys.toList(),
                            onValueChange = viewModel::onMacroSectorSelected
                        )
                        
                        SelectorField(
                            label = "Meso Sector",
                            value = state.selectedMesoSector,
                            options = state.availableMesoSectors,
                            onValueChange = viewModel::onMesoSectorSelected,
                            enabled = state.selectedMacroSector.isNotBlank()
                        )
                    }
                }

                // Actor Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Actor",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        RadioSelector(
                            label = "Actor Type",
                            options = listOf("Existing Actor", "New Actor"),
                            selectedOption = if (state.isNewActor) "New Actor" else "Existing Actor",
                            onOptionSelected = { viewModel.onNewActorToggled(it == "New Actor") }
                        )
                        
                        if (state.isNewActor) {
                            OutlinedTextField(
                                value = state.actorName,
                                onValueChange = viewModel::onActorNameChanged,
                                label = { Text("Actor Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            SelectorField(
                                label = "Select Actor",
                                value = state.actorName,
                                options = state.availableActors,
                                onValueChange = viewModel::onActorNameChanged,
                                enabled = state.selectedMesoSector.isNotBlank()
                            )
                        }
                    }
                }

                // Rating Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Rating",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        SelectorField(
                            label = "Indicator Category",
                            value = state.selectedIndicatorCategory,
                            options = SectorConstants.INDICATORS.keys.toList(),
                            onValueChange = viewModel::onIndicatorCategorySelected
                        )
                        
                        SelectorField(
                            label = "Indicator Type",
                            value = state.selectedIndicatorType,
                            options = state.availableIndicatorTypes,
                            onValueChange = viewModel::onIndicatorTypeSelected,
                            enabled = state.selectedIndicatorCategory.isNotBlank()
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Your Rating",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RatingBar(
                                rating = state.rating,
                                onRatingChanged = viewModel::onRatingChanged
                            )
                        }
                    }
                }

                // Additional Info Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Additional Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        CommentField(
                            value = state.comment,
                            onValueChange = viewModel::onCommentChanged
                        )
                        
                        Button(
                            onClick = { /* TODO: Implement image selection */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Add Photo"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Photo")
                        }
                    }
                }

                // Photo Upload Section
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Photos",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Photo preview
                            if (state.selectedPhoto != null) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                ) {
                                    AsyncImage(
                                        model = state.selectedPhoto,
                                        contentDescription = "Selected photo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            
                            // Add photo button
                            FilledTonalButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Add photo"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (state.selectedPhoto == null) "Add Photo" else "Change Photo")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.submitRating()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Rating")
                }
            }
        }

        // Show error dialog if there's an error
        if (state.error != null) {
            AlertDialog(
                onDismissRequest = viewModel::clearError,
                title = { Text("Error") },
                text = { Text(state.error!!) },
                confirmButton = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("OK")
                    }
                }
            )
        }
    }
} 