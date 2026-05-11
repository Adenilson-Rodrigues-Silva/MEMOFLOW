package com.arsdevstudio.memoflow.ui.screens.gratitude

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.ui.viewmodel.GratitudeViewModel
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
val SolarGold = Color(0xFFFFD700)
val SolarOrange = Color(0xFFFF8C00)
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

@Composable
fun rememberSolarAiGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "solar_gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "offset"
    )
    
    return Brush.linearGradient(
        colors = listOf(SolarGold, SolarOrange, SolarGold),
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Repeated
    )
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GratitudeScreen(
    onBack: () -> Unit,
    onNavigateToStore: () -> Unit,
    viewModel: GratitudeViewModel = viewModel(factory = GratitudeViewModel.Factory)
) {
    val context = LocalContext.current
    val todaysGratitudes by viewModel.todaysGratitudes.collectAsState()
    val allGratitudes by viewModel.allGratitudes.collectAsState()
    val dailyCount by viewModel.dailyCount.collectAsState()
    val flashbackCount by viewModel.flashbackCount.collectAsState()
    
    var gratitudeText by remember { mutableStateOf("") }
    var gratitudeToEdit by remember { mutableStateOf<GratitudeEntity?>(null) }
    var showFlashback by remember { mutableStateOf<GratitudeEntity?>(null) }
    
    val solarGradient = rememberSolarAiGradient()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(context.getString(R.string.gratitude_title), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
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
                if (todaysGratitudes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            context.getString(R.string.gratitude_empty_mural),
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
                        items(todaysGratitudes, key = { it.id }) { gratitude ->
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
                            placeholder = { Text(context.getString(R.string.gratitude_input_placeholder), color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, solarGradient, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF151515),
                                unfocusedContainerColor = Color(0xFF151515),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = SolarGold,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
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
                                    Icon(Icons.Default.Add, null, tint = SolarGold, modifier = Modifier.size(28.dp))
                                }
                            }
                        )
                        Text(
                            context.getString(R.string.gratitude_daily_count, dailyCount),
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
                                .background(SolarGold.copy(alpha = 0.1f))
                                .border(1.5.dp, solarGradient, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                context.getString(R.string.gratitude_limit_reached),
                                color = SolarGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pote da Gratidão
                    Box(modifier = Modifier.clickable {
                        if (allGratitudes.isNotEmpty()) {
                            if (flashbackCount < 3) {
                                val now = System.currentTimeMillis()
                                val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
                                val oldGratitudes = allGratitudes.filter { (now - it.date) > oneMonthMillis }
                                
                                showFlashback = if (oldGratitudes.isNotEmpty()) {
                                    oldGratitudes.random()
                                } else {
                                    allGratitudes.random()
                                }
                                viewModel.incrementFlashbackCount()
                            } else {
                                onNavigateToStore()
                            }
                        }
                    }) {
                        GratitudeJar(hasGratitude = allGratitudes.isNotEmpty())
                    }
                }
            }
        }

        // Dialog de Flashback (Removido o efeito de borda animada por estar desalinhado)
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
                title = { Text(context.getString(R.string.gratitude_edit_title), color = Color.White) },
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
                                cursorColor = SolarGold,
                                focusedBorderColor = SolarGold
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateGratitude(currentGratitude.copy(text = editText))
                        gratitudeToEdit = null
                    }) {
                        Text(context.getString(R.string.gratitude_save), color = SolarGold)
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
                            Text(context.getString(R.string.gratitude_delete), color = Color.Red)
                        }
                        TextButton(onClick = { gratitudeToEdit = null }) {
                            Text(context.getString(R.string.gratitude_cancel), color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FlashbackDialog(gratitude: GratitudeEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
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
        val locale = context.resources.configuration.locales[0]
        val formatter = DateTimeFormatter.ofPattern(context.getString(R.string.gratitude_date_format), locale)
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
                    context.getString(R.string.gratitude_flashback_title, dateStr),
                    color = SolarGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                // Card simples para evitar desalinhamento
                PostItNote(gratitude = gratitude, onLongClick = {})
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    context.getString(R.string.gratitude_flashback_footer),
                    color = Color.White.copy(alpha = 0.5f),
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerSize = size.width * 0.15f
            val path = Path().apply {
                moveTo(size.width - cornerSize, size.height)
                lineTo(size.width - cornerSize, size.height - cornerSize)
                lineTo(size.width, size.height - cornerSize)
                close()
            }
            drawPath(path, Color.Black.copy(alpha = 0.2f))
            drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(2f))
        }
        
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
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "jar_effects")
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val fireflyBlink by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "firefly"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    translationY = floatAnim
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SolarGold.copy(alpha = 0.2f * pulse), Color.Transparent)
                        )
                    )
            )

            Image(
                painter = painterResource(id = R.drawable.jar_gratitude),
                contentDescription = context.getString(R.string.gratitude_jar_title),
                modifier = Modifier.fillMaxSize()
            )

            if (hasGratitude) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    val fireflyPositions = listOf(
                        Offset(w * 0.45f, h * 0.55f),
                        Offset(w * 0.55f, h * 0.65f),
                        Offset(w * 0.35f, h * 0.70f),
                        Offset(w * 0.65f, h * 0.50f),
                        Offset(w * 0.50f, h * 0.45f),
                        Offset(w * 0.40f, h * 0.60f),
                        Offset(w * 0.60f, h * 0.75f),
                        Offset(w * 0.30f, h * 0.50f)
                    )

                    fireflyPositions.forEachIndexed { i, pos ->
                        val individualBlink = if (i % 2 == 0) fireflyBlink else (1.2f - fireflyBlink)
                        
                        drawCircle(
                            color = SolarGold.copy(alpha = 0.6f * individualBlink),
                            radius = 4.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = individualBlink),
                            radius = 1.5.dp.toPx(),
                            center = pos
                        )
                    }
                }
            }
        }
        
        Text(
            context.getString(R.string.gratitude_jar_title),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            context.getString(R.string.gratitude_jar_subtitle),
            color = SolarGold.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun Color.toHexString(): String {
    return String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}

