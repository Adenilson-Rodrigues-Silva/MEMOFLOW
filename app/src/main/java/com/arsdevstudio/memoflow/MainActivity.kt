package com.arsdevstudio.memoflow

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
import androidx.navigation.navDeepLink
import com.arsdevstudio.memoflow.navigation.Screen
import com.arsdevstudio.memoflow.ui.screens.home.HomeScreen
import com.arsdevstudio.memoflow.ui.screens.profile.ProfileScreen
import com.arsdevstudio.memoflow.ui.screens.profile.NotificationCenterScreen
import com.arsdevstudio.memoflow.ui.screens.profile.BackupScreen
import com.arsdevstudio.memoflow.ui.screens.security.SecurityScreen
import com.arsdevstudio.memoflow.ui.screens.splash.SplashScreen
import com.arsdevstudio.memoflow.ui.screens.common.UnderConstructionScreen
import com.arsdevstudio.memoflow.ui.screens.note.WriteNoteScreen
import com.arsdevstudio.memoflow.ui.screens.stats.StatisticsScreen
import com.arsdevstudio.memoflow.ui.screens.gratitude.GratitudeScreen
import com.arsdevstudio.memoflow.ui.screens.recall.RecallScreen
import com.arsdevstudio.memoflow.ui.screens.store.StoreScreen
import com.arsdevstudio.memoflow.ui.screens.map.PlacesMapScreen
import com.arsdevstudio.memoflow.ui.screens.auth.WelcomeAuthScreen
import com.arsdevstudio.memoflow.ui.theme.MemoFlowTheme
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel
import com.arsdevstudio.memoflow.ui.viewmodel.AuthEvent

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
                        SplashScreen(onFinished = { isLogged, hasSeenWelcome ->
                            when {
                                isLogged -> {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                                !hasSeenWelcome -> {
                                    navController.navigate(Screen.Onboarding.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                                else -> {
                                    navController.navigate(Screen.WelcomeAuth.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            }
                        })
                    }

                    composable(Screen.Onboarding.route) {
                        // Implementação da OnboardingScreen virá a seguir
                        OnboardingScreen(
                            onFinished = {
                                navController.navigate(Screen.WelcomeAuth.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.WelcomeAuth.route) {
                        LaunchedEffect(Unit) {
                            authViewModel.events.collect { event ->
                                if (event is AuthEvent.LoginSuccess) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.WelcomeAuth.route) { inclusive = true }
                                    }
                                }
                            }
                        }

                        WelcomeAuthScreen(
                            viewModel = authViewModel
                        )
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(navController = navController)
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onSecurityClick = { navController.navigate("security") },
                            onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                            onBackupClick = { navController.navigate(Screen.Backup.route) },
                            onStoreClick = { navController.navigate(Screen.Store.route) },
                            onLogoutSuccess = {
                                navController.navigate(Screen.WelcomeAuth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
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

                    composable(
                        route = Screen.Gratitude.route,
                        deepLinks = listOf(navDeepLink { uriPattern = "memoflow://gratitude" })
                    ) {
                        GratitudeScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = Screen.Recall.route,
                        deepLinks = listOf(navDeepLink { uriPattern = "memoflow://recall" })
                    ) {
                        RecallScreen(onBack = { navController.popBackStack() })
                    }

                    composable(Screen.Store.route) {
                        StoreScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = Screen.PlacesMap.route,
                        deepLinks = listOf(navDeepLink { uriPattern = "memoflow://map" })
                    ) {
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
                        ),
                        deepLinks = listOf(navDeepLink { uriPattern = "memoflow://write_note" })
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

