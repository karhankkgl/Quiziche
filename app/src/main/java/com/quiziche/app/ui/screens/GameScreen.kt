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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.data.model.Question
import com.quiziche.app.data.repository.QuizRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    isSingleplayer: Boolean = false,
    category: String = "all",
    onNavigateToResults: (score: Int, totalQuestions: Int, isWinner: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val quizRepository = remember { QuizRepository() }
    val userRepository = remember { UserRepository() }
    val authRepository = remember { AuthRepository() }

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var userScore by remember { mutableStateOf(0) }
    
    var timeLeft by remember { mutableStateOf(15) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        if (!isSingleplayer) {
            delay(1500) // Small delay to simulate multiplayer setup
        }
        val result = quizRepository.getQuestionsByCategory(category, 5)
        if (result.isSuccess) {
            questions = result.getOrDefault(emptyList()).shuffled().take(5)
        }
        isLoading = false
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    LaunchedEffect(timeLeft, isLoading) {
        if (!isLoading && currentQuestion != null) {
            if (timeLeft > 0 && selectedAnswer == null) {
                delay(1000)
                timeLeft -= 1
            } else if (timeLeft == 0 && selectedAnswer == null) {
                delay(1500)
                // Go to next question or results
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    selectedAnswer = null
                    timeLeft = 15
                } else {
                    scope.launch {
                        authRepository.currentUserUID?.let { uid ->
                            val won = userScore > 2
                            val displayCategory = category.replaceFirstChar { it.uppercase() }
                            userRepository.updateStats(uid, won, displayCategory, xpEarned = userScore * 10, coinsEarned = userScore * 5, isSingleplayer = isSingleplayer)
                            val matchData = mapOf(
                                "opponentIcon" to "🤖",
                                "opponentName" to if (isSingleplayer) "Solo Bot" else "Player 2",
                                "result" to if (won) "won" else "lost",
                                "score" to if (isSingleplayer) "$userScore" else "$userScore - ${5 - userScore}",
                                "category" to displayCategory,
                                "timestamp" to System.currentTimeMillis()
                            )
                            userRepository.saveMatchResult(uid, matchData)
                        }
                        onNavigateToResults(userScore, questions.size, userScore > 2)
                    }
                }
            }
        }
    }

    // Auto-navigate after selection
    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null) {
            if (selectedAnswer == currentQuestion?.correctAnswerIndex) {
                userScore++
            }
            delay(1500)
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedAnswer = null
                timeLeft = 15
            } else {
                scope.launch {
                    authRepository.currentUserUID?.let { uid ->
                        val won = userScore > 2
                        val displayCategory = category.replaceFirstChar { it.uppercase() }
                        userRepository.updateStats(uid, won, displayCategory, xpEarned = userScore * 10, coinsEarned = userScore * 5, isSingleplayer = isSingleplayer)
                        val matchData = mapOf(
                            "opponentIcon" to "🤖",
                            "opponentName" to if (isSingleplayer) "Solo Bot" else "Player 2",
                            "result" to if (won) "won" else "lost",
                            "score" to if (isSingleplayer) "$userScore" else "$userScore - ${5 - userScore}",
                            "category" to displayCategory,
                            "timestamp" to System.currentTimeMillis()
                        )
                        userRepository.saveMatchResult(uid, matchData)
                    }
                    onNavigateToResults(userScore, questions.size, userScore > 2)
                }
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = QuizichePurple700)
        }
        return
    }

    if (questions.isEmpty() || currentQuestion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No questions found for this category.", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigateToResults(0, 0, false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Return", color = QuizichePurple700)
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        QuizichePurple600,
                        QuizichePurple700,
                        QuizicheIndigo800
                    )
                )
            )
    ) {
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreCard(
                        name = "You",
                        score = userScore.toString(),
                        status = "Your Turn",
                        icon = "🎮",
                        borderColor = Color(0xFFEAB308),
                        modifier = Modifier.weight(1f)
                    )
                    if (!isSingleplayer) {
                        ScoreCard(
                            name = "Alex",
                            score = "0",
                            status = "Waiting",
                            icon = "🎯",
                            borderColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Question Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Question ${currentQuestionIndex + 1}/${questions.size}", color = QuizichePurple200, fontSize = 14.sp)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF3B82F6).copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🔬 ${category.replaceFirstChar { it.uppercase() }}",
                        color = Color(0xFFDBEAFE),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Question Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.text,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
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
                        showResult = selectedAnswer != null,
                        onClick = {
                            if (selectedAnswer == null) {
                                selectedAnswer = index
                            }
                        }
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = name, color = Color.White, fontSize = 12.sp)
                    Text(text = status, color = borderColor, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = score,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
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
    val bgColor = when {
        showResult && isCorrect -> Color(0xFF22C55E).copy(alpha = 0.5f)
        showResult && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.5f)
        isSelected -> Color.White.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val borderColor = when {
        showResult && isCorrect -> Color(0xFF4ADE80)
        showResult && isSelected && !isCorrect -> Color(0xFFF87171)
        isSelected -> Color.White.copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.2f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !showResult) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labelBg = when {
                showResult && isCorrect -> Color(0xFF4ADE80)
                showResult && isSelected && !isCorrect -> Color(0xFFF87171)
                else -> Color.White.copy(alpha = 0.2f)
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(labelBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + index).toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )

            if (showResult) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
