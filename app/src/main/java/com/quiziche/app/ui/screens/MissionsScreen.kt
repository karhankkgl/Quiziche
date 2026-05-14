package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.model.Mission
import com.quiziche.app.data.model.UserMissionProgress
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.ui.theme.Fredoka
import com.quiziche.app.ui.theme.FredokaOne
import com.quiziche.app.ui.components.ModernBackButton
import kotlinx.coroutines.launch

@Composable
fun MissionsScreen(
    onNavigateBack: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    // We will just mock some global missions here and let the repository handle progress if it existed.
    // For a real app, you would fetch these from Firestore "missions" collection.
    val missionsList = listOf(
        Mission("m1", "Daily Scholar", "Play 3 games today", 50, 100, 3, "play_games"),
        Mission("m2", "Unstoppable", "Win 2 games in a row", 100, 200, 2, "win_games"),
        Mission("m3", "Quiz Master", "Score over 50 points in a single match", 150, 300, 50, "score_points"),
        Mission("m4", "Social Butterfly", "Send a friend request", 25, 50, 1, "add_friend")
    )
    
    // Mock user progress
    val userProgress = remember { mutableStateMapOf<String, UserMissionProgress>(
        "m1" to UserMissionProgress("m1", 2, false, false),
        "m2" to UserMissionProgress("m2", 2, true, false), // ready to claim
        "m3" to UserMissionProgress("m3", 40, false, false),
        "m4" to UserMissionProgress("m4", 1, true, true)  // claimed
    )}
    
    val infiniteTransition = rememberInfiniteTransition(label = "anim")
    val starScale by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "s")

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color(0xFF475569).copy(0.4f))
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))))
                    .padding(top = 48.dp, bottom = 20.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModernBackButton(onClick = onNavigateBack)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Global Missions", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
                }
            }
            
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(missionsList) { mission ->
                    val progress = userProgress[mission.id] ?: UserMissionProgress(mission.id, 0, false, false)
                    MissionCard(
                        mission = mission,
                        progress = progress,
                        onClaim = {
                            // Update local state for visual feedback
                            userProgress[mission.id] = progress.copy(isClaimed = true)
                            // In real app: call userRepository.claimMissionReward(mission.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MissionCard(mission: Mission, progress: UserMissionProgress, onClaim: () -> Unit) {
    val progressPercent = (progress.currentAmount.toFloat() / mission.targetAmount.toFloat()).coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier.fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.6f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .border(2.dp, if (progress.isCompleted && !progress.isClaimed) Color(0xFF10B981) else Color.White.copy(0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                .border(2.dp, Color.White.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center) {
                Text("📜", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mission.title, fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                Text(mission.description, fontFamily = Fredoka, color = Color.White.copy(0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Progress bar
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(0.1f))) {
                    Box(modifier = Modifier.fillMaxWidth(progressPercent).fillMaxHeight().clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9)))))
                }
                Text("${progress.currentAmount} / ${mission.targetAmount}", fontFamily = FredokaOne, color = Color.White.copy(0.8f), fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            if (progress.isClaimed) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Gray.copy(0.5f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Claimed", fontFamily = FredokaOne, color = Color.White, fontSize = 12.sp)
                }
            } else if (progress.isCompleted) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
                    .clickable { onClaim() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Claim", fontFamily = FredokaOne, color = Color.White, fontSize = 12.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text("🪙 ${mission.rewardCoins}", fontFamily = FredokaOne, color = Color(0xFFFBBF24), fontSize = 12.sp)
                    Text("⚡ ${mission.rewardXp}", fontFamily = FredokaOne, color = Color(0xFF0EA5E9), fontSize = 12.sp)
                }
            }
        }
    }
}
