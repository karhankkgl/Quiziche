package com.quiziche.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*

@Composable
fun GameSettingsScreen(
    initialCategory: String = "all",
    onNavigateBack: () -> Unit,
    onNavigateToNext: (mode: String, category: String) -> Unit
) {
    var gameMode by remember { mutableStateOf("random") }
    var selectedCategory by remember { mutableStateOf(if (initialCategory == "all") "All Categories" else initialCategory.replaceFirstChar { it.uppercase() }) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("All Categories", "Science", "History", "Sports", "Art", "Music", "Geography", "Movies", "Literature", "Technology", "Food")

    val infiniteTransition = rememberInfiniteTransition(label = "settings_anim")
    val controllerRotate by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "c"
    )
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "b"
    )

    val modeData = listOf(
        Triple("solo", "🦓 Solo", "Practice alone"),
        Triple("random", "⚡ Ranked", "Find opponent"),
        Triple("friend", "🦁 Friend", "Play together")
    )
    val modeColors = mapOf(
        "solo" to Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9))),
        "random" to Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))),
        "friend" to Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFFBBF24)))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        // Circle decoration top-left
        Box(
            modifier = Modifier.offset((-50).dp, (-50).dp).size(200.dp)
                .clip(CircleShape).background(Color(0xFF7C3AED).copy(0.1f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 52.dp, bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBackButton(onClick = onNavigateBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Game Setup 🎯", fontFamily = FredokaOne, fontSize = 26.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Mode Section Label
            Text("🎮  Choose Mode", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 14.dp))

            // Mode cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                modeData.forEach { (mode, label, desc) ->
                    val isSelected = gameMode == mode
                    val brush = modeColors[mode] ?: Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .then(if (isSelected) Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.8f)) else Modifier)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isSelected) brush else Brush.linearGradient(listOf(Color.White.copy(0.05f), Color.White.copy(0.05f))))
                            .border(1.5.dp, if (isSelected) Color.White.copy(0.3f) else Color.White.copy(0.1f), RoundedCornerShape(22.dp))
                            .clickable { gameMode = mode }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, fontFamily = FredokaOne, fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, fontFamily = Fredoka, fontSize = 11.sp,
                                color = if (isSelected) Color.White.copy(0.8f) else Color(0xFF6B7280),
                                textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info banner
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(0.7f))
                    .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(when (gameMode) {
                        "solo" -> "🦓"
                        "random" -> "⚡"
                        else -> "🦁"
                    }, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (gameMode) {
                            "solo" -> "Solo mode doesn't affect ELO. Earn XP and Coins!"
                            "random" -> "Ranked Match: Winning raises your ELO. Losing drops it."
                            else -> "Create a room and share the invite code with your friend."
                        },
                        fontFamily = Fredoka, color = Color(0xFF4B5563), fontSize = 14.sp, lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category
            Text("🗂️  Pick Category", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black.copy(0.6f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(18.dp))
                    .clickable { categoryDropdownExpanded = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedCategory, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF7C3AED))
                }
                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category, fontFamily = Fredoka, color = Color(0xFF475569)) },
                            onClick = { selectedCategory = category; categoryDropdownExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Start button
            val backendCategory = if (selectedCategory == "All Categories") "all" else selectedCategory
            val btnLabel = when (gameMode) {
                "solo" -> "🦓  Start Solo!"
                "random" -> "⚡  Find Opponent!"
                else -> "🦁  Create Room!"
            }
            val btnBrush = modeColors[gameMode] ?: Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))

            CartoonButton(text = btnLabel, onClick = { onNavigateToNext(gameMode, backendCategory) },
                bgBrush = btnBrush, textColor = Color.White, borderColor = Color(0xFF475569))

            if (gameMode == "random") {
                Spacer(modifier = Modifier.height(12.dp))
                Text("⏱️ Estimated wait: ~5 seconds", fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 13.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        // Decorations (on top)
        Text("⭐", fontSize = 24.sp, modifier = Modifier.offset(50.dp, 70.dp), color = Color(0xFFFBBF24))
        Text("✨", fontSize = 18.sp, modifier = Modifier.offset(280.dp, 180.dp))
    }
}
