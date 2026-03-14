package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.core.view.WindowCompat
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
import com.example.memoflow.ui.screens.stats.StatisticsScreen
import com.example.memoflow.ui.screens.gratitude.GratitudeScreen
import com.example.memoflow.ui.screens.recall.RecallScreen
import com.example.memoflow.ui.theme.MemoFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CORREÇÃO CRUCIAL: Habilita o modo Edge-to-Edge e diz ao sistema 
        // para não ajustar o layout automaticamente pelo teclado (o Compose fará isso)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            MemoFlowTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    }
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

                    composable(Screen.Statistics.route) {
                        StatisticsScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Screen.Gratitude.route) {
                        GratitudeScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Screen.Recall.route) {
                        RecallScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = "write_note?noteId={noteId}&readOnly={readOnly}",
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            },
                            navArgument("readOnly") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                        val readOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: false
                        WriteNoteScreen(
                            onBack = { navController.popBackStack() },
                            noteId = if (noteId == -1L) null else noteId,
                            readOnly = readOnly
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
