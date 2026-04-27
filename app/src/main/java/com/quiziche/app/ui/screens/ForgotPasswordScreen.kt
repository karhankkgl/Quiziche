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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "forgot_anim")
    val telescopeRotate by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "t"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "sr"
    )
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "s"
    )
    val rocketY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -15f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "ry"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0C1445), Color(0xFF0EA5E9), Color(0xFF14B8A6))))
    ) {
        // Decorations
        Text("⭐", fontSize = 22.sp, modifier = Modifier.offset(20.dp, 80.dp).scale(starScale))
        Text("🌙", fontSize = 40.sp, modifier = Modifier.offset(310.dp, 60.dp))
        Text("✨", fontSize = 18.sp, modifier = Modifier.offset(60.dp, 220.dp))
        Text("🌟", fontSize = 26.sp, modifier = Modifier.offset(290.dp, 240.dp).scale(starScale * 0.9f))

        // Top ring decoration
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(40.dp, (-40).dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFF0EA5E9).copy(0.2f))
                .border(3.dp, Color.White.copy(0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 64.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Color.White.copy(0.15f)).border(2.dp, Color.White.copy(0.3f), CircleShape)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) { Text("←", fontSize = 20.sp, color = Color.White) }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Telescope illustration
            Text("🔭", fontSize = 90.sp, modifier = Modifier.rotate(telescopeRotate))
            Spacer(modifier = Modifier.height(20.dp))

            Text("Lost in Space?", fontFamily = FredokaOne, fontSize = 36.sp, color = Color.White, textAlign = TextAlign.Center)
            Text("No worries! We'll find your way back 🚀", fontFamily = Fredoka, fontSize = 16.sp,
                color = Color.White.copy(0.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 36.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(0.1f))
                    .border(3.dp, Color.White.copy(0.2f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column {
                    CartoonTextField(value = email, onValueChange = { email = it },
                        placeholder = "Your Email Address", emoji = "📧", keyboardType = KeyboardType.Email)
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color(0xFFFCA5A5), fontFamily = Fredoka, fontSize = 13.sp)
                    }
                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(successMessage!!, color = Color(0xFF86EFAC), fontFamily = Fredoka, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CartoonButton(
                text = "📡  Send Reset Link",
                onClick = {
                    isLoading = true; errorMessage = null
                    scope.launch {
                        val result = authRepository.sendPasswordResetEmail(email)
                        isLoading = false
                        if (result.isSuccess) {
                            successMessage = "Reset email sent! Check your inbox ✅"
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to send email"
                        }
                    }
                },
                isLoading = isLoading,
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))),
                textColor = Color.White,
                borderColor = Color(0xFF0C1445)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Remembered it? ", fontFamily = Fredoka, color = Color(0xFFE9D5FF), fontSize = 15.sp)
                Text("Go Back", fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateBack() })
            }
        }

        // Decorations (on top)
        Text("⭐", fontSize = 24.sp, modifier = Modifier.offset(20.dp, 60.dp).rotate(starRotate), color = Color.White.copy(alpha = 0.4f))
        Text("🚀", fontSize = 56.sp, modifier = Modifier.offset(310.dp, 100.dp).offset(y = rocketY.dp).rotate(-30f))
        Text("✨", fontSize = 18.sp, modifier = Modifier.offset(40.dp, 280.dp), color = Color(0xFFFBBF24).copy(alpha = 0.6f))
    }
}
