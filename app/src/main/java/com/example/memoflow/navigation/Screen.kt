package com.example.memoflow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen") // Adicionamos a rota do perfil aqui
}