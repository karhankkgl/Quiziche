package com.quiziche.app.data.repository

import com.quiziche.app.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QuizRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val questionsCollection = firestore.collection("questions")

    suspend fun getQuestionsByCategory(category: String, limit: Long = 5): Result<List<Question>> {
        return try {
            val query = if (category == "all" || category == "All Categories") {
                questionsCollection.limit(limit)
            } else {
                questionsCollection.whereEqualTo("category", category).limit(limit)
            }
            
            val snapshot = query.get().await()
            val questions = snapshot.toObjects(Question::class.java)
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveGeneratedQuestion(question: Question): Result<Unit> {
        return try {
            questionsCollection.add(question).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
