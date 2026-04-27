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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.model.User
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateBack: () -> Unit
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
    val waveRotate by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "w"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFECFEFF), Color(0xFFCFFAFE), Color(0xFFEDE9FE))))
    ) {
        // Decorations
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Color.White.copy(0.2f)).border(2.dp, Color.White.copy(0.4f), CircleShape)
                            .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center) {
                            Text("←", fontSize = 20.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("🐬  Friends", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
                    }
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
                    Box(modifier = Modifier.weight(1f)) {
                        if (isSelected) {
                            Box(modifier = Modifier.fillMaxWidth().offset(3.dp, 4.dp).height(44.dp)
                                .clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E1B4B)))
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6)))
                                    else Brush.horizontalGradient(listOf(Color.White, Color(0xFFF3F4F6))))
                                .border(3.dp, if (isSelected) Color(0xFF1E1B4B) else Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                .clickable { selectedTab = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title, fontFamily = FredokaOne, fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFF1E1B4B))
                        }
                    }
                }
            }

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White).border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(24.dp))
                                .padding(28.dp),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🐬", fontSize = 56.sp, modifier = Modifier.offset(y = dolphinY.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No friends yet!", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 18.sp)
                                    Text("Invite someone to play!", fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 14.sp)
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
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White).border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(24.dp))
                            .padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📨", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No pending requests", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 16.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun CartoonFriendItem(friend: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                .border(2.dp, Color(0xFF1E1B4B), CircleShape),
                contentAlignment = Alignment.Center) {
                Text(friend.avatarIcon, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Online", fontFamily = Fredoka, color = Color(0xFF10B981), fontSize = 12.sp)
                }
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))))
                .border(2.dp, Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("⚔️ Battle", fontFamily = FredokaOne, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun FriendListItem(name: String, status: String, icon: String) {
    CartoonFriendItem(friend = User(name = name, avatarIcon = icon))
}

@Composable
fun FriendRequestItem(name: String, icon: String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Color.White).border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(20.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEDE9FE))
                .border(2.dp, Color(0xFF1E1B4B), CircleShape), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("✓", fontSize = 20.sp, color = Color(0xFF10B981))
        }
    }
}
