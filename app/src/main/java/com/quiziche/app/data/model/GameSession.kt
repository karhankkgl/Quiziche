package com.quiziche.app.data.model

data class GameSession(
    val sessionId: String = "",
    val player1Id: String = "",
    val player2Id: String = "",
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val currentQuestionIndex: Int = 0,
    val questionIds: List<String> = emptyList(),
    val status: String = "WAITING", // WAITING, ACTIVE, COMPLETED
    val turnPlayerId: String = "",
    val startTime: Long = 0,
    val category: String = ""
)
