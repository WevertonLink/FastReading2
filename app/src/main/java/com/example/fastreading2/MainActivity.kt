package com.example.fastreading2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastreading2.ui.screens.HomeScreen
import com.example.fastreading2.ui.screens.QuizScreen
import com.example.fastreading2.ui.screens.ReadingScreen
import com.example.fastreading2.ui.theme.FastReading2Theme
import com.example.fastreading2.viewmodel.SpeedReadingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastReading2Theme {
                SpeedReadingApp()
            }
        }
    }
}

@Composable
fun SpeedReadingApp() {
    val viewModel: SpeedReadingViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("home") }
    
    when (currentScreen) {
        "home" -> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToReading = {
                    currentScreen = "reading"
                }
            )
        }
        "reading" -> {
            ReadingScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    currentScreen = "home"
                }
            )
        }
    }
    
    // Quiz overlay - can appear on any screen
    QuizScreen(
        viewModel = viewModel,
        onClose = {
            // Quiz closed, stay on current screen
        }
    )
}