package com.quiziche.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.quiziche.app.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Nunito with Black weight for that extra "fat" cartoon look
val NunitoBlack = FontFamily(
    Font(
        googleFont = GoogleFont("Nunito"),
        fontProvider = provider,
        weight = FontWeight.Black
    )
)

val NunitoBold = FontFamily(
    Font(
        googleFont = GoogleFont("Nunito"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont("Nunito"),
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

// Compatibility aliases
val FredokaOne = NunitoBlack
val Fredoka = NunitoBold

// Material3 Typography with extra weight
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = NunitoBlack, fontWeight = FontWeight.Black, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Bold, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Black, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Black, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = NunitoBold, fontWeight = FontWeight.Black, fontSize = 11.sp)
)
