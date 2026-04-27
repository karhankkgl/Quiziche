package com.quiziche.app.data.repository

import com.quiziche.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserUID: String?
        get() = auth.currentUser?.uid

    suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")
            
            val user = User(
                uid = uid,
                name = name,
                email = email
            )
            
            // Save to Firestore
            firestore.collection("users").document(uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID is null")
            
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google sign in failed")
            
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java) ?: throw Exception("User data mapping failed")
            } else {
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: ""
                )
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                newUser
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
