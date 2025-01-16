package com.example.maccappproject.navigation

// app/src/main/java/com.example.maccappproject/navigation/AppNavigation.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maccappproject.screens.DrawingScreen
import com.example.maccappproject.screens.GalleryScreen
import com.example.maccappproject.screens.HomeScreen
import com.example.maccappproject.screens.LoginScreen
import com.example.maccappproject.screens.SettingsScreen
import com.example.maccappproject.screens.SignupScreen

object Screen {
    const val LOGIN = "login"
    const val SIGNUP = "signup"  // Add this
    const val HOME = "home"
    const val DRAWING = "drawing"
    const val GALLERY = "gallery"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LOGIN
    ) {
        composable(Screen.LOGIN) {
            LoginScreen(navController)
        }
        composable(Screen.SIGNUP) {
            SignupScreen(navController)
        }
        composable(Screen.HOME) {
            HomeScreen(navController)
        }
        composable(Screen.DRAWING) {
            DrawingScreen(navController)
        }
        composable(Screen.GALLERY) {
            GalleryScreen(navController)
        }
        composable(Screen.SETTINGS) {
            SettingsScreen(navController)
        }
    }
}