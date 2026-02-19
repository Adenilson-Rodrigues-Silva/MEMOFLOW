package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.memoflow.ui.theme.MemoFlowTheme
// Importe o pacote inteiro para evitar que o compilador pare na linha 13
import com.example.memoflow.ui.screens.* class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoFlowTheme {
                // Use uma String simples para navegar
                var currentScreen by remember { mutableStateOf("splash") }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(onFinished = {
                            currentScreen = "home"
                        })
                    }
                    "home" -> {
                        HomeScreen()
                    }
                }
            }
        }
    }
}