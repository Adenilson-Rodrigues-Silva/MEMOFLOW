package com.arsdevstudio.memoflow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
import com.arsdevstudio.memoflow.ui.theme.MemoFlowTheme
import com.arsdevstudio.memoflow.ui.viewmodel.AuthEvent
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

data class SupportParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val alpha: Float,
    val scale: Float,
    val symbol: String
)

class MainActivity : ComponentActivity() {
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

                // Lógica de partículas (Efeito diamante/Telegram)
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
                                val newAlpha = p.alpha - 0.008f // Fica visível por mais tempo (mais sólido)
                                if (newAlpha <= 0) {
                                    iterator.remove()
                                } else {
                                    iterator.set(p.copy(
                                        x = p.x + p.vx,
                                        y = p.y + p.vy,
                                        vy = p.vy - 0.15f, // Gravidade invertida (flutuar)
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

                // Efeito de diamante (Brilho animado mais intenso e luxuoso)
                val infiniteTransition = rememberInfiniteTransition(label = "diamond_shine")
                val shimmerTranslate by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 2000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmer"
                )

                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00FFC2).copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.9f),
                        Color(0xFFB2FFFF).copy(alpha = 0.8f), // Azul diamante claro
                        Color(0xFF00E5FF).copy(alpha = 0.6f), // Ciano brilhante
                        Color.White.copy(alpha = 0.9f),
                        Color(0xFF00FFC2).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - 600f, shimmerTranslate - 600f),
                    end = Offset(shimmerTranslate, shimmerTranslate)
                )

                LaunchedEffect(userSettings?.appEntryCount) {
                    val count = userSettings?.appEntryCount ?: 0
                    if (count >= 1) {
                        Log.i("MemoFlow_Debug", "MainActivity - [DIÁLOGO] Contador=$count detectado. Iniciando timer de 4s...")
                        delay(4000)
                        showSupportDevDialog = true
                        Log.i("MemoFlow_Debug", "MainActivity - [DIÁLOGO] showSupportDevDialog setado para TRUE")
                    } else {
                        Log.d("MemoFlow_Debug", "MainActivity - [CHECK] Contador=$count. Critério não atingido.")
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
                        // Camada de partículas (Diamantes/Sifrões flutuantes)
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            particles.forEach { p ->
                                Text(
                                    text = p.symbol,
                                    modifier = Modifier
                                        .offset(x = p.x.dp, y = p.y.dp)
                                        .graphicsLayer(
                                            alpha = p.alpha,
                                            scaleX = p.scale,
                                            scaleY = p.scale
                                        ),
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Log.i("MemoFlow_Debug", "MainActivity - [UI] Renderizando AlertDialog agora!")
                        AlertDialog(
                            onDismissRequest = {
                                showSupportDevDialog = false
                                profileViewModel.resetAppEntryCount()
                            },
                            modifier = Modifier.border(4.dp, shimmerBrush, RoundedCornerShape(24.dp)),
                            title = {
                                Text(
                                    stringResource(R.string.home_support_dev_title),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.home_support_dev_desc),
                                    color = Color.Gray
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showSupportDevDialog = false
                                        profileViewModel.resetAppEntryCount()
                                        navController.navigate(Screen.Store.route)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC2))
                                ) {
                                    Text(
                                        stringResource(R.string.home_support_dev_confirm),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showSupportDevDialog = false
                                    profileViewModel.resetAppEntryCount()
                                }) {
                                    Text(
                                        stringResource(R.string.home_support_dev_later),
                                        color = Color.White
                                    )
                                }
                            },
                            containerColor = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }
        }
    }
}
