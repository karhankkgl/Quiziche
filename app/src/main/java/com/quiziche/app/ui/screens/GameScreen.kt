package com.quiziche.app.ui.screens

import androidx.activity.compose.BackHandler
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
    difficulty: String = "Any",
    roomId: String? = null,
    onNavigateToResults: (score: Int, userNickname: String, opponentScore: Int, opponentName: String, totalQuestions: Int, category: String, isWinner: Boolean, correctAnswers: Int) -> Unit
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
    var correctAnswers by remember { mutableStateOf(0) }
    
    var opponentScore by remember { mutableStateOf(0) }
    var userNickname by remember { mutableStateOf("You") }
    var opponentName by remember { mutableStateOf(if (isSingleplayer) "Solo" else "Opponent") }
    var isPlayer1 by remember { mutableStateOf(true) }

    var isGameStarted by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var preGameCountdown by remember { mutableStateOf(3) }
    
    var player1Ready by remember { mutableStateOf(false) }
    var player2Ready by remember { mutableStateOf(false) }
    var player1Answered by remember { mutableStateOf(false) }
    var player2Answered by remember { mutableStateOf(false) }
    var showSyncResult by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(1f) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var connectionTimeout by remember { mutableStateOf(15) }

    LaunchedEffect(isGameStarted, isCountingDown, player1Ready, player2Ready) {
        if (!isSingleplayer && roomId != null && !isGameStarted && !isCountingDown) {
            while (connectionTimeout > 0 && !(player1Ready && player2Ready)) {
                kotlinx.coroutines.delay(1000)
                connectionTimeout--
            }
            if (connectionTimeout == 0 && !(player1Ready && player2Ready)) {
                // Opponent failed to connect (Ghost match)
                gameRepository.completeGame(roomId)
                onNavigateToResults(userScore, userNickname, 0, "Ghost", questions.size, category, true, correctAnswers)
            }
        }
    }

    fun finalizeGame() {
        scope.launch {
            authRepository.currentUserUID?.let { uid ->
                val won = if (isSingleplayer) userScore > 2 else userScore > opponentScore
                val displayCategory = category.replaceFirstChar { it.uppercase() }
                
                userRepository.updateStats(uid, won, displayCategory, xpEarned = userScore * 10, coinsEarned = userScore * 5, isSingleplayer = isSingleplayer)
                
                val matchData = mapOf(
                    "opponentIcon" to if (isSingleplayer) "🦓" else "🎯",
                    "opponentName" to opponentName,
                    "result" to if (won) "won" else "lost",
                    "score" to if (isSingleplayer) "$correctAnswers/${questions.size}" else "$userScore - $opponentScore",
                    "category" to displayCategory,
                    "timestamp" to System.currentTimeMillis()
                )
                userRepository.saveMatchResult(uid, matchData)

                if (!isSingleplayer && roomId != null) {
                    gameRepository.completeGame(roomId)
                }
                
                onNavigateToResults(userScore, userNickname, opponentScore, opponentName, questions.size, category, won, correctAnswers)
            }
        }
    }

    fun triggerNextQuestion() {
        if (isSingleplayer) {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedAnswer = null
                showSyncResult = false
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

    fun handleSurrender() {
        scope.launch {
            authRepository.currentUserUID?.let { uid ->
                // Penalty for the one who leaves
                userRepository.updateStats(uid, won = false, category = category, xpEarned = 0, coinsEarned = 0, isSingleplayer = isSingleplayer)
                if (!isSingleplayer && roomId != null) {
                    gameRepository.completeGame(roomId)
                }
                onNavigateToResults(userScore, userNickname, 0, opponentName, questions.size, category, false, correctAnswers)
            }
        }
    }

    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(roomId) {
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
                    
                    if (!isGameStarted && !isCountingDown && player1Ready && player2Ready) {
                        isCountingDown = true
                    }

                    if (currentQuestionIndex != session.currentQuestionIndex) {
                        currentQuestionIndex = session.currentQuestionIndex
                        selectedAnswer = null
                        showSyncResult = false
                    }

                    if (isPlayer1 && isGameStarted && session.player1Answered && session.player2Answered && !showSyncResult) {
                        scope.launch {
                            showSyncResult = true
                            delay(2500)
                            triggerNextQuestion()
                        }
                    } else if (!isPlayer1 && isGameStarted && session.player1Answered && session.player2Answered) {
                        showSyncResult = true
                    }

                    if (session.status == "ABANDONED" && !isLoading) {
                        onNavigateToResults(userScore, userNickname, opponentScore, opponentName, questions.size, category, true, correctAnswers)
                        return@collectLatest
                    }

                    if (session.status == "COMPLETED" && !isLoading) {
                        // Game was ended by surrender or error
                        onNavigateToResults(userScore, userNickname, opponentScore, opponentName, questions.size, category, userScore >= opponentScore, correctAnswers)
                        return@collectLatest
                    }

                    if (questions.isEmpty() && session.questionIds.isNotEmpty()) {
                        val result = quizRepository.getQuestionsByIds(session.questionIds)
                        if (result.isSuccess) {
                            questions = result.getOrDefault(emptyList())
                        }
                        isLoading = false
                        gameRepository.setReady(roomId, isPlayer1, true)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isSingleplayer || roomId == null) {
            var result = quizRepository.getQuestionsByCategory(category, 30, difficulty)
            var loaded = result.getOrDefault(emptyList()).shuffled().take(5)
            if (loaded.isEmpty() && difficulty != "Any") {
                // Fallback 1: relax difficulty filter
                result = quizRepository.getQuestionsByCategory(category, 30, "Any")
                loaded = result.getOrDefault(emptyList()).shuffled().take(5)
            }
            if (loaded.isEmpty()) {
                // Fallback 2: all categories
                result = quizRepository.getQuestionsByCategory("all", 30, "Any")
                loaded = result.getOrDefault(emptyList()).shuffled().take(5)
            }
            questions = loaded
            isLoading = false
            isCountingDown = true
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            while (preGameCountdown > 0) {
                delay(1000)
                preGameCountdown--
            }
            isGameStarted = true
            isCountingDown = false
            if (!isSingleplayer && roomId != null && isPlayer1 && questions.isNotEmpty()) {
                gameRepository.syncNextQuestion(roomId, 0)
            }
        }
    }

    LaunchedEffect(currentQuestionIndex, isGameStarted) {
        if (isGameStarted && currentQuestion != null) {
            animatedProgress.snapTo(1f)
            val result = animatedProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
            )
            if (result.endReason == AnimationEndReason.Finished && selectedAnswer == null) {
                if (isSingleplayer) {
                    delay(1500)
                    triggerNextQuestion()
                } else if (roomId != null) {
                    gameRepository.submitAnswer(roomId, isPlayer1, userScore)
                }
            }
        }
    }

    fun onAnswerSelected(index: Int) {
        if (selectedAnswer == null) {
            selectedAnswer = index
            val isCorrect = index == currentQuestion?.correctAnswerIndex
            if (isCorrect) {
                correctAnswers++
                val timeLeftInt = (animatedProgress.value * 15).toInt()
                val points = 10 + (timeLeftInt * 1) 
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

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("🏳️ Surrender?", fontFamily = FredokaOne, color = Color.White) },
            text = { Text("If you exit now, you will lose the match and your ELO will drop! Are you sure?", fontFamily = Fredoka, color = Color.White.copy(0.8f)) },
            confirmButton = {
                TextButton(onClick = { handleSurrender() }) { Text("Yes, Exit", color = Color(0xFFEF4444), fontFamily = FredokaOne) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Stay", color = Color.White, fontFamily = FredokaOne) }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF1E293B)
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E), Color(0xFF2D1B69)))), contentAlignment = Alignment.Center) {
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
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E)))), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("😢", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("No questions found!", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                CartoonButton(text = "🏠  Return", onClick = { onNavigateToResults(0, userNickname, 0, "Opponent", 0, category, false, 0) }, bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))), textColor = Color(0xFF475569))
            }
        }
        return
    }

    if (isCountingDown && !isGameStarted) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E)))), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Get Ready!", fontFamily = FredokaOne, color = Color.White, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("$preGameCountdown", fontFamily = FredokaOne, color = Color(0xFFFBBF24), fontSize = 80.sp)
            }
        }
        return
    } else if (!isGameStarted && !isSingleplayer && !isCountingDown) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E)))), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFBBF24))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Waiting for opponent...", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 18.sp)
            }
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0D0020), Color(0xFF1A0A2E), Color(0xFF2D1B69))))
    ) {
        // Star decorations
        Text("⭐", fontSize = 16.sp, modifier = Modifier.offset(20.dp, 50.dp), color = Color.White.copy(0.2f))
        Text("✨", fontSize = 12.sp, modifier = Modifier.offset(310.dp, 80.dp), color = Color.White.copy(0.15f))
        Text("🌟", fontSize = 14.sp, modifier = Modifier.offset(280.dp, 160.dp), color = Color.White.copy(0.15f))
        Text("⭐", fontSize = 10.sp, modifier = Modifier.offset(50.dp, 200.dp), color = Color.White.copy(0.2f))

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 32.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.Center) {
                // Logo in center
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.quiziche.app.R.drawable.quiziche_minimal),
                    contentDescription = "Logo",
                    modifier = Modifier.height(30.dp).align(Alignment.Center)
                )
                
                // Close button on the right
                Box(modifier = Modifier.size(36.dp).align(Alignment.CenterEnd).clip(CircleShape).background(Color.Red.copy(0.2f)).border(1.5.dp, Color.Red.copy(0.4f), CircleShape).clickable { showExitDialog = true }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
            
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                val progressValue = animatedProgress.value
                val timeLeftInt = (progressValue * 15).toInt()
                val progressColor by animateColorAsState(if (timeLeftInt <= 5) Color.Red else Color(0xFF4ADE80), label = "color")
                
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth(progressValue),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)).background(progressColor))
                        Text("🔥", fontSize = 14.sp, modifier = Modifier.offset(x = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoreCard(name = userNickname, score = userScore.toString(), status = if (selectedAnswer != null) "Answered" else "Thinking...", icon = "🎮", borderColor = if (selectedAnswer != null) Color(0xFF4ADE80) else Color(0xFFEAB308), modifier = Modifier.weight(1f))
                    if (!isSingleplayer) {
                        val oppAnswered = if (isPlayer1) player2Answered else player1Answered
                        ScoreCard(name = opponentName, score = opponentScore.toString(), status = if (oppAnswered) "Answered" else "Thinking...", icon = "🎯", borderColor = if (oppAnswered) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.15f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Q ${currentQuestionIndex + 1}/${questions.size}", fontFamily = FredokaOne, color = Color.White, fontSize = 14.sp)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF7C3AED).copy(0.5f)).border(2.dp, Color(0xFFFBBF24).copy(0.5f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("🔬 ${category.replaceFirstChar { it.uppercase() }}", fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 12.sp)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 120.dp).clip(RoundedCornerShape(28.dp)).background(Color.White.copy(0.08f)).border(3.dp, if (currentQuestion.isAiGenerated) Color(0xFFA78BFA).copy(0.7f) else Color(0xFFFBBF24).copy(0.4f), RoundedCornerShape(28.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // AI Badge
                    if (currentQuestion.isAiGenerated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
                                    )
                                )
                                .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "✨ AI Generated",
                                fontFamily = FredokaOne,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    val questionText = androidx.compose.ui.text.buildAnnotatedString {
                        append(currentQuestion.text)
                        if (currentQuestion.isAiGenerated) {
                            pushStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    color = Color(0xFFF472B6), // Pink accent
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            append(" 🤖 AI")
                            pop()
                        }
                    }
                    Text(text = questionText, fontFamily = Fredoka, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 28.sp)
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentQuestion.options.forEachIndexed { index, option ->
                    AnswerOption(index = index, text = option, isSelected = selectedAnswer == index, isCorrect = index == currentQuestion.correctAnswerIndex, showResult = showSyncResult, onClick = { onAnswerSelected(index) })
                }
            }
        }
    }
}

@Composable
fun ScoreCard(name: String, score: String, status: String, icon: String, borderColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().offset(3.dp, 4.dp).height(84.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFF475569).copy(0.6f)))
        Box(modifier = Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(0.1f)).border(3.dp, borderColor, RoundedCornerShape(22.dp)).padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(borderColor.copy(0.25f)).border(2.dp, borderColor, CircleShape), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontFamily = Fredoka, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(status, fontFamily = Fredoka, color = borderColor, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
                Text(score, fontFamily = FredokaOne, color = Color.White, fontSize = 26.sp)
            }
        }
    }
}

@Composable
fun AnswerOption(index: Int, text: String, isSelected: Boolean, isCorrect: Boolean, showResult: Boolean, onClick: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth().height(64.dp).then(if (isSelected || (showResult && (isCorrect || isSelected))) Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF475569).copy(0.4f)) else Modifier).clip(RoundedCornerShape(22.dp)).background(bgBrush).border(3.dp, borderColor, RoundedCornerShape(22.dp)).clickable(enabled = !showResult) { onClick() }.padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(labelBg).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(label, fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(text, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            if (showResult) {
                Text(if (isCorrect) "✅" else if (isSelected) "❌" else "", fontSize = 22.sp)
            }
        }
    }
}
