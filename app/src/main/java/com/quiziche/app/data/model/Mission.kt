package com.quiziche.app.data.model

data class Mission(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val rewardCoins: Int = 0,
    val rewardXp: Int = 0,
    val targetAmount: Int = 0,
    val missionType: String = "" // "play_games", "win_games", "score_points"
)

data class UserMissionProgress(
    val missionId: String = "",
    val currentAmount: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)
