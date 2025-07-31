package com.example.fastreading2.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fastreading2.data.QuizQuestion
import com.example.fastreading2.data.QuizResult
import com.example.fastreading2.viewmodel.SpeedReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: SpeedReadingViewModel,
    onClose: () -> Unit
) {
    val showQuiz by viewModel.showQuiz.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    
    if (showQuiz) {
        QuizDialog(
            quizState = quizState,
            onAnswerSelected = { questionIndex, answerIndex ->
                viewModel.selectAnswer(questionIndex, answerIndex)
            },
            onClose = {
                viewModel.closeQuiz()
                onClose()
            }
        )
    }
}

@Composable
private fun QuizDialog(
    quizState: com.example.fastreading2.data.QuizState,
    onAnswerSelected: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (quizState.isCompleted && quizState.result != null) {
                    QuizResults(
                        result = quizState.result!!,
                        onClose = onClose
                    )
                } else {
                    QuizQuestion(
                        questions = quizState.questions,
                        currentQuestionIndex = quizState.currentQuestionIndex,
                        answers = quizState.answers,
                        onAnswerSelected = onAnswerSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestion(
    questions: List<QuizQuestion>,
    currentQuestionIndex: Int,
    answers: Map<Int, Int>,
    onAnswerSelected: (Int, Int) -> Unit
) {
    if (questions.isEmpty() || currentQuestionIndex >= questions.size) {
        return
    }
    
    val question = questions[currentQuestionIndex]
    val selectedAnswer = answers[currentQuestionIndex]
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "📝 Quiz de Compreensão",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Pergunta ${currentQuestionIndex + 1} de ${questions.size}",
            fontSize = 16.sp,
            color = Color(0xFF718096),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Question
        Text(
            text = question.question,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Options
        question.options.forEachIndexed { index, option ->
            val isSelected = selectedAnswer == index
            val isCorrect = index == question.correctAnswerIndex
            val showFeedback = selectedAnswer != null
            
            QuizOption(
                text = option,
                isSelected = isSelected,
                isCorrect = isCorrect,
                showFeedback = showFeedback,
                onClick = {
                    if (selectedAnswer == null) {
                        onAnswerSelected(currentQuestionIndex, index)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Feedback
        if (selectedAnswer != null) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = question.explanation,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF2D3748),
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (currentQuestionIndex < questions.size - 1) {
                        // Next question will be handled by ViewModel
                    } else {
                        // Quiz completed
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = if (currentQuestionIndex < questions.size - 1) {
                        "Próxima Pergunta"
                    } else {
                        "Ver Resultados"
                    }
                )
            }
        }
    }
}

@Composable
private fun QuizOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showFeedback && isCorrect -> Color(0xFF48BB78) // Green for correct
        showFeedback && isSelected && !isCorrect -> Color(0xFFE53E3E) // Red for incorrect
        isSelected -> Color(0xFF667EEA) // Blue for selected
        else -> Color(0xFFF7FAFC) // Light gray for default
    }
    
    val textColor = when {
        showFeedback && (isCorrect || (isSelected && !isCorrect)) -> Color.White
        isSelected -> Color.White
        else -> Color(0xFF2D3748)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    showFeedback && isCorrect -> "✅"
                    showFeedback && isSelected && !isCorrect -> "❌"
                    isSelected -> "✓"
                    else -> "○"
                },
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Text(
                text = text,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuizResults(
    result: QuizResult,
    onClose: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score
        Text(
            text = "${result.finalScore}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF667EEA)
        )
        
        Text(
            text = "Pontuação Final",
            fontSize = 18.sp,
            color = Color(0xFF718096),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Performance metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PerformanceMetric(
                label = "PPM",
                value = result.readingSpeed.toString(),
                color = Color(0xFF667EEA)
            )
            
            PerformanceMetric(
                label = "Compreensão",
                value = "${result.comprehensionRate}%",
                color = Color(0xFF48BB78)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ADHD Feedback
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFB74D).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎯 Feedback para TDAH",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = result.adhdFeedback,
                    fontSize = 14.sp,
                    color = Color(0xFF2D3748),
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Suggestions
        if (result.suggestions.isNotEmpty()) {
            Text(
                text = "💡 Sugestões:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            result.suggestions.forEach { suggestion ->
                Text(
                    text = "• $suggestion",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF48BB78)
            )
        ) {
            Text("Continuar Treinando")
        }
    }
}

@Composable
private fun PerformanceMetric(
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
            color = Color(0xFF718096)
        )
    }
} 