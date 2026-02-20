package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.screens.HomeScreen
import com.example.memoflow.ui.screens.ProfileScreen
import com.example.memoflow.ui.screens.SplashScreen
import com.example.memoflow.ui.theme.MemoFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoFlowTheme {
                // 1. Criamos o controlador de navegação
                val navController = rememberNavController()

                // 2. Definimos o NavHost (O motor de telas)
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route // Começa pela Splash
                ) {
                    // Rota da Splash Screen
                    composable(Screen.Splash.route) {
                        SplashScreen(onFinished = {
                            // Após os 2 segundos, vai para a Home e limpa a Splash da pilha
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        })
                    }

                    // Rota da Home Screen
                    composable(Screen.Home.route) {
                        // Passamos o navController para a HomeScreen poder abrir o Perfil
                        HomeScreen(navController = navController)
                    }

                    // Rota da Profile Screen
                    composable(Screen.Profile.route) {
                        ProfileScreen(onBack = {
                            // Volta para a tela anterior (Home)
                            navController.popBackStack()
                        })
                    }
                }
            }
        }
    }
}