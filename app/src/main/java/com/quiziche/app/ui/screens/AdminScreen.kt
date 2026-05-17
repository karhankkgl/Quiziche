package com.quiziche.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.quiziche.app.data.repository.AiQuestionRepository
import com.quiziche.app.ui.components.CartoonButton
import com.quiziche.app.ui.components.ModernBackButton
import com.quiziche.app.ui.theme.Fredoka
import com.quiziche.app.ui.theme.FredokaOne
import kotlinx.coroutines.launch

data class BulkCategoryStatus(
    val name: String,
    val state: CategoryState = CategoryState.WAITING,
    val savedCount: Int = 0
)

enum class CategoryState { WAITING, IN_PROGRESS, SUCCESS, FAILED }

@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val aiRepository = remember { AiQuestionRepository() }
    val quizRepository = remember { com.quiziche.app.data.repository.QuizRepository() }

    var isWipingMocks by remember { mutableStateOf(false) }
    var wipeResultMessage by remember { mutableStateOf<String?>(null) }

    val allCategories = listOf(
        "Science", "History", "Sports", "Art", "Music",
        "Geography", "Movies", "Literature", "Technology", "Food"
    )

    // Single generate state
    var selectedCategory by remember { mutableStateOf("Science") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedCount by remember { mutableStateOf(5) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var difficultyDropdownExpanded by remember { mutableStateOf(false) }
    var countDropdownExpanded by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Bulk generate state
    var isBulkRunning by remember { mutableStateOf(false) }
    var bulkDone by remember { mutableStateOf(false) }
    var bulkStatuses by remember {
        mutableStateOf(allCategories.map { BulkCategoryStatus(it) })
    }
    var bulkTotalSaved by remember { mutableStateOf(0) }

    val difficulties = listOf("Easy", "Medium", "Hard")
    val counts = listOf(3, 5, 10, 15, 20)

    val infiniteTransition = rememberInfiniteTransition(label = "admin_anim")
    val sparkleRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "sparkle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D001A), Color(0xFF1A0035), Color(0xFF0D0020))))
    ) {
        // Decorative circles
        Box(modifier = Modifier.offset((-60).dp, (-60).dp).size(220.dp).clip(CircleShape).background(Color(0xFF7C3AED).copy(0.12f)))
        Box(modifier = Modifier.offset(260.dp, 50.dp).size(160.dp).clip(CircleShape).background(Color(0xFFEC4899).copy(0.08f)))
        Box(modifier = Modifier.offset((-30).dp, 500.dp).size(180.dp).clip(CircleShape).background(Color(0xFF06B6D4).copy(0.07f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 52.dp, bottom = 40.dp)
        ) {

            // ===== HEADER =====
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ModernBackButton(onClick = onNavigateBack)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("AI Question Generator", fontFamily = FredokaOne, fontSize = 22.sp, color = Color.White)
                    Text("✨ Admin Panel", fontFamily = Fredoka, fontSize = 13.sp, color = Color(0xFFA78BFA))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== AI ICON CARD =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF7C3AED), Color(0xFFEC4899))))
                    .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✨", fontSize = 56.sp, modifier = Modifier.rotate(sparkleRotate))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Powered by Gemini AI", fontFamily = FredokaOne, fontSize = 18.sp, color = Color.White)
                    Text(
                        "Generate quiz questions and save them to Firestore",
                        fontFamily = Fredoka, fontSize = 13.sp, color = Color.White.copy(0.75f),
                        textAlign = TextAlign.Center, lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== BULK SEED SECTION =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0D1A40).copy(0.9f))
                    .border(2.dp, Color(0xFF06B6D4).copy(0.5f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Bulk Seed All Categories", fontFamily = FredokaOne, color = Color.White, fontSize = 17.sp)
                            Text(
                                "20 Medium questions × 10 categories = 200 questions",
                                fontFamily = Fredoka, color = Color(0xFF67E8F9), fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category status grid
                    val chunked = bulkStatuses.chunked(2)
                    chunked.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { status ->
                                BulkCategoryChip(status = status, modifier = Modifier.weight(1f))
                            }
                            // Fill empty cell if odd
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bulk progress bar
                    if (isBulkRunning || bulkDone) {
                        val completedCount = bulkStatuses.count { it.state == CategoryState.SUCCESS || it.state == CategoryState.FAILED }
                        val progressFraction = if (allCategories.isEmpty()) 0f else completedCount.toFloat() / allCategories.size
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    if (isBulkRunning) "Generating... $completedCount/${allCategories.size}" else "Completed!",
                                    fontFamily = Fredoka, color = Color(0xFF67E8F9), fontSize = 13.sp
                                )
                                Text("$bulkTotalSaved saved", fontFamily = FredokaOne, color = Color.White, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(10.dp)
                                    .clip(CircleShape).background(Color.White.copy(0.1f))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(progressFraction).fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF7C3AED))))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    CartoonButton(
                        text = when {
                            isBulkRunning -> "🔄  Generating All Categories..."
                            bulkDone -> "✅  Done! Tap to Re-seed"
                            else -> "🚀  Bulk Seed: 20 × 10 Categories"
                        },
                        isLoading = isBulkRunning,
                        onClick = {
                            if (!isBulkRunning) {
                                isBulkRunning = true
                                bulkDone = false
                                bulkTotalSaved = 0
                                bulkStatuses = allCategories.map { BulkCategoryStatus(it) }
                                scope.launch {
                                    aiRepository.generateForAllCategories(
                                        difficulty = "Medium",
                                        countPerCategory = 20,
                                        onProgress = { category, done, total, success ->
                                            bulkStatuses = bulkStatuses.map { status ->
                                                when {
                                                    status.name == category -> status.copy(
                                                        state = if (success) CategoryState.SUCCESS else CategoryState.FAILED,
                                                        savedCount = if (success) 20 else 0
                                                    )
                                                    // Mark next one as in progress
                                                    done < total && allCategories.indexOf(status.name) == done -> status.copy(state = CategoryState.IN_PROGRESS)
                                                    else -> status
                                                }
                                            }
                                            if (success) bulkTotalSaved += 20
                                        }
                                    )
                                    isBulkRunning = false
                                    bulkDone = true
                                }
                            }
                        },
                        bgBrush = Brush.linearGradient(listOf(Color(0xFF0369A1), Color(0xFF06B6D4))),
                        textColor = Color.White,
                        borderColor = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== DIVIDER =====
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.15f))
                Text("  or generate single category  ", fontFamily = Fredoka, color = Color.White.copy(0.4f), fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.15f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== SINGLE CATEGORY SECTION =====
            Text("🗂️  Category", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 10.dp))
            AdminDropdown(
                value = selectedCategory, expanded = categoryDropdownExpanded,
                onToggle = { categoryDropdownExpanded = !categoryDropdownExpanded },
                onDismiss = { categoryDropdownExpanded = false },
                items = allCategories,
                onSelect = { selectedCategory = it; categoryDropdownExpanded = false }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("📈  Difficulty", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 10.dp))
            AdminDropdown(
                value = selectedDifficulty, expanded = difficultyDropdownExpanded,
                onToggle = { difficultyDropdownExpanded = !difficultyDropdownExpanded },
                onDismiss = { difficultyDropdownExpanded = false },
                items = difficulties,
                onSelect = { selectedDifficulty = it; difficultyDropdownExpanded = false }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("🔢  Number of Questions", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 10.dp))
            AdminDropdown(
                value = selectedCount.toString(), expanded = countDropdownExpanded,
                onToggle = { countDropdownExpanded = !countDropdownExpanded },
                onDismiss = { countDropdownExpanded = false },
                items = counts.map { it.toString() },
                onSelect = { selectedCount = it.toInt(); countDropdownExpanded = false }
            )

            Spacer(modifier = Modifier.height(28.dp))

            CartoonButton(
                text = if (isGenerating) "Generating..." else "✨  Generate with AI",
                isLoading = isGenerating,
                onClick = {
                    if (!isGenerating) {
                        isGenerating = true
                        resultMessage = null
                        scope.launch {
                            val result = aiRepository.generateAndSaveQuestions(
                                category = selectedCategory,
                                difficulty = selectedDifficulty,
                                count = selectedCount
                            )
                            isGenerating = false
                            if (result.isSuccess) {
                                isSuccess = true
                                resultMessage = "✅ ${result.getOrDefault(emptyList()).size} questions saved!"
                            } else {
                                isSuccess = false
                                resultMessage = "❌ Error: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                            }
                        }
                    }
                },
                bgBrush = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))),
                textColor = Color.White,
                borderColor = Color(0xFFA78BFA)
            )

            AnimatedVisibility(visible = resultMessage != null, enter = fadeIn(), exit = fadeOut()) {
                resultMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSuccess) Color(0xFF064E3B).copy(0.8f) else Color(0xFF7F1D1D).copy(0.8f))
                            .border(2.dp, if (isSuccess) Color(0xFF34D399) else Color(0xFFF87171), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(msg, fontFamily = Fredoka, color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== WIPE MOCK QUESTIONS BUTTON =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2D0A14).copy(0.9f))
                    .border(2.dp, Color(0xFFEF4444).copy(0.5f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "🗑️ Database Cleanup",
                        fontFamily = FredokaOne,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Delete all seeded mock questions (keeping only AI-generated ones)",
                        fontFamily = Fredoka,
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    CartoonButton(
                        text = if (isWipingMocks) "🧹 Cleaning Database..." else "Wipe Non-AI Mock Questions",
                        isLoading = isWipingMocks,
                        onClick = {
                            if (!isWipingMocks) {
                                isWipingMocks = true
                                wipeResultMessage = null
                                scope.launch {
                                    val result = quizRepository.deleteMockQuestions()
                                    isWipingMocks = false
                                    if (result.isSuccess) {
                                        wipeResultMessage = "Successfully deleted ${result.getOrNull()} mock questions!"
                                    } else {
                                        wipeResultMessage = "Error: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        },
                        bgBrush = Brush.linearGradient(listOf(Color(0xFF991B1B), Color(0xFFEF4444))),
                        textColor = Color.White,
                        borderColor = Color(0xFFFCA5A5)
                    )

                    AnimatedVisibility(visible = wipeResultMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        wipeResultMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = msg,
                                fontFamily = FredokaOne,
                                color = if (msg.startsWith("Success")) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== INFO CARD =====
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(0.05f))
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ℹ️ How it works", fontFamily = FredokaOne, color = Color(0xFFA78BFA), fontSize = 15.sp)
                    Text(
                        "• Gemini AI generates questions in JSON format\n" +
                        "• Questions are saved with isAiGenerated = true\n" +
                        "• Players see ✨ AI badge during gameplay\n" +
                        "• Bulk seed: 1.5s delay between categories (rate limiting)",
                        fontFamily = Fredoka, color = Color.White.copy(0.7f), fontSize = 13.sp, lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BulkCategoryChip(status: BulkCategoryStatus, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, emoji) = when (status.state) {
        CategoryState.WAITING -> Triple(Color.White.copy(0.05f), Color.White.copy(0.1f), "⏳")
        CategoryState.IN_PROGRESS -> Triple(Color(0xFF1E3A5F), Color(0xFF38BDF8), "🔄")
        CategoryState.SUCCESS -> Triple(Color(0xFF064E3B).copy(0.6f), Color(0xFF34D399), "✅")
        CategoryState.FAILED -> Triple(Color(0xFF7F1D1D).copy(0.6f), Color(0xFFF87171), "❌")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(status.name, fontFamily = FredokaOne, color = Color.White, fontSize = 11.sp, maxLines = 1)
                if (status.state == CategoryState.SUCCESS) {
                    Text("${status.savedCount} saved", fontFamily = Fredoka, color = Color(0xFF6EE7B7), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminDropdown(
    value: String, expanded: Boolean,
    onToggle: () -> Unit, onDismiss: () -> Unit,
    items: List<String>, onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black.copy(0.6f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1040))
            .border(1.5.dp, Color(0xFF7C3AED).copy(0.5f), RoundedCornerShape(18.dp))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFFA78BFA))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, modifier = Modifier.background(Color(0xFF1E1040))) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, fontFamily = Fredoka, color = if (item == value) Color(0xFFA78BFA) else Color.White, fontSize = 15.sp) },
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}
