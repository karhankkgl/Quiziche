package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*

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
    onNavigateToGame: () -> Unit
) {
    val yourTurnGames = listOf(
        OngoingGameInfo(1, "🎯", "Alex", "Science", "3-2", "2h ago", true),
        OngoingGameInfo(2, "🎪", "John", "Sports", "2-0", "5h ago", true),
        OngoingGameInfo(3, "🎨", "Sarah", "Art", "4-4", "1d ago", true)
    )

    val waitingGames = listOf(
        OngoingGameInfo(4, "🎮", "Maria", "History", "1-1", "30m ago", false),
        OngoingGameInfo(5, "🎭", "Oliver", "Music", "0-1", "1h ago", false),
        OngoingGameInfo(6, "🎬", "Emma", "Movies", "2-3", "3h ago", false)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2FF),
                        Color(0xFFF5F3FF),
                        Color(0xFFFDF2F8)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937)
                    )
                }
                Text(
                    text = "Ongoing Games",
                    color = Color(0xFF1F2937),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Your Turn Section
            GamesSectionHeader(
                title = "Your Turn",
                count = yourTurnGames.size,
                subtitle = "Make your move now!",
                color = Color(0xFF22C55E)
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                yourTurnGames.forEach { game ->
                    OngoingGameItem(game, onNavigateToGame)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Waiting Section
            GamesSectionHeader(
                title = "Opponent's Turn",
                count = waitingGames.size,
                subtitle = "Waiting for opponent...",
                color = Color(0xFF9CA3AF)
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                waitingGames.forEach { game ->
                    OngoingGameItem(game, onNavigateToGame)
                }
            }
        }
    }
}

@Composable
fun GamesSectionHeader(title: String, count: Int, subtitle: String, color: Color) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(text = subtitle, color = Color(0xFF6B7280), fontSize = 14.sp)
    }
}

@Composable
fun OngoingGameItem(game: OngoingGameInfo, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (game.isYourTurn) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = if (game.isYourTurn) Color.Transparent else Color.White,
        border = if (game.isYourTurn) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = if (game.isYourTurn) 4.dp else 1.dp
    ) {
        val backgroundModifier = if (game.isYourTurn)
            Modifier.background(
                Brush.horizontalGradient(listOf(Color(0xFF4ADE80), Color(0xFF10B981)))
            )
        else Modifier

        Column(modifier = backgroundModifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (game.isYourTurn) Color.White.copy(alpha = 0.3f)
                            else Color(0xFFF3F4F6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = game.opponentIcon, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "vs ${game.name}",
                        color = if (game.isYourTurn) Color.White else Color(0xFF1F2937),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.category,
                        color = if (game.isYourTurn) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = game.score,
                        color = if (game.isYourTurn) Color.White else Color(0xFF1F2937),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.time,
                        color = if (game.isYourTurn) Color.White.copy(alpha = 0.8f) else Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }

            if (game.isYourTurn) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play Now",
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for ${game.name}...",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
