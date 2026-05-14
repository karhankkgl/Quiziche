package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryDetails: (String) -> Unit,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val categories = listOf(
        Triple("🚀", "Space & Science", "science"),
        Triple("🦁", "Animals & Nature", "animals"),
        Triple("🔭", "Astronomy", "astronomy"),
        Triple("🧬", "Biology", "biology"),
        Triple("🏛️", "History", "history"),
        Triple("🎵", "Music", "music"),
        Triple("⚽", "Sports", "sports"),
        Triple("🎨", "Art & Culture", "art"),
        Triple("🌍", "Geography", "geography"),
        Triple("🎬", "Movies & TV", "movies"),
        Triple("💻", "Technology", "technology"),
        Triple("🍕", "Food & Drink", "food")
    )
    val categoryColors = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFF0EA5E9)),
        listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
        listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6)),
        listOf(Color(0xFF10B981), Color(0xFF0EA5E9)),
        listOf(Color(0xFFEF4444), Color(0xFFF97316)),
        listOf(Color(0xFFEC4899), Color(0xFF7C3AED)),
        listOf(Color(0xFF10B981), Color(0xFFFBBF24)),
        listOf(Color(0xFFEC4899), Color(0xFFF97316)),
        listOf(Color(0xFF14B8A6), Color(0xFF0EA5E9)),
        listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
        listOf(Color(0xFF0EA5E9), Color(0xFF7C3AED)),
        listOf(Color(0xFFF97316), Color(0xFFEC4899))
    )

    val infiniteTransition = rememberInfiniteTransition(label = "cat_anim")
    val rocketY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "r"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗂️  Categories", fontFamily = FredokaOne, fontSize = 28.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pick your battlefield! ⚔️", fontFamily = Fredoka, fontSize = 16.sp, color = Color.White.copy(0.6f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Grid-like layout using rows of 2
            val chunked = categories.chunked(2)
            items(chunked.size) { rowIdx ->
                val row = chunked[rowIdx]
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEachIndexed { col, (emoji, name, id) ->
                        val colorIdx = rowIdx * 2 + col
                        val brush = Brush.linearGradient(categoryColors[colorIdx % categoryColors.size])
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.7f))
                                .clip(RoundedCornerShape(24.dp))
                                .background(brush)
                                .border(1.5.dp, Color.White.copy(0.15f), RoundedCornerShape(24.dp))
                                .clickable { onNavigateToCategoryDetails(id) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(emoji, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(name, fontFamily = FredokaOne, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // Decorations (on top)
        Text("🚀", fontSize = 48.sp, modifier = Modifier.offset(300.dp, 60.dp).offset(y = rocketY.dp).rotate(-25f))
        Text("⭐", fontSize = 22.sp, modifier = Modifier.offset(20.dp, 80.dp), color = Color(0xFFFBBF24))

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
                CartoonNavItem(emoji = "🏠", label = "Home", isSelected = false, onClick = onNavigateToMainMenu, modifier = Modifier.weight(1f))
                CartoonNavItem(emoji = "🗂️", label = "Categories", isSelected = true, onClick = {}, modifier = Modifier.weight(1f))
                CartoonNavItem(emoji = "🏆", label = "Rankings", onClick = onNavigateToLeaderboard, modifier = Modifier.weight(1f))
                CartoonNavItem(emoji = "🦁", label = "Friends", onClick = onNavigateToFriends, modifier = Modifier.weight(1f))
            }
        }
    }
}
