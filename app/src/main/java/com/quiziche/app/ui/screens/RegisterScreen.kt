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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.repository.AuthRepository
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMainMenu: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "reg_anim")
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "s"
    )
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "b"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0A2E), Color(0xFF4C1D95), Color(0xFF7C3AED))))
    ) {
        // Big decorative planet at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(50.dp, 50.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFEC4899), Color(0xFF7C3AED))))
                .border(4.dp, Color(0xFFFBBF24).copy(0.3f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ModernBackButton(onClick = onNavigateBack)
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEC4899))))
                    .border(4.dp, Color(0xFF475569), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⭐", fontSize = 48.sp, modifier = Modifier.offset(y = bounce.dp * 0.5f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Join the Universe!", fontFamily = FredokaOne, fontSize = 36.sp, color = Color.White, textAlign = TextAlign.Center)
            Text("Create your Quiziche account 🚀", fontFamily = Fredoka, fontSize = 16.sp, color = Color(0xFFE9D5FF), modifier = Modifier.padding(bottom = 28.dp))

            // Form card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(0.08f))
                    .border(3.dp, Color.White.copy(0.15f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CartoonTextField(value = fullName, onValueChange = { fullName = it }, placeholder = "Full Name", emoji = "👤")
                    CartoonTextField(value = email, onValueChange = { email = it }, placeholder = "Email Address", emoji = "📧", keyboardType = KeyboardType.Email)
                    CartoonTextField(value = password, onValueChange = { password = it }, placeholder = "Password", emoji = "🔒", isPassword = true)
                    CartoonTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Confirm Password", emoji = "🔑", isPassword = true)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = agreeToTerms,
                            onCheckedChange = { agreeToTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFBBF24), uncheckedColor = Color.White.copy(0.5f))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I agree to Terms & Privacy Policy", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage != null) {
                Text(errorMessage!!, color = Color(0xFFFCA5A5), fontFamily = Fredoka, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(modifier = Modifier.height(18.dp))

            CartoonButton(
                text = "⭐  Create Account",
                onClick = {
                    if (password != confirmPassword) { errorMessage = "Passwords do not match"; return@CartoonButton }
                    if (!agreeToTerms) { errorMessage = "Please agree to the terms"; return@CartoonButton }
                    isLoading = true; errorMessage = null
                    scope.launch {
                        val result = authRepository.register(email, password, fullName)
                        isLoading = false
                        if (result.isSuccess) onNavigateToMainMenu()
                        else errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                    }
                },
                isLoading = isLoading,
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFFBBF24))),
                textColor = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Already a star? ", fontFamily = Fredoka, color = Color(0xFFE9D5FF), fontSize = 15.sp)
                Text("Log In", fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateBack() })
            }
        }

        // Decorations (on top)
        Text("⭐", fontSize = 20.sp, modifier = Modifier.offset(30.dp, 70.dp).rotate(starRotate).scale(0.8f))
        Text("🌙", fontSize = 36.sp, modifier = Modifier.offset(300.dp, 50.dp).offset(y = bounce.dp))
        Text("✨", fontSize = 24.sp, modifier = Modifier.offset(15.dp, 280.dp))
        Text("🌟", fontSize = 18.sp, modifier = Modifier.offset(310.dp, 320.dp).rotate(-starRotate))
    }
}
