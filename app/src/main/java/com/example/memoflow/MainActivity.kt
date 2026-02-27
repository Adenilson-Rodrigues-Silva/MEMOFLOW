package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.screens.home.HomeScreen
import com.example.memoflow.ui.screens.profile.ProfileScreen
import com.example.memoflow.ui.screens.security.SecurityScreen
import com.example.memoflow.ui.screens.splash.SplashScreen
import com.example.memoflow.ui.screens.common.UnderConstructionScreen
import com.example.memoflow.ui.screens.note.WriteNoteScreen
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

                    composable(
                        route = "write_note?noteId={noteId}",
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                        WriteNoteScreen(
                            onBack = { navController.popBackStack() },
                            noteId = if (noteId == -1L) null else noteId
                        )
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
