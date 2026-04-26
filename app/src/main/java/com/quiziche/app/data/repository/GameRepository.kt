package com.quiziche.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quiziche.app.data.model.GameSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GameRepository {
    private val database = FirebaseDatabase.getInstance()
    private val queueRef = database.getReference("matchmaking_queue")
    private val roomsRef = database.getReference("active_rooms")
    private val auth = FirebaseAuth.getInstance()

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun joinMatchmaking(category: String, onMatchFound: (String) -> Unit) {
        val uid = currentUid ?: return
        
        // Check if there is anyone in the queue for this category
        val queueSnapshot = queueRef.child(category).get().await()
        
        if (queueSnapshot.exists() && queueSnapshot.childrenCount > 0) {
            // Find an opponent (take the first one that is not us)
            val opponentSnapshot = queueSnapshot.children.firstOrNull { it.key != uid }
            if (opponentSnapshot != null) {
                val opponentUid = opponentSnapshot.key!!
                // Remove opponent from queue
                queueRef.child(category).child(opponentUid).removeValue().await()
                
                // Create a room
                val roomId = UUID.randomUUID().toString()
                val session = GameSession(
                    sessionId = roomId,
                    player1Id = opponentUid,
                    player2Id = uid,
                    status = "ACTIVE",
                    category = category,
                    startTime = System.currentTimeMillis()
                )
                
                // Save to active_rooms
                roomsRef.child(roomId).setValue(session).await()
                
                // Notify via callback (Opponent needs to listen to this room somehow, usually by waiting on their own queue entry)
                // In a robust system, the queue entry would be updated with the roomId before deletion.
                onMatchFound(roomId)
                return
            }
        }
        
        // No opponent found, join queue
        val queueEntry = mapOf("joinedAt" to System.currentTimeMillis())
        queueRef.child(category).child(uid).setValue(queueEntry).await()
        
        // Listen for a room where we are player1
        roomsRef.orderByChild("player1Id").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val room = snapshot.children.firstOrNull()
                        val roomId = room?.key
                        if (roomId != null) {
                            queueRef.child(category).child(uid).removeValue()
                            roomsRef.removeEventListener(this)
                            onMatchFound(roomId)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    
    fun cancelMatchmaking(category: String) {
        val uid = currentUid ?: return
        queueRef.child(category).child(uid).removeValue()
    }
    
    fun observeGameSession(roomId: String): Flow<GameSession?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(GameSession::class.java)
                trySend(session)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = roomsRef.child(roomId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
    
    suspend fun updateScore(roomId: String, isPlayer1: Boolean, newScore: Int) {
        val field = if (isPlayer1) "player1Score" else "player2Score"
        roomsRef.child(roomId).child(field).setValue(newScore).await()
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
        val uid = currentUid
        if (uid == null) {
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val invites = snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                trySend(invites)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = invitesRef.child(uid)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun acceptInvite(inviteId: String, senderUid: String, category: String): Result<String> {
        val uid = currentUid ?: return Result.failure(Exception("Not logged in"))
        return try {
            // Remove the invite
            invitesRef.child(uid).child(inviteId).removeValue().await()
            
            // Create active room
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
            
            // Note: Sender should be listening to a specific place or we can notify them. 
            // For simplicity, sender could listen to `active_rooms` where `player1Id == senderUid`.
            
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
}
