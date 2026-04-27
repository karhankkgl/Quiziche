package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.quiziche.app.data.model.User
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    currentUserName: String
) {
    val userRepository = remember { UserRepository() }
    val gameRepository = remember { GameRepository() }
    val scope = rememberCoroutineScope()
    var leaderboard by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var inviteSentTo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = userRepository.getLeaderboard(50)
        if (result.isSuccess) leaderboard = result.getOrDefault(emptyList())
        isLoading = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lb_anim")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "t"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "s"
    )

    Box(modifier = Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFEDE9FE))))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                // ===== HEADER =====
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                        .border(0.dp, Color.Transparent)
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                                .background(Color.White.copy(0.3f)).border(2.dp, Color(0xFF1E1B4B), CircleShape)
                                .clickable { onNavigateBack() },
                                contentAlignment = Alignment.Center) {
                                Text("←", fontSize = 20.sp, color = Color(0xFF1E1B4B))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🏆", fontSize = 80.sp, modifier = Modifier.scale(trophyScale))
                        Text("Global Rankings", fontFamily = FredokaOne, fontSize = 30.sp, color = Color(0xFF1E1B4B))
                        Text("Who's the Quiz Champion? 👑", fontFamily = Fredoka, fontSize = 15.sp, color = Color(0xFF78350F))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Top 3 podium
                if (leaderboard.size >= 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd place
                        PodiumCard(user = leaderboard[1], rank = 2, height = 90.dp, modifier = Modifier.weight(1f))
                        // 1st place
                        PodiumCard(user = leaderboard[0], rank = 1, height = 120.dp, modifier = Modifier.weight(1f))
                        // 3rd place
                        PodiumCard(user = leaderboard[2], rank = 3, height = 70.dp, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFBBF24))
                    }
                }
            } else {
                val startIdx = if (leaderboard.size >= 3) 3 else 0
                itemsIndexed(leaderboard.drop(startIdx)) { index, user ->
                    val rank = index + startIdx + 1
                    val isCurrentUser = user.uid == gameRepository.currentUid

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .fillMaxWidth()
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCurrentUser) Color(0xFFEDE9FE) else Color.White)
                            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank badge
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(Color(0xFF1E1B4B)),
                                    contentAlignment = Alignment.Center) {
                                    Text("#$rank", fontFamily = FredokaOne, fontSize = 12.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                // Avatar
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)).border(2.dp, Color(0xFF1E1B4B), CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Text(user.avatarIcon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isCurrentUser) "You 👋" else user.name, fontFamily = FredokaOne,
                                        color = Color(0xFF1E1B4B), fontSize = 15.sp)
                                    Text(user.getClassName(), fontFamily = Fredoka, color = Color(0xFF7C3AED), fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFBBF24)).border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text("⚡ ${user.elo}", fontFamily = FredokaOne, fontSize = 13.sp, color = Color(0xFF1E1B4B))
                                    }
                                    if (!isCurrentUser) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                            .background(if (inviteSentTo == user.uid) Color(0xFF9CA3AF) else Color(0xFF10B981))
                                            .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(10.dp))
                                            .clickable(enabled = inviteSentTo != user.uid) {
                                                scope.launch {
                                                    val r = gameRepository.sendInvite(user.uid, currentUserName)
                                                    if (r.isSuccess) inviteSentTo = user.uid
                                                }
                                            }.padding(horizontal = 8.dp, vertical = 2.dp)) {
                                            Text(if (inviteSentTo == user.uid) "Sent ✓" else "⚔️ Battle",
                                                fontFamily = FredokaOne, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Decorations (on top)
        Text("⭐", fontSize = 26.sp, modifier = Modifier.offset(20.dp, 100.dp).rotate(starRotate), color = Color(0xFFFBBF24))
        Text("✨", fontSize = 20.sp, modifier = Modifier.offset(310.dp, 130.dp).rotate(-starRotate).scale(0.8f))
        Text("🌟", fontSize = 18.sp, modifier = Modifier.offset(50.dp, 200.dp))
    }
}

@Composable
fun PodiumCard(user: User, rank: Int, height: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val (emoji, brush) = when (rank) {
        1 -> "🥇" to Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
        2 -> "🥈" to Brush.linearGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
        else -> "🥉" to Brush.linearGradient(listOf(Color(0xFFCD7F32), Color(0xFF92400E)))
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 32.sp)
        Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(brush)
            .border(3.dp, Color(0xFF1E1B4B), CircleShape), contentAlignment = Alignment.Center) {
            Text(user.avatarIcon, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(user.name.split(" ").first(), fontFamily = FredokaOne, fontSize = 12.sp, color = Color(0xFF1E1B4B))
        Box(modifier = Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(brush).border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            contentAlignment = Alignment.Center) {
            Text("${user.elo}", fontFamily = FredokaOne, fontSize = 14.sp, color = Color.White)
        }
    }
}
