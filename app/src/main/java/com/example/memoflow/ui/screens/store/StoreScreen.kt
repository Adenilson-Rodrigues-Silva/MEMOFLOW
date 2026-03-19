package com.example.memoflow.ui.screens.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoflow.ui.components.home.PurpleAI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDonationSheet by remember { mutableStateOf(false) }
    
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

            Spacer(modifier = Modifier.height(32.dp))
            
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
            
            Spacer(modifier = Modifier.height(16.dp))

            // Botão "Pague um café"
            OutlinedButton(
                onClick = { showDonationSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PurpleAI.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Coffee, contentDescription = null, tint = PurpleAI, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pague um café ao desenvolvedor", color = PurpleAI, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showDonationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDonationSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF121212),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Apoie o Projeto",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Sua contribuição ajuda a manter o MemoFlow vivo e evoluindo!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    DonationOption(
                        icon = Icons.Default.Coffee,
                        label = "Um cafézinho",
                        price = "R$ 3,90",
                        onClick = { 
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showDonationSheet = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DonationOption(
                        icon = Icons.Default.Fastfood,
                        label = "Pão com ovo",
                        price = "R$ 5,90",
                        onClick = { 
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showDonationSheet = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DonationOption(
                        icon = Icons.Default.Restaurant,
                        label = "Arroz e feijão",
                        price = "R$ 8,90",
                        onClick = { 
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showDonationSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DonationOption(
    icon: ImageVector,
    label: String,
    price: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PurpleAI.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpleAI, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Contribuição voluntária", color = Color.Gray, fontSize = 12.sp)
            }
            Text(price, color = PurpleAI, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
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
