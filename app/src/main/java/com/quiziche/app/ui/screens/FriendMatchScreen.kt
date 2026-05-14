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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.data.repository.GameRepository
import com.quiziche.app.ui.theme.*
import com.quiziche.app.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Composable
fun FriendMatchScreen(
    category: String = "all",
    difficulty: String = "Any",
    onNavigateBack: () -> Unit,
    onNavigateToGame: (String) -> Unit
) {
    val gameRepository = remember { GameRepository() }
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf<String?>(null) } // null = choose, "host", "join"
    var roomCode by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isWaiting by remember { mutableStateOf(false) }

    // Room creation category/difficulty (selected on this screen)
    val categories = listOf("All Categories", "Science", "History", "Sports", "Art", "Music", "Geography", "Movies", "Literature", "Technology", "Food")
    val difficulties = listOf("Any", "Easy", "Medium", "Hard")
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedDifficulty by remember { mutableStateOf("Any") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var difficultyDropdownExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "friend_anim")
    val lionBounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "l"
    )
    val starRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "s"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E0D3B), Color(0xFF0F172A))))
    ) {
        // Purple glow top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(60.dp, (-60).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED).copy(0.15f))
        )
        // Orange glow bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-40).dp, 40.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFFF97316).copy(0.1f))
        )

        // Decorations
        Text("⭐", fontSize = 18.sp, modifier = Modifier.offset(24.dp, 90.dp).rotate(starRotate), color = Color(0xFFFBBF24).copy(0.5f))
        Text("✨", fontSize = 14.sp, modifier = Modifier.offset(320.dp, 130.dp).rotate(-starRotate).scale(0.85f), color = Color(0xFFA78BFA).copy(0.4f))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp).padding(top = 56.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back
            Row(modifier = Modifier.fillMaxWidth()) {
                ModernBackButton(onClick = { if (mode != null) mode = null else onNavigateBack() })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lion illustration
            Text("🦁", fontSize = 90.sp, modifier = Modifier.offset(y = lionBounce.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Friend Battle!", fontFamily = FredokaOne, fontSize = 36.sp, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Create a room & share your code,\nor join a friend's room!",
                fontFamily = Fredoka, fontSize = 15.sp, color = Color.White.copy(0.5f), textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            when (mode) {
                null -> {
                    // Choose mode
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // HOST
                        Box {
                            Box(modifier = Modifier.fillMaxWidth().offset(5.dp, 6.dp).height(90.dp)
                                .clip(RoundedCornerShape(24.dp)).background(Color(0xFF1E293B)))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(90.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFFBBF24))))
                                    .border(3.dp, Color.White.copy(0.2f), RoundedCornerShape(24.dp))
                                    .clickable { mode = "host" }
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏠", fontSize = 44.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Create Room", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                                        Text("Get a code & invite friend", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        // JOIN
                        Box {
                            Box(modifier = Modifier.fillMaxWidth().offset(5.dp, 6.dp).height(90.dp)
                                .clip(RoundedCornerShape(24.dp)).background(Color(0xFF1E293B)))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(90.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))))
                                    .border(3.dp, Color.White.copy(0.2f), RoundedCornerShape(24.dp))
                                    .clickable { mode = "join" }
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔑", fontSize = 44.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Enter Code", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                                        Text("Join with room code", fontFamily = Fredoka, color = Color.White.copy(0.8f), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "host" -> {
                    if (generatedCode == null) {
                        // === Category selection ===
                        Text("🗂️  Pick Category", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.6f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.5.dp, Color(0xFFF97316).copy(0.4f), RoundedCornerShape(16.dp))
                                .clickable { categoryDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedCategory, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp)
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFFF97316))
                            }
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontFamily = Fredoka, color = Color.White, fontSize = 15.sp) },
                                        onClick = { selectedCategory = cat; categoryDropdownExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // === Difficulty selection ===
                        Text("📈  Select Difficulty", fontFamily = FredokaOne, color = Color.White, fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(0.6f))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.5.dp, Color(0xFF7C3AED).copy(0.4f), RoundedCornerShape(16.dp))
                                .clickable { difficultyDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedDifficulty, fontFamily = Fredoka, color = Color.White, fontSize = 16.sp)
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF7C3AED))
                            }
                            DropdownMenu(
                                expanded = difficultyDropdownExpanded,
                                onDismissRequest = { difficultyDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                difficulties.forEach { diff ->
                                    DropdownMenuItem(
                                        text = { Text(diff, fontFamily = Fredoka, color = Color.White, fontSize = 15.sp) },
                                        onClick = { selectedDifficulty = diff; difficultyDropdownExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val backendCategory = if (selectedCategory == "All Categories") "all" else selectedCategory
                        CartoonButton(
                            text = "🏠  Create Room",
                            onClick = {
                                isLoading = true; errorMessage = null
                                scope.launch {
                                    try {
                                        withTimeout(10000) {
                                            val result = gameRepository.createPrivateRoom(backendCategory, selectedDifficulty)
                                            isLoading = false
                                            if (result.isSuccess) {
                                                val data = result.getOrNull()!!
                                                generatedCode = data.second
                                                isWaiting = true
                                                // Listen for player 2
                                                gameRepository.observeGameSession(data.first).collect { session ->
                                                    if (session?.player2Id?.isNotEmpty() == true) {
                                                        isWaiting = false
                                                        onNavigateToGame(data.first)
                                                    }
                                                }
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Timeout. Try again."
                                    }
                                }
                            },
                            isLoading = isLoading,
                            bgBrush = Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFFBBF24))),
                            textColor = Color(0xFF1E293B)
                        )
                    } else {
                        // Show code
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), spotColor = Color.Black.copy(0.7f))
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF1E293B))
                                .border(2.dp, Color.White.copy(0.1f), RoundedCornerShape(28.dp))
                                .padding(28.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("🏠", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Share this code:", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                // Code display
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))))
                                        .border(2.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp))
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(generatedCode!!, fontFamily = FredokaOne, fontSize = 36.sp, color = Color.White,
                                        letterSpacing = 8.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                // Category/diff badge row
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFF97316).copy(0.2f)).border(1.dp, Color(0xFFF97316).copy(0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                        Text(selectedCategory, fontFamily = Fredoka, color = Color(0xFFFBBF24), fontSize = 12.sp)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF7C3AED).copy(0.2f)).border(1.dp, Color(0xFF7C3AED).copy(0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                        Text(selectedDifficulty, fontFamily = Fredoka, color = Color(0xFFA78BFA), fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (isWaiting) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Waiting for friend... 🦁", fontFamily = Fredoka, color = Color.White.copy(0.6f), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "join" -> {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), spotColor = Color.Black.copy(0.7f))
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF1E293B))
                            .border(2.dp, Color.White.copy(0.1f), RoundedCornerShape(28.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Text("🔑  Enter Room Code", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(2.dp, Color(0xFF7C3AED).copy(0.5f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                TextField(
                                    value = roomCode, onValueChange = { roomCode = it.uppercase() },
                                    singleLine = true,
                                    placeholder = { Text("ROOM CODE", fontFamily = FredokaOne, color = Color.White.copy(0.3f), fontSize = 22.sp, letterSpacing = 4.sp) },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FredokaOne, fontSize = 22.sp, color = Color.White, letterSpacing = 4.sp),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(errorMessage!!, color = Color(0xFFEF4444), fontFamily = Fredoka, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    CartoonButton(
                        text = "🔑  Join Game!",
                        onClick = {
                            if (roomCode.isEmpty()) { errorMessage = "Enter a room code!"; return@CartoonButton }
                            isLoading = true; errorMessage = null
                            scope.launch {
                                val result = gameRepository.joinPrivateRoom(roomCode)
                                isLoading = false
                                if (result.isSuccess) onNavigateToGame(result.getOrNull()!!)
                                else errorMessage = result.exceptionOrNull()?.message ?: "Room not found"
                            }
                        },
                        isLoading = isLoading,
                        bgBrush = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899))),
                        textColor = Color.White
                    )
                }
            }
        }
    }
}
