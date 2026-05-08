package com.arsdevstudio.memoflow.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel

@Composable
fun WelcomeAuthScreen(
    viewModel: AuthViewModel // Recebe o ViewModel da MainActivity
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color.Black)
    )

    val googleGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4285F4), 
            Color(0xFFEA4335), 
            Color(0xFFFBBC05), 
            Color(0xFF34A853)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_login))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MemoFlow",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Eternize seu fluxo, siga seus rastros.",
                color = Color(0xFF00FFC2),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Suas memórias organizadas geograficamente e protegidas com a segurança do Google.",
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(56.dp))

            // BOTÃO GOOGLE
            Surface(
                onClick = { viewModel.signInWithGoogle(context) },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .border(2.5.dp, googleGradient, RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar com Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- DIALOG DE LOADING LÚDICO (Com a nova animação de Boas-Vindas) ---
        if (uiState.isLoading) {
            Dialog(onDismissRequest = { }, properties = DialogProperties(false, false)) {
                Surface(
                    color = Color(0xFF1A1A1A), 
                    shape = RoundedCornerShape(28.dp), 
                    modifier = Modifier.size(220.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, 
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val loadingComp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_boasvindas))
                        LottieAnimation(
                            composition = loadingComp,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Iniciando sua jornada...",
                            color = Color.White, 
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}

