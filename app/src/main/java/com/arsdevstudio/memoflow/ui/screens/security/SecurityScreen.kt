package com.arsdevstudio.memoflow.ui.screens.security

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.arsdevstudio.memoflow.R
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    viewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory)
) {
    val context = LocalContext.current
    val neonGreen = Color(0xFF00FFC2)
    val userSettings by viewModel.userSettings.collectAsState()
    
    var showPinDialog by remember { mutableStateOf(false) }
    var showRemovePinDialog by remember { mutableStateOf(false) }

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(stringResource(R.string.security_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                stringResource(R.string.security_desc),
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Biometria
            SecurityToggleItem(
                title = stringResource(R.string.security_biometric_title),
                description = stringResource(R.string.security_biometric_desc),
                icon = Icons.Default.Fingerprint,
                checked = userSettings.isBiometricEnabled,
                onCheckedChange = { viewModel.updateBiometric(it) },
                neonGreen = neonGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN das Notas
            if (userSettings.pin.isNullOrEmpty()) {
                SecurityClickItem(
                    title = stringResource(R.string.security_set_pin_title),
                    description = stringResource(R.string.security_set_pin_desc),
                    icon = Icons.Default.Lock,
                    onClick = { showPinDialog = true },
                    neonGreen = neonGreen
                )
            } else {
                SecurityClickItem(
                    title = stringResource(R.string.security_pin_active_title),
                    description = stringResource(R.string.security_pin_active_desc),
                    icon = Icons.Default.LockOpen,
                    onClick = { showRemovePinDialog = true },
                    neonGreen = neonGreen,
                    titleColor = neonGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tempo de Bloqueio (Futuro)
            SecurityToggleItem(
                title = stringResource(R.string.security_auto_lock_title),
                description = stringResource(R.string.security_auto_lock_desc),
                icon = Icons.Default.Timer,
                checked = true,
                onCheckedChange = {}, 
                neonGreen = neonGreen
            )
        }
    }

    if (showPinDialog) {
        var pinStep by remember { mutableIntStateOf(1) } // 1: Definir, 2: Confirmar
        var firstPin by remember { mutableStateOf("") }
        var secondPin by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { 
                Text(
                    if (pinStep == 1) stringResource(R.string.security_dialog_set_pin) else stringResource(R.string.security_dialog_confirm_pin), 
                    color = Color.White 
                ) 
            },
            text = {
                Column {
                    Text(
                        if (pinStep == 1) stringResource(R.string.security_pin_instructions) else stringResource(R.string.security_pin_confirm_instructions),
                        color = Color.Gray, 
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = if (pinStep == 1) firstPin else secondPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                if (pinStep == 1) firstPin = input else secondPin = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = neonGreen,
                            focusedIndicatorColor = neonGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinStep == 1) {
                            if (firstPin.length == 4) pinStep = 2
                        } else {
                            if (firstPin == secondPin) {
                                viewModel.updatePin(firstPin)
                                Toast.makeText(context, context.getString(R.string.security_pin_success), Toast.LENGTH_SHORT).show()
                                showPinDialog = false
                            } else {
                                Toast.makeText(context, context.getString(R.string.security_pin_mismatch), Toast.LENGTH_SHORT).show()
                                secondPin = ""
                            }
                        }
                    },
                    enabled = if (pinStep == 1) firstPin.length == 4 else secondPin.length == 4
                ) {
                    Text(if (pinStep == 1) stringResource(R.string.security_next) else stringResource(R.string.security_confirm), color = neonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(stringResource(R.string.security_cancel), color = Color.White)
                }
            }
        )
    }

    if (showRemovePinDialog) {
        AlertDialog(
            onDismissRequest = { showRemovePinDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text(stringResource(R.string.security_remove_pin_title), color = Color.White) },
            text = { Text(stringResource(R.string.security_remove_pin_desc), color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removePin()
                    showRemovePinDialog = false
                    Toast.makeText(context, context.getString(R.string.security_pin_removed_toast), Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.security_remove_action), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePinDialog = false }) {
                    Text(stringResource(R.string.security_cancel), color = Color.White)
                }
            }
        )
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

@Composable
fun SecurityClickItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    neonGreen: Color,
    titleColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = neonGreen, modifier = Modifier.size(28.dp))

        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

