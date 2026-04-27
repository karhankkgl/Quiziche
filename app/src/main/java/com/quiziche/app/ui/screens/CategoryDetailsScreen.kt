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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colors: List<Color>
)

@Composable
fun CategoryDetailsScreen(
    categoryId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGameSettings: () -> Unit
) {
    val categoryData = mapOf(
        "science" to Category("science", "Science", "🔬", listOf(Color(0xFF60A5FA), Color(0xFF06B6D4))),
        "history" to Category("history", "History", "📚", listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
        "sports" to Category("sports", "Sports", "⚽", listOf(Color(0xFF4ADE80), Color(0xFF10B981))),
        "art" to Category("art", "Art", "🎨", listOf(Color(0xFFF472B6), Color(0xFFE11D48))),
        "music" to Category("music", "Music", "🎵", listOf(Color(0xFFA78BFA), Color(0xFF6366F1))),
        "geography" to Category("geography", "Geography", "🌍", listOf(Color(0xFF2DD4BF), Color(0xFF0891B2))),
        "movies" to Category("movies", "Movies", "🎬", listOf(Color(0xFFF87171), Color(0xFFEC4899))),
        "literature" to Category("literature", "Literature", "📖", listOf(Color(0xFF818CF8), Color(0xFFA855F7))),
        "technology" to Category("technology", "Technology", "💻", listOf(Color(0xFF94A3B8), Color(0xFF475569))),
        "food" to Category("food", "Food & Drink", "🍕", listOf(Color(0xFFFACC15), Color(0xFFEA580C)))
    )

    val category = categoryData[categoryId] ?: categoryData["science"]!!

    val topPlayers = listOf(
        Triple(1, "QuizMaster", 2450),
        Triple(2, "BrainBox", 2380),
        Triple(3, "ScienceGuru", 2310),
        Triple(4, "Einstein Jr", 2250),
        Triple(5, "NerdAlert", 2190)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBackButton(onClick = onNavigateBack)
                Spacer(modifier = Modifier.width(14.dp))
                Text("Category Details", fontFamily = FredokaOne, fontSize = 22.sp, color = Color.White)
            }

            // Category Banner
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp), spotColor = category.colors[0].copy(0.4f))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(category.colors))
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(category.icon, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(category.name, fontFamily = FredokaOne, color = Color.White, fontSize = 26.sp)
                    Text("Test your ${category.name.lowercase()} knowledge!",
                        fontFamily = Fredoka, color = Color.White.copy(0.85f), fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }

            // Statistics Card
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("📈  Your Statistics", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CartoonStatChip("18", "Played", Brush.linearGradient(category.colors))
                        CartoonStatChip("70%", "Win Rate", Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9))))
                        CartoonStatChip("#12", "Rank", Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Category Mastery", fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape)
                        .background(Color(0xFFF3F4F6))) {
                        Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().clip(CircleShape)
                            .background(Brush.horizontalGradient(category.colors)))
                    }
                    Text("70%", fontFamily = FredokaOne, color = category.colors[0], fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                }
            }

            // Leaderboard Section
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("🏆  Top Players", fontFamily = FredokaOne, color = Color(0xFF1E1B4B), fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF1E1B4B).copy(0.3f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(24.dp))
                ) {
                    Column {
                        topPlayers.forEachIndexed { index, player ->
                            LeaderboardRow(rank = player.first, name = player.second, score = player.third, isLast = index == topPlayers.size - 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), spotColor = category.colors[0].copy(0.4f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(category.colors))
                    .border(3.dp, Color(0xFF1E1B4B), RoundedCornerShape(22.dp))
                    .clickable { onNavigateToGameSettings() }
                    .height(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${category.icon}  Play in ${category.name}!", fontFamily = FredokaOne,
                    color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CartoonStatChip(value: String, label: String, brush: Brush) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(brush)
            .border(2.dp, Color(0xFF1E1B4B), CircleShape), contentAlignment = Alignment.Center) {
            Text(value, fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontFamily = Fredoka, color = Color(0xFF6B7280), fontSize = 12.sp)
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontFamily = FredokaOne, fontSize = 26.sp)
        Text(text = label, color = Color(0xFF6B7280), fontFamily = Fredoka, fontSize = 12.sp)
    }
}

@Composable
fun LeaderboardRow(rank: Int, name: String, score: Int, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val rankColor = when (rank) {
            1 -> Color(0xFFFEF9C3) to Color(0xFFCA8A04)
            2 -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
            3 -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
            else -> Color(0xFFF9FAFB) to Color(0xFF9CA3AF)
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(rankColor.first),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = rankColor.second,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFFF472B6)))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = Color(0xFF1F2937), fontWeight = FontWeight.SemiBold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFEAB308),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = score.toString(), color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
        }
    }
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color(0xFFF3F4F6)
        )
    }
}
