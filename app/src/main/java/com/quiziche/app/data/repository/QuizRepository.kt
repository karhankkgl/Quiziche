package com.quiziche.app.data.repository

import com.quiziche.app.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QuizRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val questionsCollection = firestore.collection("questions")

    suspend fun getQuestionsByCategory(category: String, limit: Long = 5, difficulty: String = "Any"): Result<List<Question>> {
        return try {
            var query: com.google.firebase.firestore.Query = questionsCollection

            if (category != "all" && category != "All Categories") {
                query = query.whereEqualTo("category", category)
            }
            if (difficulty != "Any") {
                query = query.whereEqualTo("difficulty", difficulty)
            }

            val snapshot = try {
                query.limit(limit).get().await()
            } catch (indexEx: Exception) {
                // Composite index may not exist; retry with category only
                android.util.Log.w("QuizRepository", "Composite query failed, retrying without difficulty filter: ${indexEx.message}")
                var fallbackQuery: com.google.firebase.firestore.Query = questionsCollection
                if (category != "all" && category != "All Categories") {
                    fallbackQuery = fallbackQuery.whereEqualTo("category", category)
                }
                fallbackQuery.limit(limit).get().await()
            }

            val questions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Question::class.java)?.copy(id = doc.id)
            }

            android.util.Log.d("QuizRepository", "Fetched ${questions.size} questions for category=$category difficulty=$difficulty")
            Result.success(questions)
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Error fetching by category: ", e)
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
    suspend fun getQuestionsByIds(ids: List<String>): Result<List<Question>> {
        return try {
            if (ids.isEmpty()) return Result.success(emptyList())
            
            // Firestore 'in' queries support up to 10 items.
            // Our questionIds limit is usually 5, so this is safe.
            val snapshot = questionsCollection.whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids).get().await()
            val questions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Question::class.java)?.copy(id = doc.id)
            }
            
            // Sort questions to match the exact order of 'ids'
            val sortedQuestions = ids.mapNotNull { id -> questions.find { it.id == id } }
            
            android.util.Log.d("QuizRepository", "Fetched ${sortedQuestions.size} questions by IDs")
            Result.success(sortedQuestions)
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Error fetching by IDs: ", e)
            Result.failure(e)
        }
    }
}
