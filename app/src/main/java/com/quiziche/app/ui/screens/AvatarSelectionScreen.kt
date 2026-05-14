package com.quiziche.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.data.repository.UserRepository
import com.quiziche.app.ui.theme.FredokaOne
import com.quiziche.app.ui.components.ModernBackButton
import kotlinx.coroutines.launch

@Composable
fun AvatarSelectionScreen(
    onNavigateBack: () -> Unit
) {
    val avatars = listOf("🎮", "🦁", "🐧", "🦖", "🦄", "🐼", "🤖", "🦊", "🐶", "🐱", "🦉", "🐸", "🐙", "🦋", "🐯", "🐴")
    val userRepository = remember { UserRepository() }
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var currentAvatar by remember { mutableStateOf("🎮") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authRepository.currentUserUID?.let { uid ->
            val res = userRepository.getUserProfile(uid)
            if (res.isSuccess) {
                currentAvatar = res.getOrNull()?.avatarIcon ?: "🎮"
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), spotColor = Color(0xFF475569).copy(0.4f))
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))))
                    .padding(top = 48.dp, bottom = 20.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModernBackButton(onClick = onNavigateBack)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Select Avatar", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(avatars) { avatar ->
                    val isSelected = currentAvatar == avatar
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(elevation = if (isSelected) 8.dp else 2.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White.copy(0.2f) else Color.White.copy(0.05f))
                            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color(0xFFEC4899) else Color.White.copy(0.2f), CircleShape)
                            .clickable(enabled = !isLoading) {
                                isLoading = true
                                scope.launch {
                                    authRepository.currentUserUID?.let { uid ->
                                        val res = userRepository.getUserProfile(uid)
                                        if (res.isSuccess) {
                                            val user = res.getOrNull()!!
                                            val updated = user.copy(avatarIcon = avatar)
                                            userRepository.updateUserProfile(updated)
                                            currentAvatar = avatar
                                        }
                                    }
                                    isLoading = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatar, fontSize = 40.sp)
                    }
                }
            }
        }
    }
}
