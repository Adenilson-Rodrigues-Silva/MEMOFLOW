package com.example.memoflow.ui.screens.store

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoflow.utils.BillingManager
import com.example.memoflow.ui.components.home.PurpleAI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    onBack: () -> Unit,
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory(LocalContext.current.applicationContext as android.app.Application))
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPremium by viewModel.isPremium.collectAsState()
    val products by viewModel.products.collectAsState()
    val scope = rememberCoroutineScope()
    
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
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

    val starScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "star_scale"
    )

    // Escuta eventos de compra
    LaunchedEffect(Unit) {
        viewModel.purchaseEvents.collect { event ->
            when (event) {
                is BillingManager.PurchaseEvent.Success -> {
                    if (event.productId == "premium_lifetime") {
                        Toast.makeText(context, "Parabéns! Você agora é PREMIUM ✨", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Obrigado pelo seu apoio! ❤️", Toast.LENGTH_SHORT).show()
                    }
                }
                is BillingManager.PurchaseEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is BillingManager.PurchaseEvent.Cancelled -> {
                    // Opcional: Toast.makeText(context, "Compra cancelada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolução do Sistema", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restorePurchases() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restaurar", tint = Color.White)
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
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                scope.launch {
                                    val app = context.applicationContext as com.example.memoflow.MemoApplication
                                    app.billingPrefs.setPremium(!isPremium)
                                    Toast.makeText(context, if (!isPremium) "MODO PREMIUM ATIVADO (TESTE)" else "MODO FREE ATIVADO (TESTE)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
            ) {
                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFFFFD700).copy(alpha = 0.4f), CircleShape)
                            .blur(25.dp)
                    )
                }
                
                Icon(
                    Icons.Default.Star, 
                    contentDescription = null, 
                    tint = if (isPremium) Color(0xFFFFD700) else PurpleAI, 
                    modifier = Modifier.size(if (isPremium) 80.dp * starScale else 80.dp)
                )
            }
            
            Text(
                text = if (isPremium) "MemoFlow Premium Ativo" else "MemoFlow Premium",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPremium) Color(0xFFFFD700) else Color.White
            )
            
            Text(
                if (isPremium) "Obrigado por apoiar o desenvolvimento!" else "Desbloqueie o potencial máximo do seu diário",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PlanCard(
                title = "Versão Grátis",
                price = "R$ 0,00",
                benefits = listOf(
                    "3 notas por dia",
                    "3 cápsulas do tempo ao total",
                    "Relembrar 2x ao dia",
                    "Backup manual JSON",
                    "IA: Apenas Insight Diário"
                ),
                isPremium = false,
                isSelected = !isPremium
            )

            Spacer(modifier = Modifier.height(24.dp))

            val premiumProduct = products.find { it.productId == "premium_lifetime" }
            val premiumPrice = premiumProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "R$ 49,90"

            PlanCard(
                title = "Compra Única",
                price = premiumPrice,
                benefits = listOf(
                    "3 notas por dia",
                    "Cápsulas do tempo ilimitadas",
                    "Relembrar 6x dia",
                    "Backup automático Google Drive",
                    "Selo Premium no Perfil",
                    "IA: 12 usos/dia por período (D/S/M)"
                ),
                isPremium = true,
                brush = premiumBrush,
                isSelected = isPremium
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            if (!isPremium) {
                Button(
                    onClick = { activity?.let { viewModel.buyPremium(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(premiumBrush, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("EVOLUIR AGORA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Surface(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("VOCÊ JÁ É PREMIUM ✨", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

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

                    val coffeePrice = products.find { it.productId == "donation_coffee" }?.oneTimePurchaseOfferDetails?.formattedPrice ?: "R$ 3,90"
                    val snackPrice = products.find { it.productId == "donation_snack" }?.oneTimePurchaseOfferDetails?.formattedPrice ?: "R$ 5,90"
                    val mealPrice = products.find { it.productId == "donation_meal" }?.oneTimePurchaseOfferDetails?.formattedPrice ?: "R$ 8,90"

                    DonationOption(
                        icon = Icons.Default.Coffee,
                        label = "Um cafézinho",
                        price = coffeePrice,
                        onClick = { 
                            activity?.let { viewModel.donate(it, "coffee") }
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showDonationSheet = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DonationOption(
                        icon = Icons.Default.Fastfood,
                        label = "Pão com ovo",
                        price = snackPrice,
                        onClick = { 
                            activity?.let { viewModel.donate(it, "snack") }
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showDonationSheet = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DonationOption(
                        icon = Icons.Default.Restaurant,
                        label = "Arroz e feijão",
                        price = mealPrice,
                        onClick = { 
                            activity?.let { viewModel.donate(it, "meal") }
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
    brush: Brush? = null,
    isSelected: Boolean = false
) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected && isPremium && brush != null) {
            BorderStroke(2.dp, brush)
        } else if (isSelected) {
            BorderStroke(2.dp, PurpleAI)
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        },
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
