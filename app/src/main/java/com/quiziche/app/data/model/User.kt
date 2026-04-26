package com.quiziche.app.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 0,
    val winCount: Int = 0,
    val totalGames: Int = 0,
    val topCategory: String = "None",
    val avatarIcon: String = "🎮",
    val elo: Int = 1000,
    val categoryStats: Map<String, Int> = emptyMap() // Map of Category Name -> Win Count
) {
    fun getClassName(): String {
        return when {
            xp < 500 -> "Newbie"
            xp < 1500 -> "Amateur"
            xp < 3000 -> "Rookie"
            xp < 5000 -> "Apprentice"
            xp < 8000 -> "Challenger"
            xp < 12000 -> "Pro"
            xp < 17000 -> "Master"
            xp < 25000 -> "Grandmaster"
            xp < 50000 -> "Champion"
            else -> "Legend"
        }
    }
}
