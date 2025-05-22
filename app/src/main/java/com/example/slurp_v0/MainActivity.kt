package com.example.slurp_v0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.slurp_v0.ui.auth.AuthScreen
import com.example.slurp_v0.ui.components.BottomNavBar
import com.example.slurp_v0.ui.navigation.NavGraph
import com.example.slurp_v0.ui.theme.SLURP_v0Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SLURP_v0Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isAuthenticated by remember { mutableStateOf(false) }

                    if (!isAuthenticated) {
                        AuthScreen(
                            onAuthSuccess = { isAuthenticated = true }
                        )
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}