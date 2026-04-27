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
import com.quiziche.app.ui.components.*
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
        .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                // ===== HEADER =====
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp), spotColor = Color.Black.copy(0.8f))
                        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                        .border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            ModernBackButton(onClick = onNavigateBack)
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🏆", fontSize = 80.sp, modifier = Modifier.scale(trophyScale))
                        Text("Global Rankings", fontFamily = FredokaOne, fontSize = 30.sp, color = Color(0xFF78350F))
                        Text("Who's the Quiz Champion? 👑", fontFamily = Fredoka, fontSize = 15.sp, color = Color(0xFF78350F).copy(0.8f))
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
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.6f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCurrentUser) Color(0xFF334155) else Color(0xFF1E293B))
                            .border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank badge
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                                    .background(Color(0xFF475569)),
                                    contentAlignment = Alignment.Center) {
                                    Text("#$rank", fontFamily = FredokaOne, fontSize = 12.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                // Avatar
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                                    .background(Color.White.copy(0.05f)).border(1.5.dp, Color.White.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Text(user.avatarIcon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isCurrentUser) "You 👋" else user.name, fontFamily = FredokaOne,
                                        color = Color.White, fontSize = 15.sp)
                                    Text(user.getClassName(), fontFamily = Fredoka, color = Color(0xFFC4B5FD), fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFBBF24)).border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text("⚡ ${user.elo}", fontFamily = FredokaOne, fontSize = 13.sp, color = Color(0xFF78350F))
                                    }
                                    if (!isCurrentUser) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                            .background(if (inviteSentTo == user.uid) Color(0xFF4B5563) else Color(0xFF059669))
                                            .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(10.dp))
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
        Box(modifier = Modifier
            .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Color.Black.copy(0.6f))
            .size(54.dp).clip(CircleShape).background(brush)
            .border(1.5.dp, Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Text(user.avatarIcon, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(user.name.split(" ").first(), fontFamily = FredokaOne, fontSize = 12.sp, color = Color.White)
        Box(modifier = Modifier.fillMaxWidth().height(height)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp), spotColor = Color.Black.copy(0.7f))
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(brush).border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            contentAlignment = Alignment.Center) {
            Text("${user.elo}", fontFamily = FredokaOne, fontSize = 14.sp, color = Color.White)
        }
    }
}
