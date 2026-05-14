package com.quiziche.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiziche.app.ui.theme.Fredoka
import com.quiziche.app.ui.theme.FredokaOne

@Composable
fun CartoonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    bgBrush: Brush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
    textColor: Color = Color.White,
    borderColor: Color = Color.White.copy(0.2f),
    elevation: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = if (isLoading) 0.dp else elevation, shape = RoundedCornerShape(20.dp), spotColor = borderColor.copy(0.4f))
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = textColor, modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
        } else {
            Text(text = text, fontFamily = FredokaOne, fontSize = 18.sp, color = textColor)
        }
    }
}

@Composable
fun CartoonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    emoji: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        BasicCartoonInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            keyboardType = keyboardType,
            isPassword = isPassword,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BasicCartoonInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        placeholder = {
            Text(placeholder, fontFamily = Fredoka, color = Color.White.copy(0.4f), fontSize = 14.sp)
        },
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Fredoka, fontSize = 16.sp, color = Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        )
    )
}

@Composable
fun CartoonCard(
    modifier: Modifier = Modifier,
    bgBrush: Brush = Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
    borderColor: Color = Color.White.copy(0.1f),
    elevation: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.4f))
            .clip(RoundedCornerShape(24.dp))
            .background(bgBrush)
            .border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        content()
    }
}
@Composable
fun ModernBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = Color.Black.copy(0.6f))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))))
            .border(2.dp, Color.White, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("◀", fontSize = 20.sp, color = Color.White, modifier = Modifier.offset(x = (-1).dp))
    }
}

@Composable
fun CartoonNavItem(
    emoji: String,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    badge: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clickable { onClick() }.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF7C3AED) else Color.White.copy(0.05f))
                        .then(if (isSelected) Modifier.border(1.5.dp, Color.White.copy(0.2f), RoundedCornerShape(14.dp)) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
                if (badge > 0) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp).size(16.dp).clip(CircleShape).background(Color.Red).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text(if (badge > 9) "9+" else badge.toString(), color = Color.White, fontSize = 9.sp, fontFamily = FredokaOne)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontFamily = FredokaOne, color = if (isSelected) Color(0xFFFBBF24) else Color.White.copy(0.6f), fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

// Keep for legacy compatibility if needed
@Composable
fun CartoonNavigationItem(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    CartoonNavItem(emoji = when (label) {
        "Home" -> "🏠"
        "Categories" -> "🗂️"
        "Rankings" -> "🏆"
        "Friends" -> "🦁"
        else -> "⭐"
    }, label = label, isSelected = isSelected, onClick = onClick, badge = badgeCount, modifier = modifier)
}

@Composable
fun CategorySelectionDialog(
    onDismissRequest: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        Triple("🔬", "Science", "science"),
        Triple("🏛️", "History", "history"),
        Triple("⚽", "Sports", "sports"),
        Triple("🎨", "Art", "art"),
        Triple("🎵", "Music", "music"),
        Triple("🌍", "Geography", "geography"),
        Triple("🎬", "Movies", "movies"),
        Triple("📚", "Literature", "literature"),
        Triple("💻", "Technology", "technology"),
        Triple("🍕", "Food", "food"),
        Triple("🎲", "Random Mix", "all")
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF2D1B69),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Select a Category", fontFamily = FredokaOne, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(categories.size) { idx ->
                    val cat = categories[idx]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.1f))
                            .clickable { onCategorySelected(cat.third) }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.first, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(cat.second, fontFamily = FredokaOne, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", fontFamily = Fredoka, color = Color(0xFFFCA5A5))
            }
        }
    )
}
