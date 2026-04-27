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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.model.User
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.ui.theme.*

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
    onNavigateBack: () -> Unit,
    onLogOut: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }
    var userProfile by remember { mutableStateOf<User?>(null) }
    var matchHistory by remember { mutableStateOf<List<MatchResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        authRepository.currentUserUID?.let { uid ->
            val result = userRepository.getUserProfile(uid)
            if (result.isSuccess) userProfile = result.getOrNull()
            val historyResult = userRepository.getMatchHistory(uid)
            if (historyResult.isSuccess) {
                matchHistory = historyResult.getOrDefault(emptyList()).mapIndexed { index, map ->
                    MatchResult(index, map["opponentIcon"] as? String ?: "🤖",
                        map["opponentName"] as? String ?: "Unknown",
                        map["result"] as? String ?: "lost",
                        map["score"] as? String ?: "0-0",
                        map["category"] as? String ?: "General")
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "profile_anim")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "a"
    )
    val zebraRotate by infiniteTransition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "z"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF5F3FF), Color(0xFFEDE9FE))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)
        ) {
            // ===== HEADER BAR =====
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))))
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Color.White.copy(0.2f)).border(2.dp, Color.White.copy(0.4f), CircleShape)
                            .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center) {
                            Text("←", fontSize = 20.sp, color = Color.White)
                        }
                        Text("My Profile", fontFamily = FredokaOne, fontSize = 22.sp, color = Color.White)
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(0.7f)).border(2.dp, Color.White.copy(0.3f), CircleShape)
                            .clickable { authRepository.logout(); onLogOut() },
                            contentAlignment = Alignment.Center) {
                            Text("⏻", fontSize = 16.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Avatar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                            .border(4.dp, Color.White, CircleShape).scale(avatarScale),
                            contentAlignment = Alignment.Center) {
                            Text(userProfile?.avatarIcon ?: "🎮", fontSize = 44.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(userProfile?.name ?: "QuizMaster", fontFamily = FredokaOne, fontSize = 22.sp, color = Color.White)
                            Text("${userProfile?.getClassName() ?: "Beginner"} • Level ${userProfile?.level ?: 1}",
                                fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFBBF24)).border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("⚡ ${userProfile?.elo ?: 1000} ELO", fontFamily = FredokaOne, fontSize = 12.sp, color = Color(0xFF1E1B4B))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(0.2f)).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text("🪙 ${userProfile?.coins ?: 0}", fontFamily = FredokaOne, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== STATS CARDS =====
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val winRate = if ((userProfile?.totalGames ?: 0) > 0) {
                    ((userProfile?.winCount?.toFloat() ?: 0f) / (userProfile?.totalGames?.toFloat() ?: 1f) * 100).toInt()
                } else 0
                listOf(
                    Triple("🎮", "${userProfile?.totalGames ?: 0}", "Matches"),
                    Triple("🏆", "$winRate%", "Win Rate"),
                    Triple("🔬", userProfile?.topCategory?.take(5) ?: "None", "Best Cat.")
                ).forEachIndexed { i, (emoji, value, label) ->
                    val brushes = listOf(
                        Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9))),
                        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899)))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), spotColor = brushes[i].toString().substring(0, 10).let { Color(0xFF1E1B4B).copy(0.3f) })
                            .clip(RoundedCornerShape(18.dp))
                            .background(brushes[i])
                            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(18.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 28.sp)
                            Text(value, fontFamily = FredokaOne, fontSize = 16.sp, color = Color.White)
                            Text(label, fontFamily = Fredoka, fontSize = 10.sp, color = Color.White.copy(0.8f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== MATCH HISTORY =====
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("⚔️  Match History", fontFamily = FredokaOne, fontSize = 20.sp, color = Color(0xFF1E1B4B))
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (matchHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp)).padding(28.dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matches yet!", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 18.sp)
                        Text("Start playing to see your history", fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 14.sp)
                    }
                }
            } else {
                matchHistory.forEach { match ->
                    CartoonMatchHistoryCard(match)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Decorations (on top)
        Text("🦓", fontSize = 56.sp, modifier = Modifier.offset(290.dp, 100.dp).rotate(zebraRotate))
        Text("⭐", fontSize = 28.sp, modifier = Modifier.offset(20.dp, 180.dp).scale(avatarScale))
        Text("🦒", fontSize = 32.sp, modifier = Modifier.offset(300.dp, 280.dp).rotate(-zebraRotate))
    }
}

@Composable
fun CartoonMatchHistoryCard(match: MatchResult) {
    val isWin = match.result == "won"
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
            .clip(RoundedCornerShape(20.dp))
            .background(if (isWin) Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9)))
                else Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFF97316))))
            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(Color.White.copy(0.25f)).border(2.dp, Color(0xFF1E1B4B), CircleShape),
                contentAlignment = Alignment.Center) {
                Text(match.opponentIcon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("vs ${match.opponentName}", fontFamily = FredokaOne, color = Color.White, fontSize = 15.sp)
                Text(match.category, fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(match.score, fontFamily = FredokaOne, fontSize = 18.sp, color = Color.White)
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.25f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(if (isWin) "WIN 🏆" else "LOSS", fontFamily = FredokaOne, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

// Legacy compatibility
@Composable
fun ProfileStatCard(
    icon: ImageVector? = null,
    emoji: String? = null,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().offset(3.dp, 4.dp).height(100.dp)
            .clip(RoundedCornerShape(18.dp)).background(Color(0xFF1E1B4B)))
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)
            .clip(RoundedCornerShape(18.dp)).background(color.copy(0.15f))
            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(18.dp)).padding(10.dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji ?: "📊", fontSize = 24.sp)
                Text(value, fontFamily = FredokaOne, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                Text(label, fontFamily = Fredoka, fontSize = 10.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun MatchHistoryCard(match: MatchResult) = CartoonMatchHistoryCard(match)
