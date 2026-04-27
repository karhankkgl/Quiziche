package com.quiziche.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    textColor: Color = Color(0xFF1E1B4B),
    borderColor: Color = Color(0xFF1E1B4B),
    elevation: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = if (isLoading) 0.dp else elevation, shape = RoundedCornerShape(20.dp), spotColor = borderColor.copy(0.4f))
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .border(3.dp, borderColor, RoundedCornerShape(20.dp))
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
            .border(2.dp, Color.White.copy(0.2f), RoundedCornerShape(18.dp))
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
            Text(placeholder, color = Color.White.copy(0.45f), fontFamily = Fredoka, fontSize = 16.sp)
        },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Fredoka,
            fontSize = 16.sp,
            color = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CartoonCard(
    modifier: Modifier = Modifier,
    bgBrush: Brush = Brush.linearGradient(listOf(Color.White, Color(0xFFF9FAFB))),
    borderColor: Color = Color(0xFF1E1B4B),
    elevation: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(24.dp), spotColor = borderColor.copy(0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(bgBrush)
            .border(3.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        content()
    }
}
