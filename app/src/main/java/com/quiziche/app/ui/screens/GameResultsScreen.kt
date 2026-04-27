package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay

@Composable
fun GameResultsScreen(
    score: Int,
    userNickname: String = "You",
    opponentScore: Int = 0,
    opponentName: String = "Opponent",
    totalQuestions: Int,
    category: String = "General",
    isSingleplayer: Boolean,
    isWinner: Boolean,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToRematch: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "results_anim")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "t"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "s"
    )
    val confettiY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "c"
    )

    val bgBrush = if (isWinner)
        Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF0D9488)))
    else
        Brush.verticalGradient(listOf(Color(0xFF1A0A2E), Color(0xFF2D1B69), Color(0xFF4C1D95)))

    Box(
        modifier = Modifier.fillMaxSize().background(bgBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(top = 60.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big emoji
            Text(
                text = if (isWinner) "🏆" else "😢",
                fontSize = 100.sp,
                modifier = Modifier.scale(trophyScale)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Win/loss badge
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (isWinner) Color(0xFFFBBF24) else Color.White.copy(0.15f))
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isWinner) "🎉  VICTORY!" else "😢  SO CLOSE!",
                    fontFamily = FredokaOne, fontSize = 26.sp,
                    color = if (isWinner) Color(0xFF1E1B4B) else Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isWinner) "You crushed it! 🔥" else "Better luck next time!",
                fontFamily = Fredoka, fontSize = 16.sp, color = Color.White.copy(0.75f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Score cards
            Box(modifier = Modifier.fillMaxWidth()) {
                // Shadow
                Box(modifier = Modifier.fillMaxWidth().offset(4.dp, 5.dp).clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E1B4B)))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isWinner) Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
                            else Brush.horizontalGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.08f))))
                        .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(60.dp).clip(CircleShape)
                            .background(Color.White.copy(if (isWinner) 0.3f else 0.15f))
                            .border(2.dp, Color(0xFF1E1B4B), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Text("🎮", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isWinner) "WINNER" else "PLAYER", fontFamily = Fredoka,
                                color = if (isWinner) Color(0xFF78350F) else Color.White.copy(0.6f), fontSize = 11.sp)
                            Text(userNickname, fontFamily = FredokaOne,
                                color = if (isWinner) Color(0xFF1E1B4B) else Color.White, fontSize = 18.sp)
                        }
                        Text("$score", fontFamily = FredokaOne, fontSize = 44.sp,
                            color = if (isWinner) Color(0xFF1E1B4B) else Color.White)
                    }
                }
            }

            if (!isSingleplayer) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().offset(4.dp, 5.dp).clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1E1B4B)))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (!isWinner) Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
                                else Brush.horizontalGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.08f))))
                            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(28.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(60.dp).clip(CircleShape)
                                .background(Color.White.copy(if (!isWinner) 0.3f else 0.15f))
                                .border(2.dp, Color(0xFF1E1B4B), CircleShape),
                                contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (!isWinner) "WINNER" else "2ND PLACE", fontFamily = Fredoka,
                                    color = if (!isWinner) Color(0xFF78350F) else Color.White.copy(0.6f), fontSize = 11.sp)
                                Text(opponentName, fontFamily = FredokaOne,
                                    color = if (!isWinner) Color(0xFF1E1B4B) else Color.White, fontSize = 18.sp)
                            }
                            Text("$opponentScore", fontFamily = FredokaOne, fontSize = 44.sp,
                                color = if (!isWinner) Color(0xFF1E1B4B) else Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats card
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().offset(4.dp, 5.dp).clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E1B4B)))
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.1f))
                    .border(3.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp)).padding(20.dp)
                ) {
                    Column {
                        Text("📊  Match Stats", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            val accuracy = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
                            ResultStatChip("$totalQuestions", "Questions")
                            ResultStatChip("$score", "Correct", Color(0xFF86EFAC))
                            ResultStatChip("$accuracy%", "Accuracy", Color(0xFFFBBF24))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(0.1f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🪙  Coins Earned", fontFamily = Fredoka, color = Color.White.copy(0.7f), fontSize = 14.sp)
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBBF24)).border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text("+${score * 5} 🪙", fontFamily = FredokaOne, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            CartoonButton(
                text = "⚡  Play Again!",
                onClick = onNavigateToRematch,
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                textColor = Color(0xFF1E1B4B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().offset(4.dp, 5.dp).height(52.dp)
                    .clip(RoundedCornerShape(18.dp)).background(Color(0xFF1E1B4B).copy(0.5f)))
                Box(modifier = Modifier.fillMaxWidth().height(52.dp)
                    .clip(RoundedCornerShape(18.dp)).background(Color.White.copy(0.12f))
                    .border(2.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp))
                    .clickable { onNavigateToMainMenu() },
                    contentAlignment = Alignment.Center) {
                    Text("🏠  Main Menu", fontFamily = FredokaOne, fontSize = 16.sp, color = Color.White)
                }
            }
        }

        // Decorations (on top)
        if (isWinner) {
            listOf("🎉","⭐","🌟","✨","🎊","💫").forEachIndexed { i, emoji ->
                Text(emoji, fontSize = (18 + i * 4).sp,
                    modifier = Modifier.offset((30 + i * 55).dp, (20 + (i % 3) * 60).dp)
                        .offset(y = confettiY.dp).rotate(starRotate * (if (i % 2 == 0) 1f else -1f))
                        .scale(0.7f + (i % 3) * 0.2f))
            }
        } else {
            listOf("😢","💔","🌧️","⭐").forEachIndexed { i, emoji ->
                Text(emoji, fontSize = (18 + i * 4).sp,
                    modifier = Modifier.offset((40 + i * 80).dp, (20 + i * 50).dp).scale(0.7f),
                    color = Color.White.copy(0.3f))
            }
        }
    }
}

@Composable
fun ResultStatChip(value: String, label: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = FredokaOne, fontSize = 28.sp, color = valueColor)
        Text(label, fontFamily = Fredoka, fontSize = 12.sp, color = Color.White.copy(0.6f))
    }
}

// Keep for legacy
@Composable
fun ResultCard(
    name: String, score: String, icon: String, isWinner: Boolean, label: String
) {}

@Composable
fun ResultStatItem(value: String, label: String, color: Color) {
    ResultStatChip(value, label, color)
}
