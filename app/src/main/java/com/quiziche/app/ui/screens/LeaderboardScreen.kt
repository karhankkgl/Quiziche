package com.quiziche.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsKabaddi
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
import com.quiziche.app.data.model.User
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.data.repository.UserRepository
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
        if (result.isSuccess) {
            leaderboard = result.getOrDefault(emptyList())
        }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4F46E5), // Indigo
                        Color(0xFF9333EA), // Purple
                        Color(0xFFDB2777)  // Pink
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Global Leaderboard",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // List
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = Color.White
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF9333EA))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        itemsIndexed(leaderboard) { index, user ->
                            val rank = index + 1
                            val isCurrentUser = user.uid == gameRepository.currentUid
                            
                            LeaderboardItem(
                                rank = rank,
                                user = user,
                                isCurrentUser = isCurrentUser,
                                isInviteSent = inviteSentTo == user.uid,
                                onChallengeClick = {
                                    scope.launch {
                                        val result = gameRepository.sendInvite(user.uid, currentUserName)
                                        if (result.isSuccess) {
                                            inviteSentTo = user.uid
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    user: User,
    isCurrentUser: Boolean,
    isInviteSent: Boolean,
    onChallengeClick: () -> Unit
) {
    val backgroundColor = if (isCurrentUser) Color(0xFFF3E8FF) else Color(0xFFF9FAFB)
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF6B7280) // Gray
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isCurrentUser) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = rankColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.avatarIcon, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser) "You (${user.name})" else user.name,
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Class: ${user.getClassName()}",
                    color = Color(0xFF9333EA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // ELO and Challenge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.elo} ELO",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                
                if (!isCurrentUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onChallengeClick,
                        enabled = !isInviteSent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInviteSent) Color(0xFF9CA3AF) else Color(0xFF10B981)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isInviteSent) "Sent" else "Challenge",
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
