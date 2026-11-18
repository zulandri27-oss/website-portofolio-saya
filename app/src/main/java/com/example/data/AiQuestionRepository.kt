package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class AiQuestionRepository {

    private val itemSchema = Schema.obj(
        properties = mapOf(
            "stimulusTitle" to Schema.string(description = "Concise title for the context stimulus, reading passage, or empirical scenario"),
            "stimulusContent" to Schema.string(description = "Rich academic context stimulus (data excerpt, primary source, or experimental brief) grounding the question"),
            "questionStem" to Schema.string(description = "Clear, unambiguous academic question stem aligned to the requested Bloom level"),
            "options" to Schema.array(
                items = Schema.string(),
                description = "Exactly 4 plausible academic options (A-D) for MULTIPLE_CHOICE or COMPLEX_MC, or an empty array for ESSAY"
            ),
            "correctOptionIndex" to Schema.integer(description = "0-based index (0..3) of the single best answer for objective items, or -1 for ESSAY"),
            "pedagogicalRationale" to Schema.string(description = "Detailed pedagogical explanation of the correct reasoning and why distractors fail"),
            "scoringRubric" to Schema.string(description = "Structured grading criteria and point allocation"),
            "curriculumAlignment" to Schema.string(description = "Relevant curriculum standard code or learning competency descriptor"),
            "estimatedMinutes" to Schema.integer(description = "Realistic student completion time in minutes (1 to 30)"),
            "discriminationIndex" to Schema.double(description = "Estimated psychometric discrimination index between 0.20 and 0.85")
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = itemSchema
                temperature = 0.4f
            }
        )
    }

    suspend fun generateAssessmentItem(
        userId: String,
        subject: String,
        gradeLevel: String,
        topic: String,
        bloomLevel: BloomLevel,
        format: QuestionFormat,
        difficulty: DifficultyLevel,
        customStimulus: String
    ): Result<QuestionItem> {
        val cleanSubject = subject.trim().take(60).ifBlank { "General Studies" }
        val cleanGrade = gradeLevel.trim().take(40).ifBlank { "Grade 11" }
        val cleanTopic = topic.trim().take(150)

        if (cleanTopic.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a curriculum topic or learning objective."))
        }

        val stimulusInstruction = if (customStimulus.isNotBlank()) {
            "Use and refine this educator-provided context stimulus (preserving its core data/facts):\n\"\"\"${customStimulus.trim().take(2500)}\"\"\""
        } else {
            "Author an original, rigorous academic stimulus (short reading passage, experimental dataset summary, or primary scenario, 60-140 words) that students must analyze to answer."
        }

        val formatInstruction = when (format) {
            QuestionFormat.MULTIPLE_CHOICE ->
                "Provide exactly 4 distinct, parallel-length options in the `options` array (without A/B/C/D prefixes) and set `correctOptionIndex` to 0, 1, 2, or 3."
            QuestionFormat.COMPLEX_MC ->
                "Design a multi-statement or assertion-reason HOTS item with 4 synthesized analytical options in `options` (without A/B/C/D prefixes) and set `correctOptionIndex` to 0, 1, 2, or 3."
            QuestionFormat.ESSAY ->
                "Design an extended-response constructed prompt. Set `options` to an empty array [] and `correctOptionIndex` to -1, and provide a detailed multi-level analytical `scoringRubric`."
        }

        val prompt = """
            You are a senior psychometrician and curriculum assessment designer.
            Create one institutional-grade assessment item with these exact specifications:
            - Subject: $cleanSubject
            - Grade / Level: $cleanGrade
            - Topic / Objective: $cleanTopic
            - Bloom's Cognitive Taxonomy: ${bloomLevel.code} (${bloomLevel.shortTitle} — ${bloomLevel.tierDescription})
            - Question Format: ${format.displayLabel}
            - Difficulty Tier: ${difficulty.displayLabel}

            Stimulus requirement:
            $stimulusInstruction

            Format requirement:
            $formatInstruction

            Ensure high academic precision, zero ambiguity, plausible distractors rooted in common student misconceptions, and accurate pedagogical metadata.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val jsonString = response.text?.trim()
                ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))

            val json = JSONObject(jsonString)
            val rawOptions = json.optJSONArray("options")
            val parsedOptions = buildList {
                if (rawOptions != null) {
                    val limit = minOf(rawOptions.length(), 6)
                    for (i in 0 until limit) {
                        val opt = rawOptions.optString(i, "").trim()
                        if (opt.isNotEmpty()) add(opt)
                    }
                }
            }

            val finalOptions = if (format == QuestionFormat.ESSAY) emptyList() else parsedOptions
            val rawCorrectIndex = json.optInt("correctOptionIndex", if (finalOptions.isEmpty()) -1 else 0)
            val finalCorrectIndex = if (finalOptions.isEmpty()) {
                -1L
            } else {
                rawCorrectIndex.coerceIn(0, finalOptions.lastIndex).toLong()
            }

            val estMinutes = json.optInt("estimatedMinutes", 3).coerceIn(1, 120).toLong()
            val discIndex = json.optDouble("discriminationIndex", 0.42).coerceIn(0.0, 1.0)

            val item = QuestionItem(
                userId = userId,
                subject = cleanSubject,
                gradeLevel = cleanGrade,
                topic = cleanTopic,
                bloomLevel = bloomLevel.code,
                questionFormat = format.wireName,
                difficulty = difficulty.wireName,
                stimulusTitle = json.optString("stimulusTitle", "Context Stimulus").take(160),
                stimulusContent = json.optString("stimulusContent", "").take(4000),
                questionStem = json.optString("questionStem", "").take(2000).ifBlank {
                    throw IllegalStateException("Generated item missed a valid question stem.")
                },
                options = finalOptions,
                correctOptionIndex = finalCorrectIndex,
                pedagogicalRationale = json.optString("pedagogicalRationale", "").take(2000),
                scoringRubric = json.optString("scoringRubric", "").take(2000),
                curriculumAlignment = json.optString("curriculumAlignment", "$cleanSubject · $cleanGrade").take(200),
                estimatedMinutes = estMinutes,
                discriminationIndex = discIndex,
                reviewStatus = ReviewStatus.DRAFT.wireName
            )
            Result.success(item)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini item generation timed out", e)
            Result.failure(Exception("AI item generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini item generation failed", e)
            Result.failure(Exception(e.localizedMessage ?: "AI item generation failed."))
        }
    }

    private companion object {
        const val TAG = "AiQuestionRepo"
    }
}
