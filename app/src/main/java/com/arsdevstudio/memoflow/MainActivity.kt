package com.arsdevstudio.memoflow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.arsdevstudio.memoflow.navigation.Screen
import com.arsdevstudio.memoflow.ui.screens.auth.OnboardingScreen
import com.arsdevstudio.memoflow.ui.screens.auth.WelcomeAuthScreen
import com.arsdevstudio.memoflow.ui.screens.common.UnderConstructionScreen
import com.arsdevstudio.memoflow.ui.screens.gratitude.GratitudeScreen
import com.arsdevstudio.memoflow.ui.screens.home.HomeScreen
import com.arsdevstudio.memoflow.ui.screens.map.PlacesMapScreen
import com.arsdevstudio.memoflow.ui.screens.note.WriteNoteScreen
import com.arsdevstudio.memoflow.ui.screens.profile.BackupScreen
import com.arsdevstudio.memoflow.ui.screens.profile.NotificationCenterScreen
import com.arsdevstudio.memoflow.ui.screens.profile.ProfileScreen
import com.arsdevstudio.memoflow.ui.screens.profile.ProfileViewModel
import com.arsdevstudio.memoflow.ui.screens.recall.RecallScreen
import com.arsdevstudio.memoflow.ui.screens.security.SecurityScreen
import com.arsdevstudio.memoflow.ui.screens.splash.SplashScreen
import com.arsdevstudio.memoflow.ui.screens.stats.StatisticsScreen
import com.arsdevstudio.memoflow.ui.screens.store.StoreScreen
import com.arsdevstudio.memoflow.ui.components.home.SupportDevDialog
import com.arsdevstudio.memoflow.ui.components.home.SupportParticle
import com.arsdevstudio.memoflow.ui.theme.MemoFlowTheme
import com.arsdevstudio.memoflow.ui.viewmodel.AuthEvent
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            MemoFlowTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)

                val userSettingsState = profileViewModel.userSettings.collectAsState()
                val userSettings = userSettingsState.value
                var showSupportDevDialog by remember { mutableStateOf(false) }
                val particles = remember { mutableStateListOf<SupportParticle>() }

                LaunchedEffect(showSupportDevDialog) {
                    if (showSupportDevDialog) {
                        val symbols = listOf("✨", "💎", "💖", "💎", "💖")
                        val random = java.util.Random()
                        while (true) {
                            if (particles.size < 100) {
                                particles.add(
                                    SupportParticle(
                                        id = random.nextInt(),
                                        x = (random.nextFloat() - 0.5f) * 300f,
                                        y = (random.nextFloat() - 0.5f) * 300f,
                                        vx = (random.nextFloat() - 0.5f) * 6f,
                                        vy = (random.nextFloat() - 0.5f) * 6f - 2f,
                                        alpha = 1f,
                                        scale = 0.5f + random.nextFloat() * 1.0f,
                                        symbol = symbols[random.nextInt(symbols.size)]
                                    )
                                )
                            }
                            val iterator = particles.listIterator()
                            while (iterator.hasNext()) {
                                val p = iterator.next()
                                val newAlpha = p.alpha - 0.008f
                                if (newAlpha <= 0) {
                                    iterator.remove()
                                } else {
                                    iterator.set(p.copy(
                                        x = p.x + p.vx,
                                        y = p.y + p.vy,
                                        vy = p.vy - 0.15f,
                                        alpha = newAlpha
                                    ))
                                }
                            }
                            delay(16)
                        }
                    } else {
                        particles.clear()
                    }
                }

                // Lógica de verificação do diálogo de suporte (Uma vez ao dia em dias ímpares)
                val billingPrefs = remember { com.arsdevstudio.memoflow.utils.BillingPrefs(this@MainActivity) }
                val lastShownDate by billingPrefs.lastSupportPopupDate.collectAsState(initial = "")
                
                LaunchedEffect(userSettings?.appEntryCount) {
                    userSettings?.let { settings ->
                        val count = settings.appEntryCount
                        val today = java.time.LocalDate.now().toString()
                        val isOddDay = java.time.LocalDate.now().dayOfMonth % 2 != 0

                        if (isOddDay && count >= 3 && lastShownDate != today) {
                            Log.i("MemoFlow_Debug", "MainActivity - EXIBINDO DIÁLOGO (Hoje: $today, Count: $count)")
                            delay(2000)
                            showSupportDevDialog = true
                            billingPrefs.setLastSupportPopupDate(today)
                        }
                    }
                }
                
                // Lógica Biometria (dispara 1 vez na inicialização)
                var biometricDone by remember { mutableStateOf(false) }
                LaunchedEffect(userSettings?.isBiometricEnabled) {
                    if (userSettings?.isBiometricEnabled == true && !biometricDone) {
                        biometricDone = true
                        val executor = ContextCompat.getMainExecutor(this@MainActivity)
                        val biometricPrompt = BiometricPrompt(this@MainActivity, executor, 
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    Log.d("Biometria", "Sucesso no acesso!")
                                }
                            }
                        )
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Memo Flow")
                            .setSubtitle("Autentique-se")
                            .setNegativeButtonText("PIN")
                            .build()
                        biometricPrompt.authenticate(promptInfo)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                            OnboardingScreen(
                                onFinished = {
                                    authViewModel.updateHasSeenWelcome(true)
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
                            HomeScreen(navController = navController, activity = this@MainActivity)
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
                            StatisticsScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToStore = { navController.navigate(Screen.Store.route) }
                            )
                        }

                        composable(
                            route = Screen.Gratitude.route,
                            deepLinks = listOf(navDeepLink { uriPattern = "memoflow://gratitude" })
                        ) {
                            GratitudeScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToStore = { navController.navigate(Screen.Store.route) }
                            )
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

                    if (showSupportDevDialog) {
                        SupportDevDialog(
                            onDismiss = {
                                showSupportDevDialog = false
                            },
                            onConfirm = {
                                showSupportDevDialog = false
                                navController.navigate(Screen.Store.route)
                            },
                            particles = particles
                        )
                    }
                }
            }
        }
    }
}
