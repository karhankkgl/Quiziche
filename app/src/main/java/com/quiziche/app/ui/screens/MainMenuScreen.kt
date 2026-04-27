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

data class OngoingGame(
    val id: Int,
    val opponentIcon: String,
    val opponentName: String,
    val status: String,
    val category: String,
    val score: String
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

    val ongoingGames = listOf(
        OngoingGame(1, "🎯", "Alex", "Your Turn", "Science", "3-2"),
        OngoingGame(2, "🎮", "Maria", "Waiting", "History", "1-1"),
        OngoingGame(3, "🎪", "John", "Your Turn", "Sports", "2-0")
    )

    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")
    val rocket1Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "r1"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "sr"
    )
    val zebraScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "z"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFEDE9FE))))
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
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))))
                    .border(0.dp, Color.Transparent, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToProfile() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEC4899))))
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = userProfile?.avatarIcon ?: "🎮", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Hey, ${userProfile?.name?.split(" ")?.firstOrNull() ?: "Explorer"}! 👋",
                                    fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp)
                                Text(text = "Level ${userProfile?.level ?: 1} • Quiz Master",
                                    fontFamily = Fredoka, color = Color.White.copy(0.75f), fontSize = 13.sp)
                            }
                        }
                        // ELO badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFBBF24))
                                .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(16.dp))
                                .clickable { onNavigateToLeaderboard() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${userProfile?.elo ?: 1000}", fontFamily = FredokaOne, fontSize = 16.sp, color = Color(0xFF1E1B4B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🪙 ${userProfile?.coins ?: 0}", fontFamily = FredokaOne, fontSize = 14.sp, color = Color(0xFF1E1B4B))
                            }
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
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF7C3AED).copy(0.5f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))))
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(28.dp))
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Play Now! →", fontFamily = FredokaOne, color = Color(0xFF7C3AED), fontSize = 14.sp)
                        }
                    }
                    // Decoration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮", fontSize = 52.sp)
                        Text("VS", fontFamily = FredokaOne, fontSize = 18.sp, color = Color(0xFFFBBF24))
                        Text("🎯", fontSize = 40.sp)
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
                    bgBrush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF7C3AED))),
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
                    bgBrush = Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFFBBF24))),
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
                    bgBrush = Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0EA5E9))),
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
                    bgBrush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                    onClick = onNavigateToLeaderboard
                ) {
                    Text("🏆", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rankings", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 16.sp)
                    Text("Top 100", fontFamily = Fredoka, color = Color(0xFF1E1B4B).copy(0.75f), fontSize = 12.sp)
                }
            }

            // ===== ONGOING GAMES =====
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡ Active Battles", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 20.sp)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7C3AED))
                        .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                        .clickable { onNavigateToOngoingGames() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("See All →", fontFamily = Fredoka, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ongoingGames.forEach { game ->
                    CartoonOngoingCard(game = game, onNavigateToGame = onNavigateToGame)
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
                            textColor = Color(0xFF1E1B4B)
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
                .background(Color.White)
                .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                CartoonNavItem(emoji = "🏠", label = "Home", isSelected = true, onClick = {})
                CartoonNavItem(emoji = "🗂️", label = "Categories", onClick = onNavigateToCategories)
                CartoonNavItem(emoji = "⚡", label = "Battles", onClick = onNavigateToOngoingGames, badge = 2)
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
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF1E1B4B).copy(0.4f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun CartoonOngoingCard(game: OngoingGame, onNavigateToGame: (String) -> Unit) {
    val isYourTurn = game.status == "Your Turn"
    val bgBrush = if (isYourTurn)
        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9)))
    else
        Brush.linearGradient(listOf(Color.White, Color(0xFFF3F4F6)))

    Box(
        modifier = Modifier
            .width(240.dp)
            .height(130.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(if (isYourTurn) Color.White.copy(0.25f) else Color(0xFFEDE9FE))
                        .border(2.dp, Color(0xFF1E1B4B), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(game.opponentIcon, fontSize = 22.sp) }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("vs ${game.opponentName}", fontFamily = FredokaOne,
                        color = if (isYourTurn) Color.White else Color(0xFF1E1B4B), fontSize = 15.sp)
                    Text(game.category, fontFamily = Fredoka,
                        color = if (isYourTurn) Color.White.copy(0.75f) else Color(0xFF6B7280), fontSize = 12.sp)
                }
                Text(game.score, fontFamily = FredokaOne,
                    color = if (isYourTurn) Color.White else Color(0xFF1E1B4B), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isYourTurn) Color.White else Color(0xFFEDE9FE))
                    .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                    .clickable { if (isYourTurn) onNavigateToGame(game.id.toString()) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isYourTurn) "⚡ Play Now!" else "⏳ Waiting...",
                    fontFamily = FredokaOne,
                    color = if (isYourTurn) Color(0xFF10B981) else Color(0xFF6B7280),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CartoonNavItem(
    emoji: String,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    badge: Int = 0
) {
    Box(modifier = Modifier.clickable { onClick() }.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF3F4F6))
                        .then(if (isSelected) Modifier.border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(14.dp)) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
                if (badge > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(4.dp, (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(badge.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontFamily = Fredoka, fontSize = 11.sp,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    badgeCount: Int = 0
) {
    // Keep for legacy usage
    CartoonNavItem(emoji = when (label) {
        "Home" -> "🏠"
        "Categories" -> "🗂️"
        "Games" -> "⚡"
        "Friends" -> "🦁"
        else -> "⭐"
    }, label = label, isSelected = isSelected, onClick = onClick, badge = badgeCount)
}
