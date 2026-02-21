package com.example.memoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    val neonGreen = Color(0xFF00FFC2)

    // ESTRATÉGICO: Estados para controlar os switches
    var isBiometryEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text("Segurança", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "Proteja suas memórias com biometria ou PIN.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ESTRATÉGICO: Opção de Biometria com Switch
            SecurityToggleItem(
                title = "Ativar Biometria",
                description = "Usar digital para desbloquear",
                icon = Icons.Default.Fingerprint,
                checked = isBiometryEnabled,
                onCheckedChange = { isBiometryEnabled = it },
                neonGreen = neonGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ESTRATÉGICO: Placeholder para o Tempo de Bloqueio
            SecurityToggleItem(
                title = "Tempo de Bloqueio",
                description = "Bloquear após 30 segundos",
                icon = Icons.Default.Timer,
                checked = true,
                onCheckedChange = {}, // Faremos a lógica de tempo depois
                neonGreen = neonGreen
            )
        }
    }
}

@Composable
fun SecurityToggleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    neonGreen: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = neonGreen, modifier = Modifier.size(28.dp))

        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }

        // O Switch elegante
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = neonGreen,
                checkedTrackColor = neonGreen.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun SecurityScreenPreview() {
    com.example.memoflow.ui.theme.MemoFlowTheme(darkTheme = true) {
        SecurityScreen(onBack = { })
    }
}