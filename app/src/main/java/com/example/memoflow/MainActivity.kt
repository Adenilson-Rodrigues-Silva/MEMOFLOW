package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.screens.home.HomeScreen
import com.example.memoflow.ui.screens.profile.ProfileScreen
import com.example.memoflow.ui.screens.profile.NotificationCenterScreen
import com.example.memoflow.ui.screens.profile.BackupScreen
import com.example.memoflow.ui.screens.security.SecurityScreen
import com.example.memoflow.ui.screens.splash.SplashScreen
import com.example.memoflow.ui.screens.common.UnderConstructionScreen
import com.example.memoflow.ui.screens.note.WriteNoteScreen
import com.example.memoflow.ui.screens.stats.StatisticsScreen
import com.example.memoflow.ui.screens.gratitude.GratitudeScreen
import com.example.memoflow.ui.screens.recall.RecallScreen
import com.example.memoflow.ui.screens.store.StoreScreen
import com.example.memoflow.ui.screens.map.PlacesMapScreen
import com.example.memoflow.ui.screens.auth.WelcomeAuthScreen
import com.example.memoflow.ui.theme.MemoFlowTheme
import com.example.memoflow.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            MemoFlowTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)

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
                        SplashScreen(onFinished = { hasSeenWelcome ->
                            if (hasSeenWelcome) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.WelcomeAuth.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        })
                    }

                    composable(Screen.WelcomeAuth.route) {
                        val authState = authViewModel.uiState.collectAsState().value
                        
                        WelcomeAuthScreen(
                            onSkip = { authViewModel.skipSignIn() },
                            viewModel = authViewModel
                        )
                        
                        LaunchedEffect(authState.isSuccess) {
                            if (authState.isSuccess) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.WelcomeAuth.route) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(navController = navController)
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onSecurityClick = { navController.navigate("security") },
                            onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                            onBackupClick = { navController.navigate(Screen.Backup.route) }
                        )
                    }

                    composable(Screen.Notifications.route) {
                        NotificationCenterScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Screen.Backup.route) {
                        BackupScreen(onBack = { navController.popBackStack() })
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

                    composable(Screen.Store.route) {
                        StoreScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Screen.PlacesMap.route) {
                        PlacesMapScreen(onBack = { navController.popBackStack() })
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
