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
                var newWeeklyElo = user.weeklyElo
                if (!isSingleplayer) {
                    val eloChange = if (won) 15 else -10
                    newElo = (user.elo + eloChange).coerceAtLeast(0)
                    newWeeklyElo = (user.weeklyElo + eloChange).coerceAtLeast(0)
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
                    "weeklyElo" to newWeeklyElo,
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

    suspend fun getLeaderboard(limitCount: Long = 50, isWeekly: Boolean = false): Result<List<User>> {
        return try {
            val field = if (isWeekly) "weeklyElo" else "elo"
            val snapshot = usersCollection
                .orderBy(field, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategoryLeaderboard(categoryId: String, limitCount: Long = 5): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("categoryStats.$categoryId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java).filter { (it.categoryStats[categoryId] ?: 0) > 0 }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsersByUsername(query: String): Result<List<User>> {
        return try {
            if (query.isBlank()) return Result.success(emptyList())
            // Note: Firestore doesn't support 'contains' queries natively.
            // Using a simple prefix match or fetching a subset and filtering in memory.
            val snapshot = usersCollection.get().await()
            val users = snapshot.toObjects(User::class.java)
            val filtered = users.filter { it.name.contains(query, ignoreCase = true) }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(currentUid: String, targetUid: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val currentUserRef = usersCollection.document(currentUid)
                val targetUserRef = usersCollection.document(targetUid)
                
                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)
                
                val currentUser = currentUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                val targetUser = targetUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                
                if (!targetUser.friendRequests.contains(currentUid)) {
                    val newRequests = targetUser.friendRequests + currentUid
                    transaction.update(targetUserRef, "friendRequests", newRequests)
                }
                if (!currentUser.sentFriendRequests.contains(targetUid)) {
                    val newSent = currentUser.sentFriendRequests + targetUid
                    transaction.update(currentUserRef, "sentFriendRequests", newSent)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(currentUid: String, targetUid: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val currentUserRef = usersCollection.document(currentUid)
                val targetUserRef = usersCollection.document(targetUid)
                
                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)
                
                val currentUser = currentUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                val targetUser = targetUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                
                val newFriendsCurrent = (currentUser.friends + targetUid).distinct()
                val newRequestsCurrent = currentUser.friendRequests - targetUid
                
                val newFriendsTarget = (targetUser.friends + currentUid).distinct()
                val newSentTarget = targetUser.sentFriendRequests - currentUid
                
                transaction.update(currentUserRef, mapOf("friends" to newFriendsCurrent, "friendRequests" to newRequestsCurrent))
                transaction.update(targetUserRef, mapOf("friends" to newFriendsTarget, "sentFriendRequests" to newSentTarget))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectFriendRequest(currentUid: String, targetUid: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val currentUserRef = usersCollection.document(currentUid)
                val targetUserRef = usersCollection.document(targetUid)
                
                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)
                
                val currentUser = currentUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                val targetUser = targetUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                
                val newRequestsCurrent = currentUser.friendRequests - targetUid
                val newSentTarget = targetUser.sentFriendRequests - currentUid
                
                transaction.update(currentUserRef, "friendRequests", newRequestsCurrent)
                transaction.update(targetUserRef, "sentFriendRequests", newSentTarget)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeFriend(currentUid: String, targetUid: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val currentUserRef = usersCollection.document(currentUid)
                val targetUserRef = usersCollection.document(targetUid)
                
                val currentUserSnapshot = transaction.get(currentUserRef)
                val targetUserSnapshot = transaction.get(targetUserRef)
                
                val currentUser = currentUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                val targetUser = targetUserSnapshot.toObject(User::class.java) ?: return@runTransaction
                
                val newFriendsCurrent = currentUser.friends - targetUid
                val newFriendsTarget = targetUser.friends - currentUid
                
                transaction.update(currentUserRef, "friends", newFriendsCurrent)
                transaction.update(targetUserRef, "friends", newFriendsTarget)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedRandomUsers() {
        val avatars = listOf("🎮", "🦁", "🐧", "🦖", "🦄", "🐼", "🤖", "🦊", "🐶", "🐱")
        val names = listOf("QuizMaster", "BrainBox", "ScienceGuru", "EinsteinJr", "NerdAlert", "TriviaKing", "FactMachine", "SmartyPants", "KnowledgeBase", "Thinker", "Genius", "Scholar", "Professor", "WhizKid", "Savant", "Brainiac", "Intellect", "Sage", "Oracle", "Guru")
        val categories = listOf("science", "history", "sports", "art", "music", "geography", "movies", "literature", "technology", "food")
        
        for (i in 1..25) {
            val uid = java.util.UUID.randomUUID().toString()
            val elo = (800..2500).random()
            val weeklyElo = (800..1800).random()
            val xp = (100..50000).random()
            val coins = (0..5000).random()
            val winCount = (0..500).random()
            val totalGames = winCount + (0..200).random()
            val topCategory = categories.random()
            
            val catStats = mutableMapOf<String, Int>()
            for (c in categories.shuffled().take(3)) {
                catStats[c] = (1..100).random()
            }
            
            val user = User(
                uid = uid,
                name = names.random() + (1..99).random(),
                email = "user$i@quiziche.test",
                level = (xp / 1000) + 1,
                xp = xp,
                coins = coins,
                winCount = winCount,
                totalGames = totalGames,
                topCategory = topCategory,
                avatarIcon = avatars.random(),
                elo = elo,
                weeklyElo = weeklyElo,
                categoryStats = catStats,
                friends = emptyList(),
                friendRequests = emptyList(),
                sentFriendRequests = emptyList()
            )
            usersCollection.document(uid).set(user).await()
        }
    }
}
