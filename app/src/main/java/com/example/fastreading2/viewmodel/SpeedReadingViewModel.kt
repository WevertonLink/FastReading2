package com.example.fastreading2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastreading2.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class SpeedReadingViewModel : ViewModel() {
    
    // ===== STATE FLOWS =====
    private val _currentWord = MutableStateFlow("Digite um assunto ou use o texto padrão")
    val currentWord: StateFlow<String> = _currentWord.asStateFlow()
    
    private val _isReading = MutableStateFlow(false)
    val isReading: StateFlow<Boolean> = _isReading.asStateFlow()
    
    private val _readingStats = MutableStateFlow(ReadingStats())
    val readingStats: StateFlow<ReadingStats> = _readingStats.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _showQuiz = MutableStateFlow(false)
    val showQuiz: StateFlow<Boolean> = _showQuiz.asStateFlow()
    
    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()
    
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()
    
    // ===== PRIVATE VARIABLES =====
    private var readingSession = ReadingSession()
    private var readingJob: kotlinx.coroutines.Job? = null
    private var currentText = ""
    
    // ===== PUBLIC FUNCTIONS =====
    
    /**
     * Inicia a leitura rápida
     */
    fun startReading() {
        if (readingSession.words.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor, gere um texto ou adicione seu próprio texto!"
            )
            return
        }
        
        _isReading.value = true
        readingSession = readingSession.copy(
            isRunning = true,
            startTime = System.currentTimeMillis()
        )
        
        startReadingJob()
    }
    
    /**
     * Pausa a leitura
     */
    fun pauseReading() {
        _isReading.value = false
        readingSession = readingSession.copy(isRunning = false)
        readingJob?.cancel()
    }
    
    /**
     * Reinicia a leitura
     */
    fun resetReading() {
        pauseReading()
        readingSession = readingSession.copy(
            currentIndex = 0,
            startTime = null
        )
        _currentWord.value = if (readingSession.words.isNotEmpty()) {
            readingSession.words[0]
        } else {
            "Digite um assunto ou use o texto padrão"
        }
        updateStats()
        updateProgress()
    }
    
    /**
     * Altera a velocidade de leitura
     */
    fun changeSpeed(newSpeed: Int) {
        val speed = newSpeed.coerceIn(100, 1000)
        readingSession = readingSession.copy(wordsPerMinute = speed)
        _readingStats.value = _readingStats.value.copy(currentSpeed = speed)
        
        if (_isReading.value) {
            startReadingJob()
        }
    }
    
    /**
     * Gera texto baseado em um tópico
     */
    fun generateText(topic: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                delay(2000) // Simula geração de texto
                val generatedText = generateTextForTopic(topic)
                currentText = generatedText
                loadText(generatedText)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao gerar texto: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Carrega texto personalizado
     */
    fun loadCustomText(text: String) {
        currentText = text
        loadText(text)
    }
    
    /**
     * Inicia o quiz após completar a leitura
     */
    fun startQuiz() {
        val questions = generateQuizQuestions(currentText)
        _quizState.value = QuizState(questions = questions)
        _showQuiz.value = true
    }
    
    /**
     * Seleciona resposta do quiz
     */
    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        val currentQuizState = _quizState.value
        val updatedAnswers = currentQuizState.answers.toMutableMap()
        updatedAnswers[questionIndex] = answerIndex
        
        _quizState.value = currentQuizState.copy(answers = updatedAnswers)
        
        // Se é a última pergunta, calcular resultado
        if (questionIndex == currentQuizState.questions.size - 1) {
            calculateQuizResult()
        }
    }
    
    /**
     * Fecha o quiz
     */
    fun closeQuiz() {
        _showQuiz.value = false
        _quizState.value = QuizState()
    }
    
    /**
     * Ativa/desativa modo foco
     */
    fun toggleFocusMode() {
        _uiState.value = _uiState.value.copy(
            isFocusMode = !_uiState.value.isFocusMode
        )
    }
    
    // ===== PRIVATE FUNCTIONS =====
    
    private fun startReadingJob() {
        readingJob?.cancel()
        readingJob = viewModelScope.launch {
            val words = readingSession.words
            val speed = readingSession.wordsPerMinute
            val intervalMs = (60.0 / speed * 1000).toLong()
            
            while (readingSession.currentIndex < words.size && _isReading.value) {
                val currentIndex = readingSession.currentIndex
                _currentWord.value = words[currentIndex]
                
                readingSession = readingSession.copy(currentIndex = currentIndex + 1)
                updateStats()
                updateProgress()
                
                delay(intervalMs)
            }
            
            if (readingSession.currentIndex >= words.size) {
                completeReading()
            }
        }
    }
    
    private fun updateStats() {
        val wordsRead = readingSession.currentIndex
        val startTime = readingSession.startTime
        val timeElapsed = if (startTime != null) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else 0
        
        val currentSpeed = if (timeElapsed > 0) {
            ((wordsRead * 60) / timeElapsed).coerceAtLeast(1)
        } else readingSession.wordsPerMinute
        
        _readingStats.value = ReadingStats(
            wordsRead = wordsRead,
            timeElapsed = timeElapsed,
            currentSpeed = currentSpeed,
            averageSpeed = currentSpeed,
            progress = if (readingSession.words.isNotEmpty()) {
                wordsRead.toFloat() / readingSession.words.size
            } else 0f
        )
    }
    
    private fun updateProgress() {
        val progress = if (readingSession.words.isNotEmpty()) {
            readingSession.currentIndex.toFloat() / readingSession.words.size
        } else 0f
        _progress.value = progress
    }
    
    private fun completeReading() {
        _isReading.value = false
        readingSession = readingSession.copy(isRunning = false)
        _currentWord.value = "🎉 Texto concluído!"
        
        // Iniciar quiz após um delay
        viewModelScope.launch {
            delay(1000)
            startQuiz()
        }
    }
    
    private fun loadText(text: String) {
        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        readingSession = readingSession.copy(
            words = words,
            currentIndex = 0
        )
        
        if (words.isNotEmpty()) {
            _currentWord.value = words[0]
        }
        
        updateStats()
        updateProgress()
    }
    
    private fun generateTextForTopic(topic: String): String {
        // Base de conhecimento simples para gerar textos educativos
        val topicLower = topic.lowercase()
        
        return when {
            topicLower.contains("fotossíntese") || topicLower.contains("plantas") -> {
                "A fotossíntese é um processo biológico fundamental realizado pelas plantas, algas e algumas bactérias. Durante este processo, os organismos convertem energia luminosa, geralmente do sol, em energia química armazenada em moléculas de glicose. A fotossíntese ocorre principalmente nas folhas das plantas, especificamente nos cloroplastos, organelas que contêm clorofila. A equação básica da fotossíntese é: 6CO2 + 6H2O + energia luminosa → C6H12O6 + 6O2. Este processo é essencial para a vida na Terra, pois produz oxigênio e serve como base da cadeia alimentar."
            }
            topicLower.contains("brasil") || topicLower.contains("história") -> {
                "A história do Brasil é rica e complexa, começando com os povos indígenas que habitavam o território há milhares de anos. Em 1500, Pedro Álvares Cabral chegou ao Brasil, iniciando o período colonial português. Durante os séculos XVI a XVIII, o Brasil foi explorado principalmente para extração de pau-brasil, cultivo de cana-de-açúcar e mineração de ouro. O trabalho escravo foi amplamente utilizado, trazendo milhões de africanos forçadamente. Em 1822, Dom Pedro I declarou a independência do Brasil. O período imperial durou até 1889, quando foi proclamada a República."
            }
            topicLower.contains("programação") || topicLower.contains("código") -> {
                "A programação é o processo de criar instruções para computadores executarem tarefas específicas. Envolve escrever código usando linguagens de programação como Python, Java, JavaScript, C++ e muitas outras. Cada linguagem tem suas próprias regras de sintaxe e é adequada para diferentes tipos de projetos. Os conceitos fundamentais incluem variáveis, estruturas de controle, funções, algoritmos e estruturas de dados. A programação orientada a objetos organiza código em classes e objetos, facilitando a manutenção e reutilização."
            }
            topicLower.contains("inteligência artificial") || topicLower.contains("ia") -> {
                "A Inteligência Artificial (IA) é um campo da ciência da computação que busca criar sistemas capazes de realizar tarefas que normalmente requerem inteligência humana. Isso inclui aprendizado, raciocínio, percepção, compreensão de linguagem natural e tomada de decisões. A IA moderna se baseia em técnicas como machine learning, deep learning e redes neurais artificiais. O machine learning permite que sistemas aprendam padrões a partir de dados sem programação explícita. Deep learning usa redes neurais profundas para processar informações complexas."
            }
            else -> {
                "$topic é um assunto fascinante que merece estudo aprofundado. Este campo de conhecimento abrange diversos aspectos teóricos e práticos que são fundamentais para compreender sua importância. Os especialistas dedicam anos de pesquisa para desvendar os mistérios e complexidades relacionados a $topic. Através de metodologias científicas rigorosas, é possível analisar diferentes perspectivas e abordagens. A aplicação prática destes conhecimentos tem impacto significativo em nossa sociedade."
            }
        }
    }
    
    private fun generateQuizQuestions(text: String): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        
        // Pergunta 1: Tema principal
        questions.add(
            QuizQuestion(
                question = "Qual é o tema principal do texto?",
                options = listOf(
                    getMainTopic(text),
                    "Técnicas de memorização",
                    "História da tecnologia",
                    "Métodos de estudo"
                ),
                correctAnswerIndex = 0,
                explanation = "O tema principal é identificado pelas palavras-chave mais frequentes no texto.",
                category = QuestionCategory.MAIN_TOPIC
            )
        )
        
        // Pergunta 2: Detalhe específico
        val sentences = text.split("[.!?]+".toRegex()).filter { it.trim().length > 10 }
        val keySentence = sentences.getOrNull(sentences.size / 2) ?: sentences.firstOrNull() ?: ""
        
        questions.add(
            QuizQuestion(
                question = "Segundo o texto, qual afirmação está correta?",
                options = listOf(
                    keySentence.trim(),
                    "O texto não menciona essa informação específica",
                    "Esta afirmação contradiz o que foi apresentado",
                    "Essa informação não estava presente no texto"
                ),
                correctAnswerIndex = 0,
                explanation = "Esta informação estava presente no texto que você acabou de ler.",
                category = QuestionCategory.DETAIL
            )
        )
        
        // Pergunta 3: Compreensão geral
        questions.add(
            QuizQuestion(
                question = "Qual é a ideia central que o texto transmite?",
                options = listOf(
                    getCentralIdea(text),
                    "O texto foca principalmente em aspectos teóricos",
                    "A abordagem apresentada é puramente acadêmica",
                    "O conteúdo enfatiza apenas conceitos básicos"
                ),
                correctAnswerIndex = 0,
                explanation = "A ideia central conecta todos os conceitos apresentados no texto.",
                category = QuestionCategory.COMPREHENSION
            )
        )
        
        return questions
    }
    
    private fun getMainTopic(text: String): String {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("leitura") || lowerText.contains("ler") -> "Técnicas de leitura e compreensão"
            lowerText.contains("fotossíntese") || lowerText.contains("plantas") -> "Processo de fotossíntese nas plantas"
            lowerText.contains("brasil") || lowerText.contains("história") -> "História e desenvolvimento do Brasil"
            lowerText.contains("programação") || lowerText.contains("código") -> "Programação e desenvolvimento de software"
            lowerText.contains("inteligência artificial") || lowerText.contains("machine learning") -> "Inteligência artificial e suas aplicações"
            else -> "Desenvolvimento de habilidades e conhecimento"
        }
    }
    
    private fun getCentralIdea(text: String): String {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("leitura") && lowerText.contains("velocidade") -> "A leitura rápida pode ser desenvolvida com prática"
            lowerText.contains("fotossíntese") -> "A fotossíntese é essencial para a vida na Terra"
            lowerText.contains("brasil") && lowerText.contains("história") -> "O Brasil passou por diversas transformações históricas"
            lowerText.contains("programação") -> "A programação envolve resolver problemas através de código"
            lowerText.contains("inteligência artificial") -> "A IA está transformando diversas áreas da sociedade"
            else -> "O conhecimento se desenvolve através de estudo e prática"
        }
    }
    
    private fun calculateQuizResult() {
        val currentQuizState = _quizState.value
        val questions = currentQuizState.questions
        val answers = currentQuizState.answers
        
        val correctAnswers = questions.mapIndexedNotNull { index, question ->
            if (answers[index] == question.correctAnswerIndex) 1 else null
        }.size
        
        val comprehensionRate = if (questions.isNotEmpty()) {
            ((correctAnswers.toFloat() / questions.size) * 100).toInt()
        } else 0
        val readingSpeed = _readingStats.value.currentSpeed
        val finalScore = (readingSpeed * comprehensionRate) / 100
        
        val adhdFeedback = generateADHDFeedback(readingSpeed, comprehensionRate)
        
        val result = QuizResult(
            readingSpeed = readingSpeed,
            comprehensionRate = comprehensionRate,
            finalScore = finalScore,
            adhdFeedback = adhdFeedback.message,
            suggestions = adhdFeedback.suggestions
        )
        
        _quizState.value = currentQuizState.copy(
            isCompleted = true,
            result = result
        )
    }
    
    private fun generateADHDFeedback(speed: Int, comprehension: Int): ADHDFeedback {
        return when {
            comprehension >= 80 && speed >= 400 -> {
                ADHDFeedback(
                    type = FeedbackType.BALANCED,
                    message = "Excelente! Você está conseguindo manter alta velocidade com ótima compreensão. Continue assim!",
                    suggestions = listOf(
                        "Tente aumentar gradualmente a velocidade",
                        "Mantenha o foco na compreensão",
                        "Pratique com textos mais complexos"
                    )
                )
            }
            comprehension >= 80 -> {
                ADHDFeedback(
                    type = FeedbackType.COMPREHENSION_HIGH,
                    message = "Ótima compreensão! Agora você pode tentar aumentar gradualmente a velocidade.",
                    suggestions = listOf(
                        "Aumente a velocidade em 50 PPM",
                        "Mantenha a qualidade da compreensão",
                        "Pratique regularmente"
                    )
                )
            }
            comprehension >= 60 -> {
                ADHDFeedback(
                    type = FeedbackType.IMPROVEMENT,
                    message = "Boa compreensão! Tente diminuir um pouco a velocidade para melhorar o entendimento.",
                    suggestions = listOf(
                        "Diminua a velocidade em 50 PPM",
                        "Foque na compreensão primeiro",
                        "Pratique com textos mais simples"
                    )
                )
            }
            else -> {
                ADHDFeedback(
                    type = FeedbackType.COMPREHENSION_LOW,
                    message = "Foque na compreensão primeiro. Diminua a velocidade e pratique com textos mais simples.",
                    suggestions = listOf(
                        "Comece com velocidades menores (200-250 PPM)",
                        "Pratique com textos mais curtos",
                        "Use o modo foco para reduzir distrações"
                    )
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        readingJob?.cancel()
    }
} 