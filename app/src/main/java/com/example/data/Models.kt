package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class BloomLevel(
    val code: String,
    val title: String,
    val shortTitle: String,
    val tierDescription: String
) {
    C1("C1", "C1 · Recall", "Recall", "Retrieve factual knowledge from memory"),
    C2("C2", "C2 · Comprehension", "Understand", "Construct meaning and summarize concepts"),
    C3("C3", "C3 · Application", "Apply", "Execute procedures in novel situations"),
    C4("C4", "C4 · Analysis", "Analyze", "Deconstruct relationships and patterns"),
    C5("C5", "C5 · Evaluation", "Evaluate", "Critique claims against criteria and evidence"),
    C6("C6", "C6 · Creation", "Create", "Synthesize original hypotheses or designs");

    companion object {
        fun fromCode(raw: String?): BloomLevel =
            entries.firstOrNull { it.code.equals(raw, ignoreCase = true) || it.name.equals(raw, ignoreCase = true) } ?: C4
    }
}

enum class QuestionFormat(val wireName: String, val displayLabel: String) {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "Multiple Choice"),
    COMPLEX_MC("COMPLEX_MC", "Complex Analysis"),
    ESSAY("ESSAY", "Essay & Rubric");

    companion object {
        fun fromWire(raw: String?): QuestionFormat =
            entries.firstOrNull { it.wireName == raw || it.name == raw } ?: MULTIPLE_CHOICE
    }
}

enum class DifficultyLevel(val wireName: String, val displayLabel: String) {
    FOUNDATIONAL("FOUNDATIONAL", "Foundational"),
    STANDARD("STANDARD", "Standard"),
    HOTS("HOTS", "HOTS");

    companion object {
        fun fromWire(raw: String?): DifficultyLevel =
            entries.firstOrNull { it.wireName == raw || it.name == raw } ?: STANDARD
    }
}

enum class ReviewStatus(val wireName: String, val displayLabel: String) {
    DRAFT("DRAFT", "Draft"),
    VALIDATED("VALIDATED", "Validated"),
    APPROVED("APPROVED", "Approved");

    companion object {
        fun fromWire(raw: String?): ReviewStatus =
            entries.firstOrNull { it.wireName == raw || it.name == raw } ?: DRAFT
    }
}

enum class AssessmentStatus(val wireName: String, val displayLabel: String) {
    DRAFT("DRAFT", "Draft"),
    READY("READY", "Ready"),
    PUBLISHED("PUBLISHED", "Published");

    companion object {
        fun fromWire(raw: String?): AssessmentStatus =
            entries.firstOrNull { it.wireName == raw || it.name == raw } ?: DRAFT
    }
}

@Keep
@IgnoreExtraProperties
data class QuestionItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val topic: String = "",
    val bloomLevel: String = BloomLevel.C4.code,
    val questionFormat: String = QuestionFormat.MULTIPLE_CHOICE.wireName,
    val difficulty: String = DifficultyLevel.STANDARD.wireName,
    val stimulusTitle: String = "",
    val stimulusContent: String = "",
    val questionStem: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Long = 0L,
    val pedagogicalRationale: String = "",
    val scoringRubric: String = "",
    val curriculumAlignment: String = "",
    val estimatedMinutes: Long = 3L,
    val discriminationIndex: Double = 0.40,
    val reviewStatus: String = ReviewStatus.DRAFT.wireName,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val bloomEnum: BloomLevel get() = BloomLevel.fromCode(bloomLevel)
    val formatEnum: QuestionFormat get() = QuestionFormat.fromWire(questionFormat)
    val difficultyEnum: DifficultyLevel get() = DifficultyLevel.fromWire(difficulty)
    val reviewStatusEnum: ReviewStatus get() = ReviewStatus.fromWire(reviewStatus)
}

@Keep
@IgnoreExtraProperties
data class AssessmentPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val description: String = "",
    val questionIds: List<String> = emptyList(),
    val totalMinutes: Long = 0L,
    val status: String = AssessmentStatus.DRAFT.wireName,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: AssessmentStatus get() = AssessmentStatus.fromWire(status)
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiGenerationUiState {
    data object Idle : AiGenerationUiState
    data object Generating : AiGenerationUiState
    data class Generated(val item: QuestionItem, val savedToBankId: String? = null) : AiGenerationUiState
    data class Error(val message: String) : AiGenerationUiState
}
