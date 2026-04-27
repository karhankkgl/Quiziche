package com.quiziche.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.quiziche.app.R
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import com.quiziche.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToMainMenu: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID")
            .requestEmail().build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isLoading = true
                scope.launch {
                    val authResult = authRepository.signInWithGoogle(idToken)
                    isLoading = false
                    if (authResult.isSuccess) onNavigateToMainMenu()
                    else errorMessage = authResult.exceptionOrNull()?.message ?: "Google login failed"
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Google sign in failed: ${e.statusCode}"
        }
    }

    // Floating star animation
    val infiniteTransition = rememberInfiniteTransition(label = "login_anim")
    val rocketY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -18f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rocket"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "star"
    )
    val star2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "star2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0A2E), Color(0xFF2D1B69), Color(0xFF4C1D95))))
    ) {
        // Planet bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-40).dp, 60.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF7C3AED), Color(0xFF1A0A2E))))
                .border(4.dp, Color(0xFFFBBF24).copy(alpha = 0.4f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                    .border(4.dp, Color(0xFF1E1B4B), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🧠", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Quiziche",
                fontFamily = FredokaOne,
                color = Color.White,
                fontSize = 44.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Welcome Back, Explorer! 👋",
                fontFamily = Fredoka,
                color = Color(0xFFE9D5FF),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // Card form
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(3.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column {
                    CartoonTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email Address",
                        emoji = "📧",
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    CartoonTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        emoji = "🔒",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Forgot password?",
                            fontFamily = Fredoka,
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color(0xFFFCA5A5), fontSize = 13.sp,
                    fontFamily = Fredoka, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Login Button - cartoon style
            CartoonButton(
                text = "🚀  Launch In!",
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = authRepository.login(email, password)
                        isLoading = false
                        if (result.isSuccess) onNavigateToMainMenu()
                        else errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                    }
                },
                isLoading = isLoading,
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                textColor = Color(0xFF1E1B4B)
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(0.2f)))
                Text("  or  ", color = Color.White.copy(0.5f), fontFamily = Fredoka, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(1.dp).weight(1f).background(Color.White.copy(0.2f)))
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Google button
            CartoonButton(
                text = "🌐  Sign in with Google",
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                isLoading = false,
                bgBrush = Brush.horizontalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.1f))),
                textColor = Color.White,
                borderColor = Color.White.copy(0.3f)
            )

            Spacer(modifier = Modifier.height(28.dp))
            Row {
                Text("New to the galaxy? ", fontFamily = Fredoka, color = Color(0xFFE9D5FF), fontSize = 15.sp)
                Text(
                    text = "Register Now",
                    fontFamily = Fredoka,
                    color = Color(0xFFFBBF24),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }

        // Decorations (on top)
        Text("🚀", fontSize = 56.sp, modifier = Modifier.offset(280.dp, 80.dp).rotate(-20f).offset(y = rocketY.dp))
        Text("⭐", fontSize = 24.sp, modifier = Modifier.offset(30.dp, 120.dp).rotate(starRotate))
        Text("✨", fontSize = 18.sp, modifier = Modifier.offset(320.dp, 280.dp).scale(0.8f))
        Text("🌟", fontSize = 20.sp, modifier = Modifier.offset(40.dp, 350.dp).rotate(-starRotate))
    }
}


