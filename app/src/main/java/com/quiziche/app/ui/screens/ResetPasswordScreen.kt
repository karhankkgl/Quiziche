package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    onNavigateBackToLogin: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "reset_anim")
    val lockScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "l"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF10B981), Color(0xFFFBBF24))))
    ) {
        Box(
            modifier = Modifier.align(Alignment.TopStart).offset((-50).dp, (-50).dp)
                .size(200.dp).clip(CircleShape)
                .background(Color(0xFF10B981).copy(0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔐", fontSize = 100.sp, modifier = Modifier.scale(lockScale))
            Spacer(modifier = Modifier.height(20.dp))
            Text("New Password", fontFamily = FredokaOne, fontSize = 38.sp, color = Color.White, textAlign = TextAlign.Center)
            Text("Set a strong new password 💪", fontFamily = Fredoka, fontSize = 16.sp,
                color = Color.White.copy(0.85f), modifier = Modifier.padding(bottom = 36.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(0.1f))
                    .border(3.dp, Color.White.copy(0.2f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CartoonTextField(value = newPassword, onValueChange = { newPassword = it },
                        placeholder = "New Password", emoji = "🔒", isPassword = true)
                    CartoonTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password", emoji = "🔑", isPassword = true)

                    if (message != null) {
                        Text(message!!, color = if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            fontFamily = Fredoka, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSuccess) {
                CartoonButton(
                    text = "🔓  Update Password",
                    onClick = {
                        if (newPassword != confirmPassword) { message = "Passwords don't match!"; return@CartoonButton }
                        if (newPassword.length < 6) { message = "Password too short!"; return@CartoonButton }
                        isLoading = true
                        scope.launch {
                            message = "Password updated! ✅"
                            isSuccess = true
                            isLoading = false
                        }
                    },
                    isLoading = isLoading,
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFFFBBF24))),
                    textColor = Color(0xFF064E3B),
                    borderColor = Color(0xFF064E3B)
                )
            } else {
                CartoonButton(
                    text = "🚀  Back to Login",
                    onClick = onNavigateBackToLogin,
                    bgBrush = Brush.horizontalGradient(listOf(Color.White, Color(0xFFEDE9FE))),
                    textColor = Color(0xFF1E1B4B),
                    borderColor = Color(0xFF1E1B4B)
                )
            }
        }

        // Decorations (on top)
        repeat(5) { i ->
            Text("⭐", fontSize = (14 + i * 4).sp, color = Color.White.copy(0.3f),
                modifier = Modifier.offset((30 + i * 70).dp, (50 + i * 40).dp))
        }
    }
}
