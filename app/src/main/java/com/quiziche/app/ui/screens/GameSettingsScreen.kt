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
import androidx.compose.material.icons.filled.Info
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
    initialCategory: String = "all",
    onNavigateBack: () -> Unit,
    onNavigateToNext: (mode: String, category: String) -> Unit
) {
    var gameMode by remember { mutableStateOf("random") }
    var selectedCategory by remember { mutableStateOf(if (initialCategory == "all") "All Categories" else initialCategory.replaceFirstChar { it.uppercase() }) }
    var difficulty by remember { mutableStateOf("Medium") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All Categories", "Science", "History", "Sports", "Art", "Music", "Geography", "Movies", "Literature", "Technology", "Food")

    // Dynamic background colors
    val bgTopColor by animateColorAsState(
        targetValue = when (gameMode) {
            "solo" -> Color(0xFFF0FDF4)
            "random" -> Color(0xFFFDF4FF)
            else -> Color(0xFFEFF6FF)
        },
        label = "bgTop"
    )
    val bgMidColor by animateColorAsState(
        targetValue = when (gameMode) {
            "solo" -> Color(0xFFDCFCE7)
            "random" -> Color(0xFFFAE8FF)
            else -> Color(0xFFDBEAFE)
        },
        label = "bgMid"
    )
    val bgBottomColor by animateColorAsState(
        targetValue = when (gameMode) {
            "solo" -> Color(0xFFBBF7D0)
            "random" -> Color(0xFFF3E8FF)
            else -> Color(0xFFBFDBFE)
        },
        label = "bgBottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTopColor, bgMidColor, bgBottomColor)
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
                    description = "Practice",
                    icon = Icons.Default.PersonAdd,
                    isSelected = gameMode == "solo",
                    activeColor = Color(0xFF22C55E),
                    onClick = { gameMode = "solo" },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "Ranked",
                    description = "Find opponent",
                    icon = Icons.Default.Groups,
                    isSelected = gameMode == "random",
                    activeColor = Color(0xFF9333EA),
                    onClick = { gameMode = "random" },
                    modifier = Modifier.weight(1f)
                )
                ModeCard(
                    title = "Friend",
                    description = "Play together",
                    icon = Icons.Default.PersonAdd,
                    isSelected = gameMode == "friend",
                    activeColor = Color(0xFF3B82F6),
                    onClick = { gameMode = "friend" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Info Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor = when (gameMode) {
                        "solo" -> Color(0xFF22C55E)
                        "random" -> Color(0xFF9333EA)
                        else -> Color(0xFF3B82F6)
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (gameMode) {
                            "solo" -> "Solo mode does not affect your ELO. Play to earn XP and Coins."
                            "random" -> "Ranked Match: Winning increases your ELO. Losing drops it."
                            else -> "Create a room and share the invite code with your friend."
                        },
                        color = Color(0xFF4B5563),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
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
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryDropdownExpanded = true },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
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
                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category, color = Color(0xFF1F2937)) },
                            onClick = {
                                selectedCategory = category
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty (Hidden in Random mode)
            if (gameMode != "random") {
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
                    val activeColor = if (gameMode == "solo") Color(0xFF22C55E) else Color(0xFF3B82F6)
                    listOf("Easy", "Medium", "Hard").forEach { level ->
                        val isSelected = difficulty == level
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { difficulty = level },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) activeColor else Color.White,
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
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Button
            val backendCategory = if (selectedCategory == "All Categories") "all" else selectedCategory
            val buttonGradient = when (gameMode) {
                "solo" -> listOf(Color(0xFF10B981), Color(0xFF059669))
                "random" -> listOf(Color(0xFF9333EA), Color(0xFFDB2777))
                else -> listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
            }
            Button(
                onClick = { onNavigateToNext(gameMode, backendCategory) },
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
                        .background(Brush.horizontalGradient(buttonGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(gameMode) {
                            "solo" -> "Start Solo Practice"
                            "random" -> "Find Opponent"
                            else -> "Create Room & Invite"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (gameMode == "random") {
                Text(
                    text = "Estimated wait time: ~5 seconds",
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
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) activeColor else Color.White,
        label = "color"
    )
    val contentColor by animateColorAsState(
        if (isSelected) Color.White else Color(0xFF1F2937),
        label = "contentColor"
    )
    val iconColor by animateColorAsState(
        if (isSelected) Color.White else activeColor,
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
