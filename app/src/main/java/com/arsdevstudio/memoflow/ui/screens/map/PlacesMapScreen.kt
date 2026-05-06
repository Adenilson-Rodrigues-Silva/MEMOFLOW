package com.arsdevstudio.memoflow.ui.screens.map

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.ui.viewmodel.MapFilterPeriod
import com.arsdevstudio.memoflow.ui.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesMapScreen(
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val deepPurple = Color(0xFF6A00FF)
    val iceBlue = Color(0xFF80DEEA)
    
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedMonthForGallery by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.selectedNotes) {
        if (uiState.selectedNotes.isNotEmpty()) {
            showBottomSheet = true
        }
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_cobalt),
            isMyLocationEnabled = false
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        val initialLat = uiState.initialLocation?.first ?: 38.7223
        val initialLng = uiState.initialLocation?.second ?: -9.1393
        val initialZoom = uiState.initialLocation?.third ?: 10f
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), initialZoom)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val pos = cameraPositionState.position
            viewModel.saveLastLocation(pos.target.latitude, pos.target.longitude, pos.zoom)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rastros", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                onMapLongClick = { viewModel.onMapLongClick(it) }
            ) {
                if (uiState.notesWithLocation.isNotEmpty()) {
                    val latLngs = uiState.notesWithLocation.mapNotNull { 
                        if (it.latitude != null && it.longitude != null) LatLng(it.latitude, it.longitude) else null 
                    }
                    
                    if (latLngs.isNotEmpty()) {
                        val provider = remember(latLngs) {
                            HeatmapTileProvider.Builder()
                                .data(latLngs)
                                .gradient(Gradient(
                                    intArrayOf(
                                        android.graphics.Color.argb(180, 187, 134, 252),
                                        android.graphics.Color.argb(255, 106, 0, 255)
                                    ),
                                    floatArrayOf(0.2f, 1.0f)
                                ))
                                .radius(50)
                                .build()
                        }
                        TileOverlay(tileProvider = provider)
                    }
                }
            }

            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapFilterPeriod.entries.forEach { period ->
                        val isSelected = uiState.currentFilter == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(period) },
                            label = { Text(period.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = Color.Gray,
                                selectedContainerColor = deepPurple.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Gray.copy(alpha = 0.3f),
                                selectedBorderColor = deepPurple
                            )
                        )
                    }
                }
            }
        }

        if (uiState.isFiltering) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(
                    color = Color(0xFF121212),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.width(280.dp).wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_backup))
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Reorganizando seus rastros...",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Buscando memórias de ${uiState.currentFilter.label}",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false
                    viewModel.clearSelectedNotes()
                    selectedMonthForGallery = null
                },
                sheetState = sheetState,
                containerColor = Color(0xFF121212),
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding()) {
                    Text(
                        text = "Resumo dos Rastros",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val groupedNotes = uiState.selectedNotes.groupBy { note ->
                        SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR")).format(Date(note.date))
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(top = 16.dp)
                    ) {
                        groupedNotes.forEach { (month, notes) ->
                            item {
                                val locationName = notes.firstOrNull { it.locationName != null }?.locationName
                                MonthSummaryCard(
                                    month = month.replaceFirstChar { it.uppercase() },
                                    location = locationName,
                                    totalNotes = notes.size,
                                    lockedNotes = notes.count { it.isLocked },
                                    capsuleNotes = notes.count { it.isTimeCapsule },
                                    accentColor = deepPurple,
                                    capsuleColor = iceBlue,
                                    onGalleryClick = { selectedMonthForGallery = if (selectedMonthForGallery == month) null else month }
                                )
                                
                                AnimatedVisibility(visible = selectedMonthForGallery == month) {
                                    val photos = notes.filter { !it.isLocked && !it.isTimeCapsule }
                                        .flatMap { it.images }
                                    
                                    if (photos.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(top = 12.dp)) {
                                            Text("Fotos desta região", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(photos) { photoUri ->
                                                    AsyncImage(
                                                        model = photoUri,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text("Nenhuma foto pública aqui.", color = Color.DarkGray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun MonthSummaryCard(
    month: String,
    location: String?,
    totalNotes: Int,
    lockedNotes: Int,
    capsuleNotes: Int,
    accentColor: Color,
    capsuleColor: Color,
    onGalleryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = month,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (location != null) {
                    Text(text = "em $location", color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onGalleryClick) {
                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White.copy(alpha = 0.6f))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow(Icons.Default.Description, "$totalNotes notas feitas aqui", Color.Gray)
                
                if (capsuleNotes > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow(Icons.Default.AcUnit, "$capsuleNotes notas congeladas", capsuleColor)
                }
                
                if (lockedNotes > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow(Icons.Default.Lock, "$lockedNotes notas trancadas", accentColor)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

