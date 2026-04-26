package com.quiziche.app.data.repository

import com.quiziche.app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStats(uid: String, won: Boolean, category: String, xpEarned: Int, coinsEarned: Int, isSingleplayer: Boolean = false): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val userRef = usersCollection.document(uid)
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: return@runTransaction
                
                val newWinCount = if (won) user.winCount + 1 else user.winCount
                val newTotalGames = user.totalGames + 1
                val newXp = user.xp + xpEarned
                val newCoins = user.coins + coinsEarned
                
                val newLevel = (newXp / 1000) + 1
                
                var newElo = user.elo
                if (!isSingleplayer) {
                    val eloChange = if (won) 15 else -10
                    newElo = (user.elo + eloChange).coerceAtLeast(0)
                }

                val newCategoryStats = user.categoryStats.toMutableMap()
                if (won) {
                    newCategoryStats[category] = (newCategoryStats[category] ?: 0) + 1
                }
                
                val topCategory = if (newCategoryStats.isNotEmpty()) {
                    newCategoryStats.maxByOrNull { it.value }?.key ?: "None"
                } else {
                    user.topCategory
                }
                
                transaction.update(userRef, mapOf(
                    "winCount" to newWinCount,
                    "totalGames" to newTotalGames,
                    "xp" to newXp,
                    "coins" to newCoins,
                    "level" to newLevel,
                    "elo" to newElo,
                    "categoryStats" to newCategoryStats,
                    "topCategory" to topCategory
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveMatchResult(uid: String, result: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).collection("matches").add(result).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchHistory(uid: String, limitCount: Long = 10): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = usersCollection.document(uid).collection("matches")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
                
            val matches = snapshot.documents.mapNotNull { it.data }
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(limitCount: Long = 50): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("elo", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
