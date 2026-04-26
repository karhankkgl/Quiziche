package com.quiziche.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colors: List<Color>
)

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryDetails: (String) -> Unit
) {
    val categories = listOf(
        Category("science", "Science", "🔬", listOf(Color(0xFF60A5FA), Color(0xFF06B6D4))),
        Category("history", "History", "📚", listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
        Category("sports", "Sports", "⚽", listOf(Color(0xFF4ADE80), Color(0xFF10B981))),
        Category("art", "Art", "🎨", listOf(Color(0xFFF472B6), Color(0xFFE11D48))),
        Category("music", "Music", "🎵", listOf(Color(0xFFA78BFA), Color(0xFF6366F1))),
        Category("geography", "Geography", "🌍", listOf(Color(0xFF2DD4BF), Color(0xFF0891B2))),
        Category("movies", "Movies", "🎬", listOf(Color(0xFFF87171), Color(0xFFEC4899))),
        Category("literature", "Literature", "📖", listOf(Color(0xFF818CF8), Color(0xFFA855F7))),
        Category("technology", "Technology", "💻", listOf(Color(0xFF94A3B8), Color(0xFF475569))),
        Category("food", "Food & Drink", "🍕", listOf(Color(0xFFFACC15), Color(0xFFEA580C)))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2FF),
                        Color(0xFFF5F3FF),
                        Color(0xFFFDF2F8)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937)
                    )
                }
                Text(
                    text = "Categories",
                    color = Color(0xFF1F2937),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Subtitle
            Text(
                text = "Choose your favorite topic to master",
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            )

            // Categories Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(categories) { index, category ->
                    CategoryCard(category) {
                        onNavigateToCategoryDetails(category.id)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(category.colors))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = category.icon, fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = category.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
