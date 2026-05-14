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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*

@Composable
fun MatchmakingScreen(
    category: String = "all",
    difficulty: String = "Any",
    onNavigateToGame: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val gameRepository = remember { com.quiziche.app.data.repository.GameRepository() }
    val scope = rememberCoroutineScope()
    var isSearching by remember { mutableStateOf(true) }
    val isConnected by gameRepository.observeConnectionState().collectAsState(initial = false)
    val currentUid = gameRepository.currentUid ?: "Not Logged In"

    LaunchedEffect(Unit) {
        gameRepository.joinMatchmaking(category = category, difficulty = difficulty) { roomId ->
            isSearching = false
            onNavigateToGame(roomId)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mm_anim")
    val ufoRotate by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "u"
    )
    val ufoY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -16f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "uy"
    )
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "r"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "ra"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "sr"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0020), Color(0xFF1A0A2E), Color(0xFF2D1B69))))
    ) {
        // Star field background
        val stars = remember { listOf(
            Pair(40f, 80f), Pair(300f, 50f), Pair(160f, 120f),
            Pair(60f, 250f), Pair(320f, 300f), Pair(20f, 420f),
            Pair(250f, 160f), Pair(340f, 220f), Pair(100f, 360f)
        )}
        stars.forEachIndexed { i, (x, y) ->
            Text(if (i % 2 == 0) "⭐" else "✨",
                fontSize = (10 + (i % 3) * 4).sp,
                modifier = Modifier.offset(x.dp, y.dp).rotate(starRotate * (if (i % 2 == 0) 1f else -0.5f)).scale(0.7f + (i % 3) * 0.2f),
                color = Color.White.copy(0.3f + (i % 3) * 0.1f))
        }

        // Saturn-like planet decoration
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(60.dp, (-30).dp)) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF7C3AED), Color(0xFF1A0A2E)))))
            Box(modifier = Modifier.width(170.dp).height(30.dp).align(Alignment.Center)
                .rotate(-20f).clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFFBBF24).copy(0.3f))
                .border(2.dp, Color(0xFFFBBF24).copy(0.4f), RoundedCornerShape(50.dp)))
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp).padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // UFO with radar rings
            Box(contentAlignment = Alignment.Center) {
                // Radar rings
                Box(modifier = Modifier.size((120 * radarScale).dp).clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(radarAlpha * 0.5f)))
                Box(modifier = Modifier.size((80 * radarScale).dp).clip(CircleShape)
                    .background(Color(0xFF0EA5E9).copy(radarAlpha * 0.4f)))
                // UFO
                Text("🛸", fontSize = 90.sp, modifier = Modifier.offset(y = ufoY.dp).rotate(ufoRotate))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                if (isSearching) "Scanning Galaxy..." else "Found!",
                fontFamily = FredokaOne, fontSize = 34.sp, color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Category: ${category.replaceFirstChar { it.uppercase() }}\nDifficulty: $difficulty",
                fontFamily = Fredoka, fontSize = 16.sp, color = Color(0xFFC4B5FD), textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isSearching) {
                // Dots animation
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { i ->
                        val dotScale by infiniteTransition.animateFloat(
                            initialValue = 0.6f, targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(tween(600, delayMillis = i * 200), RepeatMode.Reverse),
                            label = "dot$i"
                        )
                        Box(modifier = Modifier.size(14.dp).scale(dotScale).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEC4899)))))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Queue stat card
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(0.08f))
                        .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("🚀  In Queue", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌍", fontSize = 32.sp)
                                Text("Online", fontFamily = Fredoka, color = Color(0xFFC4B5FD), fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚡", fontSize = 32.sp)
                                Text("Live", fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎯", fontSize = 32.sp)
                                Text("Matching", fontFamily = Fredoka, color = Color(0xFF86EFAC), fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // padding for the fixed button at the bottom
            }

            Spacer(modifier = Modifier.weight(1f))

            // Debug overlay
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(0.4f)).padding(10.dp)
            ) {
                Column {
                    Text("UID: ${currentUid.take(8)}...", color = Color.White.copy(0.7f), fontSize = 10.sp, fontFamily = Fredoka)
                    Text(
                        "Firebase: ${if (isConnected) "Connected ✅" else "Disconnected ❌"}",
                        color = if (isConnected) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                        fontSize = 10.sp, fontFamily = Fredoka
                    )
                }
            }
        }

        // Fixed Cancel button
        if (isSearching) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 28.dp, vertical = 24.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), spotColor = Color(0xFFEF4444).copy(0.4f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEF4444).copy(0.9f))
                    .border(3.dp, Color(0xFF7F1D1D), RoundedCornerShape(18.dp))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕  Cancel Search", fontFamily = FredokaOne, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
