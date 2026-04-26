package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
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
            if (result.isSuccess) {
                userProfile = result.getOrNull()
            }
        }
    }

    val ongoingGames = listOf(
        OngoingGame(1, "🎯", "Alex", "Your Turn", "Science", "3-2"),
        OngoingGame(2, "🎮", "Maria", "Waiting", "History", "1-1"),
        OngoingGame(3, "🎪", "John", "Your Turn", "Sports", "2-0")
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
                .padding(bottom = 80.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToProfile() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFA855F7), Color(0xFFEC4899))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userProfile?.avatarIcon ?: "🎮", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userProfile?.name ?: "Guest",
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Level ${userProfile?.level ?: 1}",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFEF9C3),
                        modifier = Modifier.padding(vertical = 4.dp).clickable { onNavigateToLeaderboard() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Leaderboard",
                                tint = Color(0xFFCA8A04),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile?.elo ?: 1000} ELO",
                                color = Color(0xFF854D0E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "|", color = Color(0xFFCA8A04).copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Coins",
                                tint = Color(0xFFCA8A04),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userProfile?.coins?.toString() ?: "0",
                                color = Color(0xFF854D0E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Main Action Buttons
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                // Quick Game Start
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                            )
                        )
                        .clickable { onNavigateToGameSettings() }
                        .padding(32.dp)
                ) {
                    Column {
                        Text(text = "🎯", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quick Game Start",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Find opponent and play now!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "⚡",
                        fontSize = 48.sp,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Singleplayer
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                                )
                            )
                            .clickable { onNavigateToCategories() }
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(text = "👤", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Singleplayer",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Practice solo",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Categories
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF97316), Color(0xFFEF4444))
                                )
                            )
                            .clickable { onNavigateToCategories() }
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(text = "🗂️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Categories",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Browse topics",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Ongoing Games Section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ongoing Games",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "See All >",
                        color = Color(0xFF9333EA),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToOngoingGames() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp)
                ) {
                    ongoingGames.forEach { game ->
                        OngoingGameCard(game, onNavigateToGame)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
            // Invite Dialog
            if (invites.isNotEmpty()) {
                val invite = invites.first()
                val senderName = invite["senderName"] as? String ?: "Someone"
                val inviteId = invite["inviteId"] as? String ?: ""
                val senderUid = invite["senderUid"] as? String ?: ""
                val category = invite["category"] as? String ?: "General"
                var isAccepting by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { 
                        scope.launch { gameRepository.rejectInvite(inviteId) }
                    },
                    title = { Text(text = "Game Challenge! ⚔️") },
                    text = { Text(text = "$senderName has challenged you to a game in $category!") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isAccepting = true
                                scope.launch {
                                    val result = gameRepository.acceptInvite(inviteId, senderUid, category)
                                    isAccepting = false
                                    if (result.isSuccess) {
                                        onNavigateToGame(result.getOrNull()!!)
                                    }
                                }
                            },
                            enabled = !isAccepting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text(if (isAccepting) "Accepting..." else "Accept")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { scope.launch { gameRepository.rejectInvite(inviteId) } }) {
                            Text("Decline", color = Color(0xFFEF4444))
                        }
                    }
                )
            }
        }

        // Bottom Navigation
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = true)
                BottomNavItem(
                    icon = Icons.Default.ViewModule,
                    label = "Categories",
                    onClick = onNavigateToCategories
                )
                BottomNavItem(
                    icon = Icons.Default.AccessTime,
                    label = "Games",
                    onClick = onNavigateToOngoingGames,
                    badgeCount = 2
                )
                BottomNavItem(
                    icon = Icons.Default.People,
                    label = "Friends",
                    onClick = onNavigateToFriends
                )
            }
        }
    }
}

@Composable
fun OngoingGameCard(game: OngoingGame, onNavigateToGame: (String) -> Unit) {
    val isYourTurn = game.status == "Your Turn"
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isYourTurn) Color.Transparent else Color.White,
        border = if (isYourTurn) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .width(280.dp)
            .background(
                if (isYourTurn) Brush.linearGradient(
                    listOf(Color(0xFF4ADE80), Color(0xFF10B981))
                )
                else Brush.linearGradient(listOf(Color.White, Color.White)),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isYourTurn) Color.White.copy(alpha = 0.3f)
                                else Color(0xFFF3F4F6)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = game.opponentIcon, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "vs ${game.opponentName}",
                            color = if (isYourTurn) Color.White else Color(0xFF1F2937),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = game.category,
                            color = if (isYourTurn) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = game.score,
                    color = if (isYourTurn) Color.White else Color(0xFF4B5563),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (isYourTurn) onNavigateToGame(game.id.toString()) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isYourTurn) Color.White else Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isYourTurn) "Your Turn - Play Now!" else "Waiting for opponent...",
                    color = if (isYourTurn) Color(0xFF059669) else Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
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
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color(0xFF9333EA) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = label,
                color = if (isSelected) Color(0xFF9333EA) else Color(0xFF9CA3AF),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
