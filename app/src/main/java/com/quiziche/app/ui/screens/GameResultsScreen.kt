package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.quiziche.app.ui.theme.*

@Composable
fun GameResultsScreen(
    score: Int,
    totalQuestions: Int,
    isSingleplayer: Boolean,
    isWinner: Boolean,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToRematch: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "results")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyScale"
    )

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
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy Animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (isWinner) "🏆" else "😢",
                    fontSize = 80.sp,
                    modifier = Modifier.scale(trophyScale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isWinner) "Victory!" else "So Close!",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isWinner) "You won the quiz duel" else "Better luck next time",
                    color = QuizichePurple200,
                    fontSize = 18.sp
                )
            }

            // Score Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                ResultCard(
                    name = "You",
                    score = "$score",
                    icon = "🎮",
                    isWinner = isWinner,
                    label = if (isSingleplayer) "SOLO" else if (isWinner) "WINNER" else "2ND PLACE"
                )
                if (!isSingleplayer) {
                    ResultCard(
                        name = "Alex",
                        score = "${totalQuestions - score}",
                        icon = "🎯",
                        isWinner = !isWinner,
                        label = if (!isWinner) "WINNER" else "2ND PLACE"
                    )
                }
            }

            // Match Statistics
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Match Statistics",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val accuracy = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
                        ResultStatItem("$totalQuestions", "Questions", Color.White)
                        ResultStatItem("$score", "Correct", Color(0xFF4ADE80))
                        ResultStatItem("$accuracy%", "Accuracy", Color(0xFFEAB308))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Coins Earned",
                            color = QuizichePurple200,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "+${score * 5} 🪙",
                            color = Color(0xFFEAB308),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateToRematch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = QuizichePurple700
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Rematch",
                            color = QuizichePurple700,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Return to Main Menu", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    name: String,
    score: String,
    icon: String,
    isWinner: Boolean,
    label: String
) {
    val bgColor = if (isWinner) Brush.horizontalGradient(listOf(Color(0xFFFACC15), Color(0xFFF97316)))
    else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .then(
                if (!isWinner) Modifier.border(
                    1.dp,
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(32.dp)
                ) else Modifier
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = if (isWinner) 0.3f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = if (isWinner) Color(0xFF78350F) else QuizichePurple200,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isWinner) "Science Master" else "Well played!",
                    color = if (isWinner) Color(0xFF78350F) else QuizichePurple200,
                    fontSize = 12.sp
                )
            }

            Text(
                text = score,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isWinner) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            )
        }
    }
}

@Composable
fun ResultStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = QuizichePurple200, fontSize = 12.sp)
    }
}
