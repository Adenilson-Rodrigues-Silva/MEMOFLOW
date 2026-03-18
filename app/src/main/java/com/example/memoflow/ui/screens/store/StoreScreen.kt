package com.example.memoflow.ui.screens.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoflow.ui.components.home.PurpleAI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "store_effects")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradient"
    )

    val premiumBrush = Brush.linearGradient(
        colors = listOf(PurpleAI, Color(0xFF6200EE), PurpleAI),
        start = androidx.compose.ui.geometry.Offset(gradientOffset, gradientOffset),
        end = androidx.compose.ui.geometry.Offset(gradientOffset + 500f, gradientOffset + 500f),
        tileMode = androidx.compose.ui.graphics.TileMode.Repeated
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolução do Sistema", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Icon(
                Icons.Default.Star, 
                contentDescription = null, 
                tint = PurpleAI, 
                modifier = Modifier.size(80.dp)
            )
            
            Text(
                "MemoFlow Premium",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Desbloqueie o potencial máximo do seu diário",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Plano Grátis
            PlanCard(
                title = "Versão Grátis",
                price = "R$ 0,00",
                benefits = listOf(
                    "3 notas por dia normais",
                    "3 cápsulas do tempo ao total",
                    "Relembrar 3x ao dia",
                    "Backup manual JSON"
                ),
                isPremium = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Plano Premium
            PlanCard(
                title = "Compra Única",
                price = "R$ 9,90",
                benefits = listOf(
                    "3 notas por dia",
                    "Cápsulas do tempo ilimitadas",
                    "Relembrar 6x dia",
                    "Backup automático Google Drive",
                    "Selo Premium no Perfil",
                    "Sem anúncios (futuro)"
                ),
                isPremium = true,
                brush = premiumBrush
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { /* Implementar compra */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(premiumBrush, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("EVOLUIR AGORA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    benefits: List<String>,
    isPremium: Boolean,
    brush: Brush? = null
) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(24.dp),
        border = if (isPremium && brush != null) BorderStroke(2.dp, brush) else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = if (isPremium) PurpleAI else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(price, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            
            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = if (isPremium) PurpleAI else Color.Gray, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(benefit, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }
    }
}
