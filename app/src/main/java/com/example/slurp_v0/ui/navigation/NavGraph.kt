package com.example.slurp_v0.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.slurp_v0.ui.dashboard.DashboardScreen
import com.example.slurp_v0.ui.explore.ExploreDataScreen
import com.example.slurp_v0.ui.submit.SubmitRatingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen()
        }
        
        composable(route = Screen.SubmitRating.route) {
            SubmitRatingScreen(
                onRatingSubmitted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.ExploreData.route) {
            ExploreDataScreen()
        }
        
        composable(route = Screen.Profile.route) {
            // ProfileScreen will be implemented later
        }
    }
}

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object SubmitRating : Screen("submit_rating")
    object ExploreData : Screen("explore_data")
    object Profile : Screen("profile")
} 