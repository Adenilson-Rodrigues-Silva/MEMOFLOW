package com.arsdevstudio.memoflow.ui.screens.note

import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.arsdevstudio.memoflow.R
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.arsdevstudio.memoflow.ui.components.EmojiSelector
import com.arsdevstudio.memoflow.ui.components.FloatingColorMenu
import com.arsdevstudio.memoflow.ui.components.PhotoGrid
import com.arsdevstudio.memoflow.ui.components.TextFormattingPanel
import com.arsdevstudio.memoflow.ui.components.VoiceNoteSection
import com.arsdevstudio.memoflow.ui.components.handleVoiceClick
import com.arsdevstudio.memoflow.ui.components.home.rememberAnimatedAiGradient
import com.arsdevstudio.memoflow.ui.components.writenote.MemoCard
import com.arsdevstudio.memoflow.ui.components.writenote.NoteDetailsDialog
import com.arsdevstudio.memoflow.ui.components.writenote.NoteOptionsOverflowMenu
import com.arsdevstudio.memoflow.ui.components.writenote.ShareOptionDialog
import com.arsdevstudio.memoflow.ui.screens.security.SecurityViewModel
import com.arsdevstudio.memoflow.utils.ShareUtils
import com.arsdevstudio.memoflow.ui.viewmodel.WriteNoteViewModel
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(
    onBack: () -> Unit,
    noteId: Long? = null,
    readOnly: Boolean = false,
    viewModel: WriteNoteViewModel = viewModel(factory = WriteNoteViewModel.Factory),
    securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory)
) {
    val context = LocalContext.current
    val richTextState = viewModel.richTextState
    val uiState by viewModel.uiStateFlow.collectAsState()
    val securitySettings by securityViewModel.userSettings.collectAsState()

    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    val surfaceDark = Color(0xFF1E1E1E)
    
    val animatedAiGradient = rememberAnimatedAiGradient()
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val infiniteTransition = rememberInfiniteTransition(label = "snow_effects")
    val snowMove by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "snow"
    )

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(richTextState.annotatedString.text.length) {
        if (!readOnly && richTextState.selection.end >= richTextState.annotatedString.text.length - 1) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            viewModel.loadNote(noteId)
        } else if (!readOnly) {
            viewModel.checkNoteLimit()
        }
    }

    if (uiState.isLimitReached) {
        AlertDialog(
            onDismissRequest = { onBack() },
            containerColor = Color(0xFF1A1A1A),
            title = { Text(stringResource(R.string.write_note_limit_reached_title), color = Color.White) },
            text = { Text(stringResource(R.string.write_note_limit_reached_desc), color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = { onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
                ) {
                    Text(stringResource(R.string.write_note_back_button), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showLockConfirmDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var isGeneratingCard by remember { mutableStateOf(false) }
    var selectedImageFullScreen by remember { mutableStateOf<Uri?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.captureLocationAndSave(context) { onBack() }
        } else {
            viewModel.saveNote()
            onBack()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) viewModel.onVoiceClick(context.cacheDir) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onImageSelected(context, uri) }

    if (uiState.isSaving) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.size(260.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_dino_dance_save_note))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val savingPhrases = stringArrayResource(R.array.write_note_saving_phrases)
                    val randomPhrase = remember(uiState.isSaving) {
                        if (uiState.isSaving && savingPhrases.isNotEmpty()) savingPhrases.random() else ""
                    }

                    Text(
                        text = randomPhrase,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    if (selectedImageFullScreen != null) {
        Dialog(onDismissRequest = { selectedImageFullScreen = null }) {
            Box(
                modifier = Modifier.fillMaxSize().clickable { selectedImageFullScreen = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageFullScreen,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
                IconButton(
                    onClick = { selectedImageFullScreen = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (readOnly) stringResource(R.string.write_note_peek_mode) else stringResource(R.string.write_note_entry), color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                },
                actions = {
                    if (readOnly) {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.write_note_exit), color = iceBlue, fontWeight = FontWeight.ExtraBold) }
                    } else {
                        IconButton(onClick = { showOverflowMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                        NoteOptionsOverflowMenu(
                            expanded = showOverflowMenu,
                            isLocked = uiState.isLocked,
                            onDismissRequest = { showOverflowMenu = false },
                            onShareClick = { showShareDialog = true; showOverflowMenu = false },
                            onFontStyleClick = { 
                                Toast.makeText(context, context.getString(R.string.write_note_font_styles_soon), Toast.LENGTH_SHORT).show()
                                showOverflowMenu = false 
                            },
                            onDetailsClick = { showDetailsDialog = true; showOverflowMenu = false },
                            onLockClick = {
                                if (securitySettings.pin.isNullOrEmpty()) Toast.makeText(context, context.getString(R.string.write_note_set_pin_first), Toast.LENGTH_LONG).show()
                                else showLockConfirmDialog = true
                                showOverflowMenu = false
                            },
                            onTimeCapsuleClick = { showDatePicker = true; showOverflowMenu = false },
                            neonGreen = neonGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            if (!readOnly && !uiState.isLimitReached) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()) {
                    AnimatedVisibility(visible = showFormatMenu) { TextFormattingPanel(state = richTextState, isVisible = showFormatMenu) }
                    AnimatedVisibility(visible = showMarkerMenu) {
                        FloatingColorMenu(
                            selectedTextColor = richTextState.currentSpanStyle.color,
                            selectedMarkerColor = (richTextState.currentSpanStyle.background as? Color) ?: Color.Transparent,
                            onTextColorSelected = { color -> viewModel.updateTextColor(color) },
                            onMarkerColorSelected = { color -> viewModel.applyMarker(color) },
                            onDismiss = { showMarkerMenu = false },
                            neonGreen = neonGreen
                        )
                    }
                    AnimatedVisibility(
                        visible = showEmojiMenu,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 15.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E1E1E),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp,
                            modifier = Modifier.padding(16.dp).border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        ) {
                            EmojiSelector(
                                emojis = uiState.diaryEmojis,
                                selectedEmoji = uiState.selectedEmoji,
                                onEmojiSelected = { emoji, humor -> viewModel.updateEmoji(emoji, humor); showEmojiMenu = false }
                            )
                        }
                    }
                    NoteBottomToolbar(
                        imageCount = uiState.images.size,
                        accentColor = if (uiState.isTimeCapsule) iceBlue else neonGreen,
                        onFormatClick = { showFormatMenu = !showFormatMenu; showEmojiMenu = false; showMarkerMenu = false },
                        onEmojiClick = { showEmojiMenu = !showEmojiMenu; showFormatMenu = false; showMarkerMenu = false },
                        onMarkerClick = { showMarkerMenu = !showMarkerMenu; showFormatMenu = false; showEmojiMenu = false },
                        onAddImageClick = { launcher.launch("image/*") },
                        onVoiceClick = { handleVoiceClick(context, uiState.isRecording, uiState.audioPath, viewModel, permissionLauncher) },
                        onSaveClick = {
                            if (uiState.latitude == null) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                viewModel.captureLocationAndSave(context) { onBack() }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp)
        ) {
            if (uiState.isTimeCapsule) {
                val unlockDateStr = uiState.unlockDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: ""
                Text(
                    text = stringResource(R.string.write_note_frozen_until, unlockDateStr),
                    color = iceBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        width = if (uiState.isLocked || uiState.isTimeCapsule) 1.dp else 1.5.dp,
                        brush = if (uiState.isLocked || uiState.isTimeCapsule) SolidColor(Color.Transparent) else animatedAiGradient,
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = surfaceDark.copy(alpha = if (readOnly) 0.4f else 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = if (readOnly && !uiState.isTimeCapsule) BorderStroke(1.dp, iceBlue.copy(alpha = 0.3f)) else null
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = uiState.title,
                                onValueChange = { if(!readOnly && !uiState.isLimitReached) viewModel.updateTitle(it) },
                                readOnly = readOnly || uiState.isLimitReached,
                                enabled = !readOnly && !uiState.isLimitReached,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default
                                ),
                                cursorBrush = SolidColor(if (uiState.isTimeCapsule) iceBlue else neonGreen)
                            )
                            Text(
                                text = stringResource(R.string.write_note_mood_label, uiState.selectedHumor),
                                color = if (uiState.isTimeCapsule) iceBlue else neonGreen,
                                fontSize = 14.sp
                            )
                        }
                        Text(text = uiState.selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    RichTextEditor(
                        state = richTextState,
                        readOnly = readOnly || uiState.isLimitReached,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().heightIn(min = 100.dp).bringIntoViewRequester(bringIntoViewRequester).padding(bottom = 80.dp),
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Color.White.copy(alpha = 0.8f)
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Default
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PhotoGrid(
                        images = uiState.images,
                        onRemove = { if(!readOnly) viewModel.removeImage(it) },
                        onExpand = { selectedImageFullScreen = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    VoiceNoteSection(
                        isRecording = uiState.isRecording,
                        recordingTime = uiState.recordingTime,
                        audioPath = uiState.audioPath,
                        viewModel = viewModel,
                        context = context,
                        permissionLauncher = permissionLauncher,
                        accentColor = if (uiState.isTimeCapsule) iceBlue else neonGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (readOnly) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val snowflakeCount = 45
                for (i in 0 until snowflakeCount) {
                    val x = ((i * 123f + (snowMove * 0.5f)) % size.width)
                    val y = ((i * 87f + snowMove) % size.height)
                    drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
        }
    }

    if (isGeneratingCard) {
        Dialog(onDismissRequest = { isGeneratingCard = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            val dialogView = LocalView.current
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.width(360.dp).wrapContentHeight()) {
                    MemoCard(
                        title = uiState.title,
                        content = richTextState.annotatedString,
                        emoji = uiState.selectedEmoji,
                        humor = uiState.selectedHumor,
                        date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(uiState.date)),
                        images = uiState.images.map { it.toString() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LaunchedEffect(Unit) {
                    delay(1500)
                    try {
                        val bitmap = Bitmap.createBitmap(dialogView.width, dialogView.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        dialogView.draw(canvas)
                        ShareUtils.shareBitmap(context, bitmap, context.getString(R.string.write_note_share_title))
                    } catch (e: Exception) { e.printStackTrace() }
                    isGeneratingCard = false
                }
            }
        }
    }

    if (showShareDialog) {
        ShareOptionDialog(
            onDismiss = { showShareDialog = false },
            onShareAsImage = { showShareDialog = false; isGeneratingCard = true },
            onShareAsText = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${uiState.title}\n\n${richTextState.annotatedString.text}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            },
            neonGreen = neonGreen
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null && selectedDate >= (System.currentTimeMillis() - 60000)) {
                        viewModel.setTimeCapsule(selectedDate)
                        viewModel.captureLocationAndSave(context) { onBack() }
                    } else {
                        Toast.makeText(context, context.getString(R.string.write_note_invalid_date), Toast.LENGTH_SHORT).show()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.write_note_freeze_action), color = iceBlue) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.write_note_cancel_action), color = Color.White) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showLockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLockConfirmDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text(if (uiState.isLocked) stringResource(R.string.write_note_unlock_title) else stringResource(R.string.write_note_lock_title), color = Color.White) },
            text = { Text(if (uiState.isLocked) stringResource(R.string.write_note_unlock_desc) else stringResource(R.string.write_note_lock_desc), color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleLock()
                    viewModel.captureLocationAndSave(context) { onBack() }
                    showLockConfirmDialog = false
                    Toast.makeText(context, if (!uiState.isLocked) context.getString(R.string.write_note_locked_toast) else context.getString(R.string.write_note_unlocked_toast), Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.write_note_confirm_action), color = neonGreen) }
            },
            dismissButton = { TextButton(onClick = { showLockConfirmDialog = false }) { Text(stringResource(R.string.write_note_cancel_action), color = Color.White) } }
        )
    }

    if (showDetailsDialog) {
        NoteDetailsDialog(
            wordCount = richTextState.annotatedString.text.trim().split("\\s+".toRegex()).size,
            charCount = richTextState.annotatedString.text.length,
            hasAudio = uiState.audioPath != null,
            imageCount = uiState.images.size,
            date = stringResource(R.string.write_note_today_label),
            onDismiss = { showDetailsDialog = false },
            neonGreen = neonGreen
        )
    }
}

@Composable
fun NoteBottomToolbar(
    imageCount: Int,
    accentColor: Color,
    onFormatClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onMarkerClick: () -> Unit,
    onAddImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val canAddMoreImages = imageCount < 3
    Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                ToolbarButton(Icons.Default.Mic, stringResource(R.string.write_note_toolbar_voice), onClick = onVoiceClick)
                ToolbarButton(Icons.Default.FormatBold, stringResource(R.string.write_note_toolbar_format), onClick = onFormatClick)
                ToolbarButton(Icons.Default.Face, stringResource(R.string.write_note_toolbar_emoji), onClick = onEmojiClick)
                IconButton(onClick = onAddImageClick, enabled = canAddMoreImages) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.write_note_toolbar_add_image),
                        tint = if (canAddMoreImages) Color.Gray else Color.DarkGray.copy(alpha = 0.5f)
                    )
                }
                ToolbarButton(icon = Icons.Default.Brush, desc = stringResource(R.string.write_note_toolbar_marker), onClick = onMarkerClick)
            }
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = accentColor,
                shape = CircleShape,
                modifier = Modifier.size(56.dp).offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun ToolbarButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(icon, desc, tint = Color.Gray) }
}

