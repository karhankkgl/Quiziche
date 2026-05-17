package com.quiziche.app.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.quiziche.app.BuildConfig
import com.quiziche.app.data.model.Question
import org.json.JSONArray

class AiQuestionRepository {

    private val TAG = "AiQuestionRepository"

    private val model = GenerativeModel(
        modelName = "gemini-3.1-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val quizRepository = QuizRepository()

    /**
     * Tüm kategoriler için toplu soru üretir.
     * Her kategori tamamlandığında onProgress callback'ini çağırır.
     */
    suspend fun generateForAllCategories(
        difficulty: String = "Medium",
        countPerCategory: Int = 20,
        onProgress: (category: String, done: Int, total: Int, success: Boolean) -> Unit = { _, _, _, _ -> }
    ): Map<String, Int> {
        val allCategories = listOf(
            "Science", "History", "Sports", "Art", "Music",
            "Geography", "Movies", "Literature", "Technology", "Food"
        )
        val results = mutableMapOf<String, Int>()

        allCategories.forEachIndexed { index, category ->
            Log.d(TAG, "Bulk seeding: $category (${ index + 1}/${allCategories.size})")
            val result = generateAndSaveQuestions(category, difficulty, countPerCategory)
            val savedCount = if (result.isSuccess) result.getOrDefault(emptyList()).size else 0
            results[category] = savedCount
            onProgress(category, index + 1, allCategories.size, result.isSuccess)
            // Rate limiting: Free Tier limiti (15 RPM) aşılmaması için 6.5 saniye bekleme süresi
            kotlinx.coroutines.delay(6500)
        }

        Log.d(TAG, "Bulk seed complete. Total saved: ${results.values.sum()} questions")
        return results
    }

    /**
     * Gemini ile verilen kategori ve zorluk için sorular üretir,
     * Firestore'a isAiGenerated=true olarak kaydeder.
     */

    suspend fun generateAndSaveQuestions(
        category: String,
        difficulty: String,
        count: Int = 5
    ): Result<List<Question>> {
        return try {
            val prompt = buildPrompt(category, difficulty, count)
            Log.d(TAG, "Sending prompt to Gemini for category=$category difficulty=$difficulty count=$count")

            val response = model.generateContent(prompt)
            val rawText = response.text ?: throw Exception("Gemini returned empty response")
            Log.d(TAG, "Gemini raw response: $rawText")

            val questions = parseQuestionsFromJson(rawText, category, difficulty)

            if (questions.isEmpty()) {
                throw Exception("No questions could be parsed from the AI response")
            }

            // Firestore'a kaydet
            val savedQuestions = mutableListOf<Question>()
            questions.forEach { question ->
                val result = quizRepository.saveGeneratedQuestion(question)
                if (result.isSuccess) {
                    savedQuestions.add(question)
                    Log.d(TAG, "Saved AI question: ${question.text}")
                } else {
                    Log.e(TAG, "Failed to save question: ${question.text}")
                }
            }

            Log.d(TAG, "Successfully saved ${savedQuestions.size}/${questions.size} AI questions")
            Result.success(savedQuestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating questions: ", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(category: String, difficulty: String, count: Int): String {
        return """
You are a quiz question generator. Generate exactly $count trivia quiz questions for the category "$category" with difficulty level "$difficulty".

Return ONLY a valid JSON array (no markdown, no explanation, no code block fences).
Each object must have exactly these fields:
- "text": the question string
- "options": array of exactly 4 answer strings
- "correctAnswerIndex": integer 0-3 indicating the correct answer index

Example format:
[{"text":"What is 2+2?","options":["3","4","5","6"],"correctAnswerIndex":1}]

Generate $count questions now:
        """.trimIndent()
    }

    private fun parseQuestionsFromJson(
        rawText: String,
        category: String,
        difficulty: String
    ): List<Question> {
        return try {
            // JSON array'i bulmak için temizle
            val cleaned = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // [ ile başlayan kısmı bul
            val startIndex = cleaned.indexOf('[')
            val endIndex = cleaned.lastIndexOf(']')

            if (startIndex == -1 || endIndex == -1) {
                Log.e(TAG, "No JSON array found in response: $cleaned")
                return emptyList()
            }

            val jsonStr = cleaned.substring(startIndex, endIndex + 1)
            val jsonArray = JSONArray(jsonStr)
            val questions = mutableListOf<Question>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    val text = obj.getString("text")
                    val optionsArray = obj.getJSONArray("options")
                    val correctIndex = obj.getInt("correctAnswerIndex")

                    val options = (0 until optionsArray.length()).map { j ->
                        optionsArray.getString(j)
                    }

                    if (options.size == 4 && correctIndex in 0..3 && text.isNotBlank()) {
                        questions.add(
                            Question(
                                id = "",
                                text = text,
                                options = options,
                                correctAnswerIndex = correctIndex,
                                category = category,
                                difficulty = difficulty,
                                isAiGenerated = true
                            )
                        )
                    } else {
                        Log.w(TAG, "Skipping malformed question at index $i")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing question at index $i: ${e.message}")
                }
            }

            questions
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            emptyList()
        }
    }
}
