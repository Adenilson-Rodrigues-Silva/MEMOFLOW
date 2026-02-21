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
import com.example.memoflow.ui.screens.SecurityScreen
import com.example.memoflow.ui.screens.SplashScreen
import com.example.memoflow.ui.screens.UnderConstructionScreen
import com.example.memoflow.ui.screens.WriteNoteScreen
import com.example.memoflow.ui.theme.MemoFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoFlowTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(onFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        })
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(navController = navController)
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onSecurityClick = { navController.navigate("security") },
                            onBackupClick = { navController.navigate("construction") }
                        )
                    }

                    // ROTA DA TELA DE NOTAS (A que acabamos de criar)
                    composable("write_note") {
                        WriteNoteScreen(onBack = { navController.popBackStack() })
                    }

                    composable("security") {
                        SecurityScreen(onBack = { navController.popBackStack() })
                    }

                    composable("construction") {
                        UnderConstructionScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}