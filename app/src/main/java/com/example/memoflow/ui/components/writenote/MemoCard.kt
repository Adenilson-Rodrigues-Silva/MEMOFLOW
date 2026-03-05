package com.example.memoflow.ui.components.writenote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MemoCard(
    title: String,
    content: String,
    emoji: String,
    humor: String,
    date: String,
    images: List<String>,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val cyan = Color(0xFF00E5FF)
    val purple = Color(0xFFD500F9)
    val neonGreen = Color(0xFF00FFC2)
    val context = LocalContext.current

    // Limita o conteúdo para caber no card
    val contentSnippet = if (content.length > 450) {
        content.substring(0, 450) + "..."
    } else {
        content
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f) // Proporção de Story
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0A), Color(0xFF151515))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(cyan, purple)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        // Data no topo
        Text(
            text = date,
            color = Color.Gray.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontFamily = fontFamily,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Conteúdo centralizado verticalmente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (title.isNotEmpty() && title != "Hoje") {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = contentSnippet,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 19.sp,
                lineHeight = 28.sp,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Rodapé
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Humor à esquerda
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = humor,
                    color = neonGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = fontFamily
                )
            }

            // Branding centralizado mas muito discreto
            Text(
                text = "MemoFlow",
                color = Color.Gray.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.align(Alignment.Center)
            )

            // Imagens à direita
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                images.take(3).forEach { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
