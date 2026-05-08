package com.arsdevstudio.memoflow.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onFinished: (Boolean) -> Unit, // ✅ Agora retorna se o usuário está logado
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val alphaAnim = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1200))
        
        // Verifica no banco se o usuário está logado com Google
        val userSettings = viewModel.getUserSettings().first()
        val isGoogleLogged = userSettings?.isGoogleLogged ?: false
        
        delay(1000)
        onFinished(isGoogleLogged)
    }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text(
            "MEMOFLOW",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.alpha(alphaAnim.value)
        )
    }
}

