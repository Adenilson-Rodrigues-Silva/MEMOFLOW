package com.example.memoflow.ui.screens.gratitude

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoflow.R
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.ui.viewmodel.GratitudeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

// Colors moved out for global access within the file
val NeonGreen = Color(0xFF00FFC2)
val NeonPink = Color(0xFFFF00E5)
val NeonBlue = Color(0xFF00E0FF)
val NeonYellow = Color(0xFFFFFF00)
val PostItColors = listOf(NeonPink, NeonBlue, NeonYellow, NeonGreen, Color(0xFFFFA500))

val PostItShape = GenericShape { size, _ ->
    val cornerSize = size.width * 0.15f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - cornerSize)
    lineTo(size.width - cornerSize, size.height)
    lineTo(0f, size.height)
    close()
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GratitudeScreen(
    onBack: () -> Unit,
    viewModel: GratitudeViewModel = viewModel(factory = GratitudeViewModel.Factory)
) {
    val gratitudes by viewModel.gratitudes.collectAsState()
    val dailyCount by viewModel.dailyCount.collectAsState()
    var gratitudeText by remember { mutableStateOf("") }
    var gratitudeToEdit by remember { mutableStateOf<GratitudeEntity?>(null) }
    var showFlashback by remember { mutableStateOf<GratitudeEntity?>(null) }
    
    val isNewYear = remember { 
        val now = LocalDate.now()
        now.monthValue == 1 && now.dayOfMonth == 1
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mural da Gratidão", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF050505), Color(0xFF0D0D2B))
                    )
                )
        ) {
            // Mural 2D Cartoon
            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                if (gratitudes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "O mural está vazio.\nEspalhe brilho hoje! ✨",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gratitudes, key = { it.id }) { gratitude ->
                            PostItNote(
                                gratitude = gratitude,
                                onLongClick = { gratitudeToEdit = gratitude }
                            )
                        }
                    }
                }
            }

            // Input e Pote
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (dailyCount < 5 || gratitudeToEdit != null) {
                        OutlinedTextField(
                            value = gratitudeText,
                            onValueChange = { gratitudeText = it },
                            placeholder = { Text("Pelo que você é grato agora?", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true, // Fix for accent issues in newer Compose
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF151515),
                                unfocusedContainerColor = Color(0xFF151515),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = NeonGreen,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (gratitudeText.isNotBlank()) {
                                            viewModel.addGratitude(
                                                gratitudeText,
                                                PostItColors.random().toHexString()
                                            )
                                            gratitudeText = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, null, tint = NeonGreen, modifier = Modifier.size(28.dp))
                                }
                            }
                        )
                        Text(
                            "${dailyCount}/5 hoje • brilhe mais",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(NeonGreen.copy(alpha = 0.1f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Luz total por hoje! Volte amanhã ✨",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pote da Gratidão com Imagem e Animações
                    Box(modifier = Modifier.clickable {
                        if (gratitudes.isNotEmpty()) {
                            val now = System.currentTimeMillis()
                            val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
                            val oldGratitudes = gratitudes.filter { (now - it.date) > oneMonthMillis }
                            
                            showFlashback = if (oldGratitudes.isNotEmpty()) {
                                oldGratitudes.random()
                            } else {
                                gratitudes.random()
                            }
                        }
                    }) {
                        GratitudeJar(hasGratitude = gratitudes.isNotEmpty())
                    }
                }
            }
        }

        // Dialog de Flashback (Reviver momento de luz)
        if (showFlashback != null) {
            FlashbackDialog(
                gratitude = showFlashback!!,
                onDismiss = { showFlashback = null }
            )
        }

        // Dialog de Edição/Exclusão
        if (gratitudeToEdit != null) {
            val currentGratitude = gratitudeToEdit!!
            var editText by remember { mutableStateOf(currentGratitude.text) }
            AlertDialog(
                onDismissRequest = { gratitudeToEdit = null },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Editar Gratidão", color = Color.White) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = NeonGreen,
                                focusedBorderColor = NeonGreen
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateGratitude(currentGratitude.copy(text = editText))
                        gratitudeToEdit = null
                    }) {
                        Text("SALVAR", color = NeonGreen)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            viewModel.deleteGratitude(currentGratitude)
                            gratitudeToEdit = null
                        }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                            Spacer(Modifier.width(4.dp))
                            Text("EXCLUIR", color = Color.Red)
                        }
                        TextButton(onClick = { gratitudeToEdit = null }) {
                            Text("CANCELAR", color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FlashbackDialog(gratitude: GratitudeEntity, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "flashback_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val dateStr = remember(gratitude.date) {
        val date = Instant.ofEpochMilli(gratitude.date).atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        date.format(formatter)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                Text(
                    "Em $dateStr você foi grato por:",
                    color = NeonYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                PostItNote(gratitude = gratitude, onLongClick = {})
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    "Toque em qualquer lugar para fechar",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PostItNote(gratitude: GratitudeEntity, onLongClick: () -> Unit) {
    val color = remember(gratitude.colorHex) { Color(android.graphics.Color.parseColor(gratitude.colorHex)) }
    val rotation = remember { (Random.nextFloat() * 8 - 4) }
    
    Box(
        modifier = Modifier
            .padding(4.dp)
            .rotate(rotation)
            .aspectRatio(1f)
            .shadow(12.dp, shape = PostItShape)
            .clip(PostItShape)
            .background(color)
            .border(3.dp, Color.Black.copy(alpha = 0.7f), PostItShape)
            .combinedClickable(
                onClick = { /* Pode abrir um detalhe ou apenas brilhar */ },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Textura de papel / brilho
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerSize = size.width * 0.15f
            // Dobra do papel no canto
            val path = Path().apply {
                moveTo(size.width - cornerSize, size.height)
                lineTo(size.width - cornerSize, size.height - cornerSize)
                lineTo(size.width, size.height - cornerSize)
                close()
            }
            drawPath(path, Color.Black.copy(alpha = 0.2f))
            drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(2f))
        }
        
        // Tachinha / Pin no topo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .size(12.dp)
                .shadow(4.dp, CircleShape)
                .background(Color.Red, CircleShape)
                .border(1.dp, Color.Black, CircleShape)
        )

        Text(
            text = gratitude.text,
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun GratitudeJar(hasGratitude: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "jar_effects")
    
    // Levitação
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Brilho da Aura
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(200.dp) // Aumentado para valorizar a imagem
                .graphicsLayer {
                    translationY = floatAnim
                },
            contentAlignment = Alignment.Center
        ) {
            // Aura de luz atrás da imagem
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonYellow.copy(alpha = 0.3f * pulse), Color.Transparent)
                        )
                    )
            )

            // A Imagem do Pote
            Image(
                painter = painterResource(id = R.drawable.jar_gratitude),
                contentDescription = "Pote da Gratidão",
                modifier = Modifier.fillMaxSize()
            )

            // Canvas sobreposto para luzes animadas e faíscas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (hasGratitude) {
                    // Vagalumes dinâmicos (dentro e saindo do pote)
                    val random = Random(42)
                    repeat(20) { i ->
                        val time = System.currentTimeMillis()
                        val offset = i * 200
                        val blink = (kotlin.math.sin((time + offset) / 400.0).toFloat() + 1f) / 2f
                        
                        // Movimento circular aleatório
                        val radiusX = w * 0.25f
                        val radiusY = h * 0.25f
                        val centerX = w * 0.5f
                        val centerY = h * 0.6f
                        
                        val angle = (time + offset) / 1000.0
                        val x = centerX + kotlin.math.cos(angle + i).toFloat() * (random.nextFloat() * radiusX)
                        val y = centerY + kotlin.math.sin(angle * 0.8 + i).toFloat() * (random.nextFloat() * radiusY)
                        
                        val color = listOf(NeonYellow, Color.Cyan, Color.White).random(random)
                        
                        // Glow difuso
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.6f * blink), Color.Transparent),
                                center = Offset(x, y),
                                radius = 12.dp.toPx()
                            ),
                            radius = 12.dp.toPx(),
                            center = Offset(x, y)
                        )
                        
                        // Núcleo de luz
                        drawCircle(
                            color = Color.White.copy(alpha = blink),
                            radius = 1.5.dp.toPx(),
                            center = Offset(x, y)
                        )

                        // Faíscas que saem pela boca do pote (ocasionalmente)
                        if (i % 4 == 0) {
                            val sparkY = centerY - (h * 0.2f) - ((time + offset) % 2000 / 2000f) * (h * 0.4f)
                            val sparkX = centerX + kotlin.math.sin((time + offset) / 300.0).toFloat() * (w * 0.1f)
                            val sparkAlpha = (1f - (centerY - (h * 0.2f) - sparkY) / (h * 0.4f)).coerceIn(0f, 1f)
                            
                            drawCircle(
                                color = NeonYellow.copy(alpha = 0.8f * sparkAlpha),
                                radius = 1.dp.toPx(),
                                center = Offset(sparkX, sparkY)
                            )
                        }
                    }
                }
            }
        }
        
        Text(
            "Pote da Gratidão",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            "Toque para reviver seus momentos de luz",
            color = NeonGreen.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun Color.toHexString(): String {
    return String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}
