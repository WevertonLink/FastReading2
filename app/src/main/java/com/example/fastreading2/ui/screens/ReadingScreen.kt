package com.example.fastreading2.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastreading2.viewmodel.SpeedReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    viewModel: SpeedReadingViewModel,
    onNavigateToHome: () -> Unit
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val readingStats by viewModel.readingStats.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var speed by remember { mutableStateOf(300) }
    
    // Animation for word display
    val infiniteTransition = rememberInfiniteTransition(label = "wordAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (uiState.isFocusMode) {
                    Color.White
                } else {
                    Color(0xFF0F0F23)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with back button
            if (!uiState.isFocusMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToHome
                    ) {
                        Text(
                            text = "←",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                    
                    Text(
                        text = "📖 Leitura Rápida",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = { viewModel.toggleFocusMode() }
                    ) {
                        Text(
                            text = if (uiState.isFocusMode) "🔍" else "🎯",
                            fontSize = 20.sp
                        )
                    }
                }
            }
            
            // Main word display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isFocusMode) {
                            Color.Transparent
                        } else {
                            Color.White.copy(alpha = 0.95f)
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentWord,
                            fontSize = if (uiState.isFocusMode) 48.sp else 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isFocusMode) Color.Black else Color(0xFF2D3748),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.scale(if (isReading) scale else 1f)
                        )
                    }
                }
            }
            
            // Progress bar
            if (!uiState.isFocusMode) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp),
                    color = Color(0xFF667EEA),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Controls section
            if (!uiState.isFocusMode) {
                // Speed control
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "⚡ Velocidade de Leitura",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "100 PPM",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            
                            Slider(
                                value = speed.toFloat(),
                                onValueChange = { 
                                    speed = it.toInt()
                                    viewModel.changeSpeed(speed)
                                },
                                valueRange = 100f..1000f,
                                steps = 17,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF667EEA),
                                    activeTrackColor = Color(0xFF667EEA),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            
                            Text(
                                text = "1000 PPM",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        Text(
                            text = "${speed} PPM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Control buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.startReading() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF48BB78)
                        ),
                        enabled = !isReading
                    ) {
                        Text("▶️ Iniciar")
                    }
                    
                    Button(
                        onClick = { viewModel.pauseReading() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF6AD55)
                        ),
                        enabled = isReading
                    ) {
                        Text("⏸️ Pausar")
                    }
                }
                
                Button(
                    onClick = { viewModel.resetReading() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53E3E)
                    )
                ) {
                    Text("🔄 Reiniciar")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "PPM",
                            value = readingStats.currentSpeed.toString(),
                            color = Color(0xFF667EEA)
                        )
                        
                        StatItem(
                            label = "Palavras",
                            value = readingStats.wordsRead.toString(),
                            color = Color(0xFF48BB78)
                        )
                        
                        StatItem(
                            label = "Tempo",
                            value = "${readingStats.timeElapsed}s",
                            color = Color(0xFFF6AD55)
                        )
                    }
                }
            }
        }
        
        // Focus mode overlay
        if (uiState.isFocusMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Tap to exit focus mode
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFocusMode() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
} 