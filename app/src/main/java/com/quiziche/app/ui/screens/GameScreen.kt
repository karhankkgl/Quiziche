package com.quiziche.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import com.quiziche.app.data.model.Question
import com.quiziche.app.data.repository.QuizRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.data.model.GameSession
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    isSingleplayer: Boolean = false,
    category: String = "all",
    roomId: String? = null,
    onNavigateToResults: (score: Int, userNickname: String, opponentScore: Int, opponentName: String, totalQuestions: Int, category: String, isWinner: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val quizRepository = remember { QuizRepository() }
    val userRepository = remember { UserRepository() }
    val authRepository = remember { AuthRepository() }
    val gameRepository = remember { GameRepository() }

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var userScore by remember { mutableStateOf(0) }
    
    var opponentScore by remember { mutableStateOf(0) }
    var userNickname by remember { mutableStateOf("You") }
    var opponentName by remember { mutableStateOf(if (isSingleplayer) "Solo Bot" else "Opponent") }
    var isPlayer1 by remember { mutableStateOf(true) }

    var isGameStarted by remember { mutableStateOf(false) }
    var player1Ready by remember { mutableStateOf(false) }
    var player2Ready by remember { mutableStateOf(false) }
    var player1Answered by remember { mutableStateOf(false) }
    var player2Answered by remember { mutableStateOf(false) }
    var showSyncResult by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(15) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    fun finalizeGame() {
        scope.launch {
            authRepository.currentUserUID?.let { uid ->
                val won = if (isSingleplayer) userScore > 2 else userScore > opponentScore
                val displayCategory = category.replaceFirstChar { it.uppercase() }
                
                userRepository.updateStats(uid, won, displayCategory, xpEarned = userScore * 10, coinsEarned = userScore * 5, isSingleplayer = isSingleplayer)
                
                val matchData = mapOf(
                    "opponentIcon" to if (isSingleplayer) "🤖" else "🎯",
                    "opponentName" to opponentName,
                    "result" to if (won) "won" else "lost",
                    "score" to "$userScore - $opponentScore",
                    "category" to displayCategory,
                    "timestamp" to System.currentTimeMillis()
                )
                userRepository.saveMatchResult(uid, matchData)

                if (!isSingleplayer && roomId != null) {
                    gameRepository.completeGame(roomId)
                }
                
                onNavigateToResults(userScore, userNickname, opponentScore, opponentName, questions.size, category, won)
            }
        }
    }

    fun triggerNextQuestion() {
        if (isSingleplayer) {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedAnswer = null
                showSyncResult = false
                timeLeft = 15
            } else {
                finalizeGame()
            }
        } else if (roomId != null && isPlayer1) {
            scope.launch {
                if (currentQuestionIndex < questions.size - 1) {
                    gameRepository.syncNextQuestion(roomId, currentQuestionIndex + 1)
                } else {
                    finalizeGame()
                }
            }
        }
    }

    // Observe Multiplayer Session
    LaunchedEffect(roomId) {
        // Fetch own nickname
        authRepository.currentUserUID?.let { uid ->
            val result = userRepository.getUserProfile(uid)
            if (result.isSuccess) {
                userNickname = result.getOrNull()?.name ?: "You"
            }
        }

        if (!isSingleplayer && roomId != null) {
            gameRepository.observeGameSession(roomId).collectLatest { session ->
                if (session != null) {
                    val uid = authRepository.currentUserUID
                    isPlayer1 = session.player1Id == uid
                    val opponentUid = if (isPlayer1) session.player2Id else session.player1Id
                    
                    opponentScore = if (isPlayer1) session.player2Score else session.player1Score
                    
                    // Fetch opponent's name if we don't have it
                    if (opponentUid.isNotEmpty() && (opponentName == "Opponent" || opponentName == "Solo Bot")) {
                        val result = userRepository.getUserProfile(opponentUid)
                        if (result.isSuccess) {
                            opponentName = result.getOrNull()?.name ?: "Opponent"
                        }
                    }
                    
                    player1Ready = session.player1Ready
                    player2Ready = session.player2Ready
                    player1Answered = session.player1Answered
                    player2Answered = session.player2Answered
                    
                    // Start game when both are ready
                    if (!isGameStarted && player1Ready && player2Ready) {
                        isGameStarted = true
                        if (isPlayer1 && session.currentQuestionStartTime == 0L) {
                            gameRepository.syncNextQuestion(roomId, 0)
                        }
                    }

                    // Sync question index
                    if (currentQuestionIndex != session.currentQuestionIndex) {
                        currentQuestionIndex = session.currentQuestionIndex
                        selectedAnswer = null
                        showSyncResult = false
                        timeLeft = 15
                    }

                    // Automatic progression if both answered
                    if (isPlayer1 && isGameStarted && session.player1Answered && session.player2Answered && !showSyncResult) {
                        scope.launch {
                            showSyncResult = true
                            delay(2500)
                            triggerNextQuestion()
                        }
                    } else if (!isPlayer1 && isGameStarted && session.player1Answered && session.player2Answered) {
                        showSyncResult = true
                    }

                    // Initial load of questions
                    if (questions.isEmpty() && session.questionIds.isNotEmpty()) {
                        val result = quizRepository.getQuestionsByCategory(category, 5)
                        if (result.isSuccess) {
                            questions = result.getOrDefault(emptyList())
                        }
                        isLoading = false
                        // Set self as ready
                        gameRepository.setReady(roomId, isPlayer1, true)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isSingleplayer || roomId == null) {
            val result = quizRepository.getQuestionsByCategory(category, 5)
            if (result.isSuccess) {
                questions = result.getOrDefault(emptyList()).shuffled().take(5)
            }
            isLoading = false
            isGameStarted = true
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    LaunchedEffect(timeLeft, isLoading, isGameStarted) {
        if (!isLoading && isGameStarted && currentQuestion != null) {
            if (timeLeft > 0 && selectedAnswer == null) {
                delay(1000)
                timeLeft -= 1
            } else if (timeLeft == 0 && selectedAnswer == null) {
                if (isSingleplayer) {
                    delay(1500)
                    triggerNextQuestion()
                } else if (roomId != null) {
                    // Mark as answered with 0 points
                    gameRepository.submitAnswer(roomId, isPlayer1, userScore)
                }
            }
        }
    }

    // Handle selection
    fun onAnswerSelected(index: Int) {
        if (selectedAnswer == null) {
            selectedAnswer = index
            val isCorrect = index == currentQuestion?.correctAnswerIndex
            if (isCorrect) {
                // Speed-based scoring: 10 base + up to 10 bonus
                val points = 10 + (timeLeft * 1) 
                userScore += points
            }
            
            if (isSingleplayer) {
                scope.launch {
                    showSyncResult = true
                    delay(2500)
                    triggerNextQuestion()
                }
            } else if (roomId != null) {
                scope.launch {
                    gameRepository.submitAnswer(roomId, isPlayer1, userScore)
                }
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E), Color(0xFF2D1B69)))),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧪", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFFFBBF24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Loading questions...", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 16.sp)
            }
        }
        return
    }

    if (questions.isEmpty() || currentQuestion == null) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E)))),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("😢", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("No questions found!", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                CartoonButton(
                    text = "🏠  Return",
                    onClick = { onNavigateToResults(0, userNickname, 0, "Opponent", 0, category, false) },
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                    textColor = Color(0xFF1E1B4B)
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0020), Color(0xFF1A0A2E), Color(0xFF2D1B69))
                )
            )
    ) {
        // Background star decorations
        Text("⭐", fontSize = 16.sp, modifier = Modifier.offset(20.dp, 50.dp), color = Color.White.copy(0.2f))
        Text("✨", fontSize = 12.sp, modifier = Modifier.offset(310.dp, 80.dp), color = Color.White.copy(0.15f))
        Text("🌟", fontSize = 14.sp, modifier = Modifier.offset(280.dp, 160.dp), color = Color.White.copy(0.15f))
        Text("⭐", fontSize = 10.sp, modifier = Modifier.offset(50.dp, 200.dp), color = Color.White.copy(0.2f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            // Top Bar - Timer & Scores
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                // Timer Progress Bar
                val progress = timeLeft / 15f
                val progressColor by animateColorAsState(
                    if (timeLeft <= 5) Color.Red else Color(0xFF4ADE80),
                    label = "color"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(progressColor)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScoreCard(
                        name = userNickname,
                        score = userScore.toString(),
                        status = if (selectedAnswer != null) "Answered" else "Thinking...",
                        icon = "🎮",
                        borderColor = if (selectedAnswer != null) Color(0xFF4ADE80) else Color(0xFFEAB308),
                        modifier = Modifier.weight(1f)
                    )
                    if (!isSingleplayer) {
                        val oppAnswered = if (isPlayer1) player2Answered else player1Answered
                        ScoreCard(
                            name = opponentName,
                            score = opponentScore.toString(),
                            status = if (oppAnswered) "Answered" else "Thinking...",
                            icon = "🎯",
                            borderColor = if (oppAnswered) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                if (!isSingleplayer && !isGameStarted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFBBF24).copy(0.15f))
                        .border(2.dp, Color(0xFFFBBF24).copy(0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp)) {
                        Text("🛸 Waiting for players to be ready...", fontFamily = Fredoka,
                            color = Color(0xFFFBBF24), textAlign = TextAlign.Center, fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Question Info
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Q ${currentQuestionIndex + 1}/${questions.size}", fontFamily = FredokaOne,
                        color = Color.White, fontSize = 14.sp)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF7C3AED).copy(0.5f))
                    .border(2.dp, Color(0xFFFBBF24).copy(0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("🔬 ${category.replaceFirstChar { it.uppercase() }}", fontFamily = Fredoka,
                        color = Color(0xFFFBBF24), fontSize = 12.sp)
                }
            }

            // Question Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(0.08f))
                    .border(3.dp, Color(0xFFFBBF24).copy(0.4f), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.text,
                    fontFamily = Fredoka,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Answer Options
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentQuestion.options.forEachIndexed { index, option ->
                    AnswerOption(
                        index = index,
                        text = option,
                        isSelected = selectedAnswer == index,
                        isCorrect = index == currentQuestion.correctAnswerIndex,
                        showResult = showSyncResult,
                        onClick = { onAnswerSelected(index) }
                    )
                }
            }
        }

        // Timer Circle (bottom right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp)
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            val sweepAngle = 360f * (timeLeft / 15f)
            val circleColor = if (timeLeft <= 5) Color.Red else Color.White

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawArc(
                    color = circleColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = timeLeft.toString(),
                color = circleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Removed redundant LaunchedEffect
}

@Composable
fun ScoreCard(
    name: String,
    score: String,
    status: String,
    icon: String,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().offset(3.dp, 4.dp).height(84.dp)
            .clip(RoundedCornerShape(22.dp)).background(Color(0xFF1E1B4B).copy(0.6f)))
        Box(modifier = Modifier.fillMaxWidth().height(84.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(0.1f))
            .border(3.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(borderColor.copy(0.25f)).border(2.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontFamily = Fredoka, color = Color.White, fontSize = 12.sp)
                    Text(status, fontFamily = Fredoka, color = borderColor, fontSize = 10.sp)
                }
                Text(score, fontFamily = FredokaOne, color = Color.White, fontSize = 26.sp)
            }
        }
    }
}

