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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.model.GameSession
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

data class OngoingGameInfo(
    val id: Int,
    val opponentIcon: String,
    val name: String,
    val category: String,
    val score: String,
    val time: String,
    val isYourTurn: Boolean
)

@Composable
fun OngoingGamesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGame: (String) -> Unit
) {
    val gameRepository = remember { GameRepository() }
    var activeGames by remember { mutableStateOf<List<GameSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val uid = gameRepository.currentUid

    LaunchedEffect(Unit) {
        gameRepository.getActiveGames().collectLatest { games: List<GameSession> ->
            activeGames = games
            isLoading = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ongoing_anim")
    val lightningScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "l"
    )
    val swordRotate by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "s"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color.Black.copy(0.8f))
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFF97316))))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Color.White.copy(0.2f)).border(2.dp, Color.White.copy(0.4f), CircleShape)
                            .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center) {
                            Text("←", fontSize = 20.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("⚡ Active Battles", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your ongoing matches", fontFamily = Fredoka, fontSize = 15.sp, color = Color.White.copy(0.8f))
                }
            }
            
            OngoingDecorations(lightningScale, swordRotate)

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
                }
            } else if (activeGames.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.White.copy(0.15f).copy(0.25f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡", fontSize = 60.sp, modifier = Modifier.scale(lightningScale))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Active Battles!", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                            Text("Start a game to see it here", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                activeGames.forEach { session ->
                    val isPlayer1 = session.player1Id == uid
                    val scoreText = "${session.player1Score} - ${session.player2Score}"
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        CartoonOngoingGameRow(
                            info = OngoingGameInfo(
                                id = session.sessionId.hashCode(),
                                opponentIcon = "🎯",
                                name = "Opponent",
                                category = session.category,
                                score = scoreText,
                                time = "🔴 Live",
                                isYourTurn = true
                            ),
                            onClick = { onNavigateToGame(session.sessionId) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CartoonOngoingGameRow(info: OngoingGameInfo, onClick: () -> Unit) {
    val brush = if (info.isYourTurn)
        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9)))
    else
        Brush.horizontalGradient(listOf(Color.White, Color(0xFFF9FAFB)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.White.copy(0.15f).copy(0.4f))
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(if (info.isYourTurn) 0.25f else 0.8f))
                        .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center) {
                        Text(info.opponentIcon, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("vs ${info.name}", fontFamily = FredokaOne,
                            color = if (info.isYourTurn) Color.White else Color.White.copy(0.9f), fontSize = 17.sp)
                        Text(info.category, fontFamily = Fredoka,
                            color = if (info.isYourTurn) Color.White.copy(0.8f) else Color.White.copy(0.6f), fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(info.score, fontFamily = FredokaOne,
                            color = if (info.isYourTurn) Color.White else Color.White.copy(0.9f), fontSize = 20.sp)
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(0.8f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(info.time, fontFamily = Fredoka, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (info.isYourTurn) Color.White else Color(0xFFEDE9FE))
                    .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(14.dp))
                    .clickable { onClick() }
                    .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text("⚡  Play Now!", fontFamily = FredokaOne,
                        color = if (info.isYourTurn) Color(0xFF10B981) else Color(0xFF7C3AED), fontSize = 15.sp)
        }
    }
}
}

// Decorations (on top)
@Composable
fun OngoingDecorations(lightningScale: Float, swordRotate: Float) {
    Text("⚡", fontSize = 56.sp, modifier = Modifier.offset(290.dp, 80.dp).scale(lightningScale), color = Color(0xFFFBBF24))
    Text("⚔️", fontSize = 36.sp, modifier = Modifier.offset(20.dp, 140.dp).rotate(swordRotate))
    Text("🎮", fontSize = 28.sp, modifier = Modifier.offset(310.dp, 220.dp))
}

@Composable
fun GamesSectionHeader(title: String, count: Int, subtitle: String, color: Color) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.clip(CircleShape).background(color).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text("$count", fontFamily = FredokaOne, color = Color.White, fontSize = 13.sp)
            }
        }
        Text(subtitle, fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 13.sp)
    }
}

@Composable
fun OngoingGameItem(game: OngoingGameInfo, onClick: () -> Unit) = CartoonOngoingGameRow(game, onClick)
