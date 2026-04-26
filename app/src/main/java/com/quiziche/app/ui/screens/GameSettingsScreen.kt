package com.quiziche.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.*

@Composable
fun GameSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNext: (String) -> Unit
) {
    var gameMode by remember { mutableStateOf("random") }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var difficulty by remember { mutableStateOf("Medium") }

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
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
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
                    text = "Game Setup",
                    color = Color(0xFF1F2937),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Game Mode Selection
            Text(
                text = "Game Mode",
                color = Color(0xFF1F2937),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeCard(
                    title = "Solo",
                    description = "Play offline",
                    icon = Icons.Default.PersonAdd, // will change icon later if needed
                    isSelected = gameMode == "solo",
                    onClick = { gameMode = "solo" },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "Random",
                    description = "Find opponent",
                    icon = Icons.Default.Groups,
                    isSelected = gameMode == "random",
                    onClick = { gameMode = "random" },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "Friend",
                    description = "Invite player",
                    icon = Icons.Default.PersonAdd,
                    isSelected = gameMode == "friend",
                    onClick = { gameMode = "friend" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Category Filter
            Text(
                text = "Category Filter",
                color = Color(0xFF1F2937),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { /* TODO: Show dialog */ },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedCategory, color = Color(0xFF1F2937))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty
            Text(
                text = "Difficulty",
                color = Color(0xFF1F2937),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Easy", "Medium", "Hard").forEach { level ->
                    val isSelected = difficulty == level
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { difficulty = level },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF9333EA) else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                color = if (isSelected) Color.White else Color(0xFF1F2937),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Button
            Button(
                onClick = { onNavigateToNext(gameMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(gameMode) {
                            "solo" -> "Play Solo"
                            "random" -> "Find Opponent"
                            else -> "Invite Friend"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (gameMode == "random") {
                Text(
                    text = "We'll match you with a player of similar skill",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) Color(0xFF9333EA) else Color.White,
        label = "color"
    )
    val contentColor by animateColorAsState(
        if (isSelected) Color.White else Color(0xFF1F2937),
        label = "contentColor"
    )
    val iconColor by animateColorAsState(
        if (isSelected) Color.White else Color(0xFF9333EA),
        label = "iconColor"
    )

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        color = backgroundColor,
        border = if (isSelected) null else BorderStroke(2.dp, Color(0xFFE5E7EB)),
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
