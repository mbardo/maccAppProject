package com.example.maccappproject.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.maccappproject.screens.*

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("drawing") { DrawingScreen(navController) }
        composable("gallery") { GalleryScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}