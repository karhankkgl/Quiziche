package com.quiziche.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.quiziche.app.data.model.GameSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class GameRepository {
    private val database = FirebaseDatabase.getInstance("https://quiziche-app-default-rtdb.europe-west1.firebasedatabase.app/")
    private val queueRef = database.getReference("matchmaking_queue")
    private val roomsRef = database.getReference("active_rooms")
    private val auth = FirebaseAuth.getInstance()
    private val quizRepository = QuizRepository()

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun joinMatchmaking(category: String, onMatchFound: (String) -> Unit) {
        val uid = currentUid ?: return
        
        // 1. Ensure we're not already in the queue
        cancelMatchmaking(category)
        
        val categoryQueueRef = queueRef.child(category)
        val claimedOpponent = AtomicReference<String?>(null)
        val transactionDeferred = CompletableDeferred<String?>()

        categoryQueueRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentData = mutableData.value
                val queue = if (currentData is Map<*, *>) {
                    currentData as Map<String, Any>
                } else {
                    emptyMap<String, Any>()
                }
                
                val opponent = queue.keys.firstOrNull { it != uid }
                
                if (opponent != null) {
                    claimedOpponent.set(opponent)
                    mutableData.child(opponent).value = null // Claim by removing
                } else {
                    claimedOpponent.set(null)
                    // If not in queue, join it
                    if (!queue.containsKey(uid)) {
                        mutableData.child(uid).value = ServerValue.TIMESTAMP
                    }
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed && error == null) {
                    transactionDeferred.complete(claimedOpponent.get())
                } else {
                    transactionDeferred.completeExceptionally(error?.toException() ?: Exception("Matchmaking failed"))
                }
            }
        })

        val opponentUid = try { transactionDeferred.await() } catch (e: Exception) { null }

        if (opponentUid != null) {
            // We claimed an opponent! We create the room.
            val roomId = UUID.randomUUID().toString()
            val questionsResult = quizRepository.getQuestionsByCategory(category, 5)
            val questionIds = questionsResult.getOrNull()?.map { it.id } ?: emptyList()
            val session = GameSession(
                sessionId = roomId,
                player1Id = opponentUid,
                player2Id = uid,
                status = "ACTIVE",
                category = category,
                questionIds = questionIds,
                startTime = System.currentTimeMillis()
            )
            roomsRef.child(roomId).setValue(session).await()
            onMatchFound(roomId)
        } else {
            // We are in the queue, wait for someone to find us
            val roomQuery = roomsRef.orderByChild("player1Id").equalTo(uid)
            val roomListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val room = snapshot.children.firstOrNull { 
                        val session = it.getValue(GameSession::class.java)
                        session?.status == "ACTIVE" && session.player2Id.isNotEmpty()
                    }
                    if (room != null) {
                        val roomId = room.key!!
                        roomQuery.removeEventListener(this)
                        onMatchFound(roomId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            roomQuery.addValueEventListener(roomListener)
        }
    }

    // --- PRIVATE ROOMS (Friend Mode) ---
    suspend fun createPrivateRoom(category: String): Result<Pair<String, String>> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        val roomId = UUID.randomUUID().toString()
        val inviteCode = (100000..999999).random().toString()
        
        val questionsResult = quizRepository.getQuestionsByCategory(category, 5)
        val questionIds = questionsResult.getOrNull()?.map { it.id } ?: emptyList()
        
        val session = GameSession(
            sessionId = roomId,
            player1Id = uid,
            status = "WAITING",
            category = category,
            questionIds = questionIds,
            inviteCode = inviteCode,
            startTime = System.currentTimeMillis()
        )
        
        return try {
            withTimeout(10000) { // 10 second timeout
                roomsRef.child(roomId).setValue(session).await()
            }
            Result.success(roomId to inviteCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setReady(roomId: String, isPlayer1: Boolean, ready: Boolean) {
        val field = if (isPlayer1) "player1Ready" else "player2Ready"
        roomsRef.child(roomId).child(field).setValue(ready).await()
    }

    suspend fun submitAnswer(roomId: String, isPlayer1: Boolean, score: Int) {
        val scoreField = if (isPlayer1) "player1Score" else "player2Score"
        val answeredField = if (isPlayer1) "player1Answered" else "player2Answered"
        
        val updates = mapOf(
            scoreField to score,
            answeredField to true
        )
        roomsRef.child(roomId).updateChildren(updates).await()
    }

    suspend fun syncNextQuestion(roomId: String, nextIndex: Int) {
        val updates = mapOf(
            "currentQuestionIndex" to nextIndex,
            "player1Answered" to false,
            "player2Answered" to false,
            "currentQuestionStartTime" to System.currentTimeMillis()
        )
        roomsRef.child(roomId).updateChildren(updates).await()
    }

    suspend fun joinPrivateRoom(inviteCode: String): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val snapshot = roomsRef.orderByChild("inviteCode").equalTo(inviteCode).get().await()
            val room = snapshot.children.firstOrNull { 
                val session = it.getValue(GameSession::class.java)
                session?.status == "WAITING" && session.player2Id.isEmpty()
            }
            
            if (room != null) {
                val roomId = room.key!!
                roomsRef.child(roomId).child("player2Id").setValue(uid).await()
                roomsRef.child(roomId).child("status").setValue("ACTIVE").await()
                Result.success(roomId)
            } else {
                Result.failure(Exception("Room not found or already full"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun cancelMatchmaking(category: String) {
        val uid = currentUid ?: return
        queueRef.child(category).child(uid).removeValue()
    }
    
    fun observeGameSession(roomId: String): Flow<GameSession?> = callbackFlow {
        val ref = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(GameSession::class.java)
                trySend(session)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
    
    suspend fun updateScore(roomId: String, isPlayer1: Boolean, newScore: Int) {
        val field = if (isPlayer1) "player1Score" else "player2Score"
        roomsRef.child(roomId).child(field).setValue(newScore).await()
    }

    suspend fun completeGame(roomId: String) {
        roomsRef.child(roomId).child("status").setValue("COMPLETED").await()
    }

    // --- GAME INVITE SYSTEM ---
    private val invitesRef = database.getReference("game_invites")

    suspend fun sendInvite(targetUid: String, senderName: String, category: String = "General"): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        val inviteId = UUID.randomUUID().toString()
        val inviteData = mapOf(
            "inviteId" to inviteId,
            "senderUid" to uid,
            "senderName" to senderName,
            "category" to category,
            "timestamp" to System.currentTimeMillis()
        )
        return try {
            invitesRef.child(targetUid).child(inviteId).setValue(inviteData).await()
            Result.success(inviteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeInvites(): Flow<List<Map<String, Any>>> = callbackFlow {
        val uid = currentUid ?: run { close(); return@callbackFlow }
        val ref = invitesRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val invites = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                trySend(invites)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun acceptInvite(inviteId: String, senderUid: String, category: String): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            invitesRef.child(uid).child(inviteId).removeValue().await()
            
            val roomId = UUID.randomUUID().toString()
            val session = GameSession(
                sessionId = roomId,
                player1Id = senderUid,
                player2Id = uid,
                status = "ACTIVE",
                category = category,
                startTime = System.currentTimeMillis()
            )
            roomsRef.child(roomId).setValue(session).await()
            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectInvite(inviteId: String) {
        currentUid?.let { uid ->
            invitesRef.child(uid).child(inviteId).removeValue().await()
        }
    }

    fun observeConnectionState(): Flow<Boolean> = callbackFlow {
        val connectedRef = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        connectedRef.addValueEventListener(listener)
        awaitClose { connectedRef.removeEventListener(listener) }
    }

    fun getActiveGames(): Flow<List<GameSession>> = callbackFlow {
        val uid = currentUid ?: return@callbackFlow
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.getValue(GameSession::class.java) }
                    .filter { it.status == "ACTIVE" && (it.player1Id == uid || it.player2Id == uid) }
                trySend(rooms)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomsRef.addValueEventListener(listener)
        awaitClose { roomsRef.removeEventListener(listener) }
    }
}
