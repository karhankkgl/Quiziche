package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.data.model.User
import kotlinx.coroutines.launch

data class GlobalMission(
    val id: Int,
    val icon: String,
    val title: String,
    val reward: String,
    val progress: Float,
    val color: Color
)

@Composable
fun MainMenuScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToOngoingGames: () -> Unit,
    onNavigateToGameSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToGame: (roomId: String) -> Unit,
    onNavigateToFriends: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }
    val gameRepository = remember { GameRepository() }
    val scope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<User?>(null) }
    val invites by gameRepository.observeInvites().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        authRepository.currentUserUID?.let { uid ->
            val result = userRepository.getUserProfile(uid)
            if (result.isSuccess) userProfile = result.getOrNull()
        }
    }

    val globalMissions = listOf(
        GlobalMission(1, "🔬", "Science Master", "500 🪙", 0.7f, Color(0xFF7C3AED)),
        GlobalMission(2, "🌍", "World Explorer", "300 🪙", 0.4f, Color(0xFF10B981)),
        GlobalMission(3, "🏛️", "History Buff", "1000 🪙", 0.1f, Color(0xFFF97316))
    )

    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")
    // Animations disabled as requested
    val rocket1Y = 0f
    val starRotate = 15f
    val zebraScale = 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        // Top decorative blob
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF7C3AED).copy(0.18f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            // ===== HEADER =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color.Black.copy(0.8f))
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                    .border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: Avatar + Name
                    Row(
                        modifier = Modifier.weight(1f).clickable { onNavigateToProfile() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEC4899))))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userProfile?.avatarIcon ?: "🎮", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userProfile?.name?.split(" ")?.firstOrNull() ?: "Explorer",
                            fontFamily = FredokaOne,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }

                    // CENTER: Logo
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.quiziche.app.R.drawable.quiziche_minimal),
                        contentDescription = "Logo",
                        modifier = Modifier.height(30.dp).weight(0.8f),
                        alignment = Alignment.Center
                    )

                    // RIGHT: ELO & Coins (50% transparent)
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(0.12f))
                                .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateToLeaderboard() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("⚡", fontSize = 13.sp)
                            Text("${userProfile?.elo ?: 1000}", fontFamily = FredokaOne, fontSize = 13.sp, color = Color.White, maxLines = 1)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🪙", fontSize = 13.sp)
                            Text("${userProfile?.coins ?: 0}", fontFamily = FredokaOne, fontSize = 13.sp, color = Color.White, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== QUICK GAME CARD =====
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(140.dp)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp), spotColor = Color.Black.copy(0.8f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))))
                    .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(28.dp))
                    .clickable { onNavigateToGameSettings() }
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("⚡ Quick Match", fontFamily = FredokaOne, color = Color.White, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Find an opponent & battle now!", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF475569).copy(0.4f))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(2.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Play Now! →", fontFamily = FredokaOne, color = Color(0xFF7C3AED), fontSize = 14.sp)
                        }
                    }
                    // Decoration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮", fontSize = 52.sp)
                        Text("VS", fontFamily = FredokaOne, fontSize = 18.sp, color = Color(0xFFFBBF24))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== MODE CARDS =====
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Singleplayer - Zebra themed
                CartoonMenuCard(
                    modifier = Modifier.weight(1f),
                    bgBrush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))),
                    onClick = onNavigateToCategories
                ) {
                    Text("🦓", fontSize = 40.sp, modifier = Modifier.scale(zebraScale))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Solo Mode", fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                    Text("Practice!", fontFamily = Fredoka, color = Color.White.copy(0.75f), fontSize = 12.sp)
                }
                // Friends - Lion themed
                CartoonMenuCard(
                    modifier = Modifier.weight(1f),
                    bgBrush = Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))),
                    onClick = onNavigateToFriends
                ) {
                    Text("🦁", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Friends", fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                    Text("Challenge!", fontFamily = Fredoka, color = Color.White.copy(0.75f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Categories - Dolphin themed
                CartoonMenuCard(
                    modifier = Modifier.weight(1f),
                    bgBrush = Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
                    onClick = onNavigateToCategories
                ) {
                    Text("🐬", fontSize = 40.sp, modifier = Modifier.offset(y = rocket1Y.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categories", fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                    Text("Explore!", fontFamily = Fredoka, color = Color.White.copy(0.75f), fontSize = 12.sp)
                }
                // Leaderboard - Trophy themed
                CartoonMenuCard(
                    modifier = Modifier.weight(1f),
                    bgBrush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
                    onClick = onNavigateToLeaderboard
                ) {
                    Text("🏆", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rankings", fontFamily = FredokaOne, color = Color(0xFF78350F), fontSize = 16.sp)
                    Text("Top 100", fontFamily = Fredoka, color = Color(0xFF78350F).copy(0.75f), fontSize = 12.sp)
                }
            }

            // ===== GLOBAL MISSIONS =====
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌍 Global Missions", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black.copy(0.4f))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7C3AED))
                        .border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Daily", fontFamily = Fredoka, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                globalMissions.forEach { mission ->
                    CartoonMissionCard(mission = mission)
                }
            }

            // ===== INVITE DIALOG =====
            if (invites.isNotEmpty()) {
                val invite = invites.first()
                val senderName = invite["senderName"] as? String ?: "Someone"
                val inviteId = invite["inviteId"] as? String ?: ""
                val senderUid = invite["senderUid"] as? String ?: ""
                val category = invite["category"] as? String ?: "General"
                var isAccepting by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { scope.launch { gameRepository.rejectInvite(inviteId) } },
                    containerColor = Color(0xFF2D1B69),
                    shape = RoundedCornerShape(28.dp),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("⚔️", fontSize = 48.sp)
                            Text("Game Challenge!", fontFamily = FredokaOne, color = Color.White, fontSize = 22.sp)
                        }
                    },
                    text = {
                        Text("$senderName wants to battle you in $category! Do you accept? 🔥",
                            fontFamily = Fredoka, color = Color(0xFFE9D5FF), fontSize = 16.sp)
                    },
                    confirmButton = {
                        CartoonButton(
                            text = if (isAccepting) "Joining..." else "⚔️ Accept!",
                            onClick = {
                                isAccepting = true
                                scope.launch {
                                    val result = gameRepository.acceptInvite(inviteId, senderUid, category)
                                    isAccepting = false
                                    if (result.isSuccess) onNavigateToGame(result.getOrNull()!!)
                                }
                            },
                            isLoading = isAccepting,
                            bgBrush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFFFBBF24))),
                            textColor = Color(0xFF475569)
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { scope.launch { gameRepository.rejectInvite(inviteId) } }) {
                            Text("Decline 😢", fontFamily = Fredoka, color = Color(0xFFFCA5A5))
                        }
                    }
                )
            }
        }

        // ===== BOTTOM NAVIGATION =====
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                CartoonNavItem(emoji = "🏠", label = "Home", isSelected = true, onClick = {})
                CartoonNavItem(emoji = "🗂️", label = "Categories", onClick = onNavigateToCategories)
                CartoonNavItem(emoji = "🏆", label = "Rankings", onClick = onNavigateToLeaderboard)
                CartoonNavItem(emoji = "🦁", label = "Friends", onClick = onNavigateToFriends)
            }
        }

        // Floating decorations (on top)
        Text("🚀", fontSize = 36.sp, modifier = Modifier.offset(310.dp, 80.dp).offset(y = rocket1Y.dp).rotate(-20f))
        Text("⭐", fontSize = 20.sp, modifier = Modifier.offset(20.dp, 130.dp).rotate(starRotate).scale(0.9f), color = Color(0xFFFBBF24))
        Text("✨", fontSize = 16.sp, modifier = Modifier.offset(300.dp, 200.dp))
    }
}

@Composable
fun CartoonMenuCard(
    modifier: Modifier = Modifier,
    bgBrush: Brush,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(elevation = 18.dp, shape = RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.7f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun CartoonMissionCard(mission: GlobalMission) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(140.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.6f))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
            .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(mission.color.copy(0.2f))
                        .border(1.dp, mission.color.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(mission.icon, fontSize = 22.sp) }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(mission.title, fontFamily = FredokaOne, color = Color.White, fontSize = 15.sp)
                    Text("Reward: ${mission.reward}", fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(mission.progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(mission.color, mission.color.copy(0.7f))))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("${(mission.progress * 100).toInt()}% Complete", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 11.sp)
        }
    }
}


