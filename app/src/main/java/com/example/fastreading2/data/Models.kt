package com.example.fastreading2.data

import androidx.compose.runtime.Stable
import java.util.UUID

/**
 * Modelo para representar uma pergunta do quiz
 */
@Stable
data class QuizQuestion(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: QuestionCategory = QuestionCategory.COMPREHENSION
)

/**
 * Categorias de perguntas para melhor organização
 */
enum class QuestionCategory {
    MAIN_TOPIC,      // Tema principal
    DETAIL,          // Detalhe específico
    COMPREHENSION    // Compreensão geral
}

/**
 * Resultado completo do quiz
 */
@Stable
data class QuizResult(
    val readingSpeed: Int,           // PPM (Palavras por minuto)
    val comprehensionRate: Int,      // % de acertos
    val finalScore: Int,             // Score combinado
    val adhdFeedback: String,        // Feedback específico para TDAH
    val suggestions: List<String>,   // Sugestões de melhoria
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Sessão de leitura atual
 */
@Stable
data class ReadingSession(
    val words: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isRunning: Boolean = false,
    val startTime: Long? = null,
    val wordsPerMinute: Int = 300,
    val sessionId: String = UUID.randomUUID().toString()
)

/**
 * Estatísticas em tempo real da leitura
 */
@Stable
data class ReadingStats(
    val wordsRead: Int = 0,
    val timeElapsed: Int = 0,
    val currentSpeed: Int = 300,
    val averageSpeed: Int = 0,
    val progress: Float = 0f
)

/**
 * Configurações do usuário
 */
@Stable
data class UserSettings(
    val defaultSpeed: Int = 300,
    val enableSound: Boolean = true,
    val enableVibration: Boolean = true,
    val focusModeEnabled: Boolean = true,
    val adhdOptimizations: Boolean = true
)

/**
 * Estado da UI principal
 */
@Stable
data class UIState(
    val currentScreen: Screen = Screen.HOME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showQuiz: Boolean = false,
    val isFocusMode: Boolean = false
)

/**
 * Telas do aplicativo
 */
enum class Screen {
    HOME,       // Tela principal
    READING,    // Tela de leitura
    QUIZ,       // Tela do quiz
    RESULTS     // Tela de resultados
}

/**
 * Eventos de UI
 */
sealed class UIEvent {
    object StartReading : UIEvent()
    object PauseReading : UIEvent()
    object ResetReading : UIEvent()
    object CompleteReading : UIEvent()
    object StartQuiz : UIEvent()
    object CloseQuiz : UIEvent()
    data class SpeedChanged(val newSpeed: Int) : UIEvent()
    data class TextGenerated(val text: String) : UIEvent()
    data class AnswerSelected(val questionIndex: Int, val answerIndex: Int) : UIEvent()
    data class TopicSubmitted(val topic: String) : UIEvent()
}

/**
 * Modelo para texto gerado
 */
@Stable
data class GeneratedText(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val content: String,
    val wordCount: Int,
    val difficulty: TextDifficulty = TextDifficulty.MEDIUM,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Níveis de dificuldade do texto
 */
enum class TextDifficulty {
    EASY,       // 100-200 palavras
    MEDIUM,     // 200-400 palavras
    HARD        // 400+ palavras
}

/**
 * Histórico de sessões
 */
@Stable
data class SessionHistory(
    val sessionId: String,
    val date: Long,
    val topic: String,
    val readingSpeed: Int,
    val comprehensionRate: Int,
    val finalScore: Int,
    val duration: Int // em segundos
)

/**
 * Feedback específico para TDAH
 */
@Stable
data class ADHDFeedback(
    val type: FeedbackType,
    val message: String,
    val suggestions: List<String>,
    val isEncouraging: Boolean = true
)

/**
 * Tipos de feedback
 */
enum class FeedbackType {
    SPEED_TOO_HIGH,      // Velocidade muito alta
    SPEED_TOO_LOW,       // Velocidade muito baixa
    COMPREHENSION_LOW,   // Compreensão baixa
    COMPREHENSION_HIGH,  // Compreensão alta
    BALANCED,           // Equilibrado
    IMPROVEMENT         // Melhoria
}

/**
 * Configurações de animação
 */
@Stable
data class AnimationSettings(
    val enableAnimations: Boolean = true,
    val animationDuration: Int = 300, // ms
    val enableHapticFeedback: Boolean = true,
    val enableSoundEffects: Boolean = true
)

/**
 * Estado do quiz
 */
@Stable
data class QuizState(
    val currentQuestionIndex: Int = 0,
    val questions: List<QuizQuestion> = emptyList(),
    val answers: MutableMap<Int, Int> = mutableMapOf(),
    val isCompleted: Boolean = false,
    val result: QuizResult? = null
) 