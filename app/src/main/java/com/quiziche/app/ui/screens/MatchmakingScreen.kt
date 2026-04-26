package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchmakingScreen(
    onNavigateToGame: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val gameRepository = remember { GameRepository() }
    val scope = rememberCoroutineScope()
    var isSearching by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        gameRepository.joinMatchmaking(category = "General") { roomId ->
            isSearching = false
            onNavigateToGame()
        }
        delay(3000)
        if (isSearching) {
            isSearching = false
            gameRepository.cancelMatchmaking("General")
            onNavigateToGame()
        }
    }
    
    // Cleanup on dispose (if user navigates away before match found)
    DisposableEffect(Unit) {
        onDispose {
            if (isSearching) {
                gameRepository.cancelMatchmaking("General")
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing)
        ),
        label = "ring1Scale"
    )
    val ring1Opacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing)
        ),
        label = "ring1Opacity"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            initialStartOffset = StartOffset(500)
        ),
        label = "ring2Scale"
    )
    val ring2Opacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            initialStartOffset = StartOffset(500)
        ),
        label = "ring2Opacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        QuizichePurple600,
                        QuizichePurple700,
                        QuizicheIndigo800
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Cancel Button
        IconButton(
            onClick = { 
                isSearching = false
                gameRepository.cancelMatchmaking("General")
                onNavigateBack() 
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radar Animation
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                // Rings
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ring1Scale)
                        .border(4.dp, Color.White.copy(alpha = ring1Opacity), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ring2Scale)
                        .border(4.dp, Color.White.copy(alpha = ring2Opacity), CircleShape)
                )

                // Central Avatar
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎮", fontSize = 72.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Status Text
            val statusOpacity by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(750),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "statusOpacity"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Searching for opponent...",
                    color = Color.White.copy(alpha = statusOpacity),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This should only take a moment",
                    color = QuizichePurple200,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Indicator
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                val progressOffset by infiniteTransition.animateFloat(
                    initialValue = -1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing)
                    ),
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .offset(x = 200.dp * (progressOffset + 0.5f))
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Tip Card
            Box(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Quick Tip",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Did you know? The category with the highest win rate is Science!",
                            color = QuizichePurple200,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Bouncing Dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                val dotY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(i * 150)
                    ),
                    label = "dot$i"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .offset(y = dotY.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
