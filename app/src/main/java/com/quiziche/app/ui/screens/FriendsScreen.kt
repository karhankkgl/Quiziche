package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.model.User
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var friendsList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = authRepository.currentUserUID
        if (uid != null) {
            val result = userRepository.getUserProfile(uid)
            if (result.isSuccess) {
                val user = result.getOrNull()
                friendsList = user?.friends?.mapNotNull { fid ->
                    userRepository.getUserProfile(fid).getOrNull()
                } ?: emptyList()
            }
        }
        isLoading = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "friends_anim")
    val dolphinY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "d"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color(0xFF475569).copy(0.4f))
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                    .padding(top = 48.dp, bottom = 20.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Text("🐬  Friends", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Search
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(0.2f))
                            .border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            singleLine = true,
                            placeholder = { Text("Search friends...", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 16.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Fredoka, fontSize = 16.sp, color = Color.White),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("🐬 My Friends", "📨 Requests").forEachIndexed { idx, title ->
                    val isSelected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(elevation = if (isSelected) 8.dp else 4.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6)))
                                else Brush.linearGradient(listOf(Color.White.copy(0.05f), Color.White.copy(0.05f)))
                            )
                            .border(1.5.dp, if (isSelected) Color.White.copy(0.2f) else Color.White.copy(0.1f), RoundedCornerShape(16.dp))
                            .clickable { selectedTab = idx }
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, fontFamily = FredokaOne, fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color.White.copy(0.7f))
                    }
                }
            }

            // List
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF0EA5E9))
                            }
                        }
                    } else if (friendsList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF475569).copy(0.25f))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1E293B)).border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                                .padding(28.dp),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🐬", fontSize = 56.sp, modifier = Modifier.offset(y = dolphinY.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No friends yet!", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp)
                                    Text("Invite someone to play!", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        val filtered = friendsList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        items(filtered.size) { idx ->
                            CartoonFriendItem(friend = filtered[idx])
                        }
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF475569).copy(0.25f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E293B)).border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                            .padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📨", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No pending requests", fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
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
                CartoonNavItem(emoji = "🏠", label = "Home", isSelected = false, onClick = onNavigateToMainMenu)
                CartoonNavItem(emoji = "🗂️", label = "Categories", onClick = onNavigateToCategories)
                CartoonNavItem(emoji = "🏆", label = "Rankings", onClick = onNavigateToLeaderboard)
                CartoonNavItem(emoji = "🦁", label = "Friends", isSelected = true, onClick = {})
            }
        }
    }
}

@Composable
fun CartoonFriendItem(friend: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.6f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .border(1.5.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                .border(2.dp, Color(0xFF475569), CircleShape),
                contentAlignment = Alignment.Center) {
                Text(friend.avatarIcon, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Online", fontFamily = Fredoka, color = Color(0xFF10B981), fontSize = 12.sp)
                }
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                .border(2.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("⚔️ Battle", fontFamily = FredokaOne, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}
