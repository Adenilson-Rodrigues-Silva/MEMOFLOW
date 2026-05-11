package com.arsdevstudio.memoflow.ui.screens.recall

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun RecallScreen(
    onBack: () -> Unit,
    viewModel: RecallViewModel = viewModel(factory = RecallViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val remaining by viewModel.remainingRefreshes.collectAsState()
    val textColor = Color(0xFF5D4037)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.paper_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.recall_back), tint = textColor)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.recall_title),
                        color = textColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif
                    )
                    if (uiState !is RecallUiState.LimitReached) {
                        Text(
                            text = stringResource(R.string.recall_refreshes, remaining),
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.loadRandomOldNote() },
                    enabled = remaining > 0 && uiState !is RecallUiState.LimitReached
                ) {
                    Icon(
                        Icons.Default.Refresh, 
                        contentDescription = stringResource(R.string.recall_refresh_desc), 
                        tint = if (remaining > 0) textColor else textColor.copy(alpha = 0.3f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 8.dp)
            ) {
                when (val state = uiState) {
                    is RecallUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = textColor
                        )
                    }
                    is RecallUiState.Empty -> {
                        Text(
                            text = stringResource(R.string.recall_empty),
                            color = textColor,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    is RecallUiState.LimitReached -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.recall_limit_title),
                                color = textColor,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.recall_limit_desc),
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                    is RecallUiState.Success -> {
                        RecallNoteContent(state.note, textColor)
                    }
                }
            }
            
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 48.dp)
                    .size(56.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.recall_close),
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun RecallNoteContent(note: NoteEntity, textColor: Color) {
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val locale = remember { context.resources.configuration.locales[0] }
    val formatter = remember(locale) { 
        DateTimeFormatter.ofPattern(context.getString(R.string.recall_date_format), locale) 
    }
    val dateStr = remember(note.date) {
        Instant.ofEpochMilli(note.date)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.recall_special_moment),
            color = textColor,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(16.dp)
        ) {
            Text(
                text = dateStr,
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (note.title.isNotEmpty() && note.title != "Hoje") {
                Text(
                    text = note.title,
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val plainText = note.contentHtml
                .replace(Regex("<[^>]*>"), "")
                .replace("&nbsp;", " ")

            Text(
                text = plainText,
                color = textColor,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontFamily = FontFamily.Serif
            )

            if (note.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Agora percorremos todas as imagens da nota
                note.images.forEach { imageUrl ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .shadow(4.dp)
                            .background(Color.White)
                            .padding(6.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            if (note.audioPath != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(textColor.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = textColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(textColor.copy(alpha = 0.2f))) {
                        Box(modifier = Modifier.fillMaxWidth(0.4f).fillMaxHeight().background(textColor))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("30s", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