@Composable
fun AnswerOption(
    index: Int,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = when {
        showResult && isCorrect -> Brush.horizontalGradient(listOf(Color(0xFF22C55E), Color(0xFF10B981)))
        showResult && isSelected && !isCorrect -> Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFF97316)))
        isSelected -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899)))
        else -> Brush.horizontalGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.05f)))
    }
    val borderColor = when {
        showResult && isCorrect -> Color(0xFF4ADE80)
        showResult && isSelected && !isCorrect -> Color(0xFFF87171)
        isSelected -> Color(0xFFFBBF24)
        else -> Color.White.copy(0.2f)
    }
    val label = ('A' + index).toString()
    val labelBg = when {
        showResult && isCorrect -> Color(0xFF4ADE80)
        showResult && isSelected && !isCorrect -> Color(0xFFF87171)
        isSelected -> Color(0xFFFBBF24)
        else -> Color.White.copy(0.2f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(
                if (isSelected || (showResult && (isCorrect || isSelected))) {
                    Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF1E1B4B).copy(0.4f))
                } else Modifier
            )
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(3.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(enabled = !showResult) { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(labelBg)
                .border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center) {
                Text(label, fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(text, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            if (showResult) {
                Text(if (isCorrect) "✅" else if (isSelected) "❌" else "", fontSize = 22.sp)
            }
        }
    }
}
