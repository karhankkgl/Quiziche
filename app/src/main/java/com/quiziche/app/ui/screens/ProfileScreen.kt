package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.data.model.User

data class MatchResult(
    val id: Int,
    val opponentIcon: String,
    val opponentName: String,
    val result: String,
    val score: String,
    val category: String
)

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }
    var userProfile by remember { mutableStateOf<User?>(null) }
    
    var matchHistory by remember { mutableStateOf<List<MatchResult>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        authRepository.currentUserUID?.let { uid ->
            val result = userRepository.getUserProfile(uid)
            if (result.isSuccess) {
                userProfile = result.getOrNull()
            }
            
            val historyResult = userRepository.getMatchHistory(uid)
            if (historyResult.isSuccess) {
                val matches = historyResult.getOrDefault(emptyList()).mapIndexed { index, map ->
                    MatchResult(
                        id = index,
                        opponentIcon = map["opponentIcon"] as? String ?: "🤖",
                        opponentName = map["opponentName"] as? String ?: "Unknown",
                        result = map["result"] as? String ?: "lost",
                        score = map["score"] as? String ?: "0-0",
                        category = map["category"] as? String ?: "General"
                    )
                }
                matchHistory = matches
            }
        }
    }

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
                    text = "Profile",
                    color = Color(0xFF1F2937),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Profile Picture & Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎮", fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userProfile?.name ?: "QuizMaster",
                    color = Color(0xFF1F2937),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Level ${userProfile?.level ?: 1} • XP: ${userProfile?.xp ?: 0}",
                        color = Color(0xFF9333EA),
                        fontSize = 14.sp
                    )
                }
            }

            // Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    icon = Icons.Default.EmojiEvents,
                    value = userProfile?.totalGames?.toString() ?: "0",
                    label = "Total Games",
                    color = Color(0xFFEAB308),
                    modifier = Modifier.weight(1f)
                )
                val winRate = if ((userProfile?.totalGames ?: 0) > 0) {
                    ((userProfile?.winCount?.toFloat() ?: 0f) / (userProfile?.totalGames?.toFloat() ?: 1f) * 100).toInt()
                } else 0
                ProfileStatCard(
                    emoji = "🏆",
                    value = "$winRate%",
                    label = "Win Rate",
                    color = Color(0xFF22C55E),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    emoji = "🔬",
                    value = userProfile?.topCategory ?: "None",
                    label = "Top Category",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }

            // Match History Section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Past Matches",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(text = "View All >", color = Color(0xFF9333EA), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                matchHistory.forEach { match ->
                    MatchHistoryCard(match)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(
    icon: ImageVector? = null,
    emoji: String? = null,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            } else if (emoji != null) {
                Text(text = emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color(0xFF1F2937),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color(0xFF6B7280),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MatchHistoryCard(match: MatchResult) {
    val isWin = match.result == "won"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isWin) Color(0xFF22C55E) else Color(0xFFEF4444))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Opponent Info
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = match.opponentIcon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "vs ${match.opponentName}",
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = match.category, color = Color(0xFF6B7280), fontSize = 12.sp)
            }

            // Score & Result
            Column(horizontalAlignment = Alignment.End) {
                val textColor = if (isWin) Color(0xFF16A34A) else Color(0xFFDC2626)
                Text(
                    text = match.score,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isWin) "Victory" else "Defeat",
                    color = textColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
