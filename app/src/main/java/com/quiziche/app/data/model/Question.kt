package com.quiziche.app.data.model

data class Question(
    val id: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = -1,
    val category: String = "",
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val isAiGenerated: Boolean = false
)
