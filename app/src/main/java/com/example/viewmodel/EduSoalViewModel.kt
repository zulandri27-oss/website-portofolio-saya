package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiGenerationUiState
import com.example.data.AiQuestionRepository
import com.example.data.AssessmentPlan
import com.example.data.AssessmentStatus
import com.example.data.BloomLevel
import com.example.data.DifficultyLevel
import com.example.data.EduSoalRepository
import com.example.data.QuestionFormat
import com.example.data.QuestionItem
import com.example.data.ReviewStatus
import com.example.data.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneratorFormState(
    val subject: String = "Biology",
    val gradeLevel: String = "Grade 11",
    val topic: String = "",
    val bloomLevel: BloomLevel = BloomLevel.C4,
    val format: QuestionFormat = QuestionFormat.MULTIPLE_CHOICE,
    val difficulty: DifficultyLevel = DifficultyLevel.HOTS,
    val customStimulus: String = ""
)

data class BankFilterState(
    val searchQuery: String = "",
    val selectedBloomTier: String = "ALL", // ALL, LOW, MID, HIGH
    val selectedStatus: ReviewStatus? = null
)

class EduSoalViewModel(
    private val aiRepository: AiQuestionRepository,
    private val repository: EduSoalRepository,
    private val currentUserId: String
) : ViewModel() {

    val questionsState: StateFlow<UiState<List<QuestionItem>>> =
        repository.observeQuestions(currentUserId)
            .map<List<QuestionItem>, UiState<List<QuestionItem>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing questions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load Item Bank"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val assessmentsState: StateFlow<UiState<List<AssessmentPlan>>> =
        repository.observeAssessments(currentUserId)
            .map<List<AssessmentPlan>, UiState<List<AssessmentPlan>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing assessments", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load Assessments"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val _formState = MutableStateFlow(GeneratorFormState())
    val formState: StateFlow<GeneratorFormState> = _formState.asStateFlow()

    private val _aiState = MutableStateFlow<AiGenerationUiState>(AiGenerationUiState.Idle)
    val aiState: StateFlow<AiGenerationUiState> = _aiState.asStateFlow()

    private val _bankFilter = MutableStateFlow(BankFilterState())
    val bankFilter: StateFlow<BankFilterState> = _bankFilter.asStateFlow()

    private val _selectedBankQuestionId = MutableStateFlow<String?>(null)
    val selectedBankQuestionId: StateFlow<String?> = _selectedBankQuestionId.asStateFlow()

    private val _selectedAssessmentId = MutableStateFlow<String?>(null)
    val selectedAssessmentId: StateFlow<String?> = _selectedAssessmentId.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    fun updateSubject(value: String) = _formState.update { it.copy(subject = value) }
    fun updateGradeLevel(value: String) = _formState.update { it.copy(gradeLevel = value) }
    fun updateTopic(value: String) = _formState.update { it.copy(topic = value) }
    fun updateBloomLevel(level: BloomLevel) = _formState.update { it.copy(bloomLevel = level) }
    fun updateFormat(format: QuestionFormat) = _formState.update { it.copy(format = format) }
    fun updateDifficulty(difficulty: DifficultyLevel) = _formState.update { it.copy(difficulty = difficulty) }
    fun updateCustomStimulus(stimulus: String) = _formState.update { it.copy(customStimulus = stimulus) }

    fun applyQuickPreset(subject: String, grade: String, topic: String, bloom: BloomLevel, format: QuestionFormat) {
        _formState.update {
            it.copy(
                subject = subject,
                gradeLevel = grade,
                topic = topic,
                bloomLevel = bloom,
                format = format,
                difficulty = if (bloom.ordinal >= 3) DifficultyLevel.HOTS else DifficultyLevel.STANDARD
            )
        }
    }

    fun generateItem() {
        val current = _formState.value
        if (current.topic.isBlank()) {
            _aiState.value = AiGenerationUiState.Error("Enter a curriculum topic or learning objective first.")
            return
        }
        viewModelScope.launch {
            _aiState.value = AiGenerationUiState.Generating
            val result = aiRepository.generateAssessmentItem(
                userId = currentUserId,
                subject = current.subject,
                gradeLevel = current.gradeLevel,
                topic = current.topic,
                bloomLevel = current.bloomLevel,
                format = current.format,
                difficulty = current.difficulty,
                customStimulus = current.customStimulus
            )
            result.fold(
                onSuccess = { generated ->
                    _aiState.value = AiGenerationUiState.Generated(item = generated, savedToBankId = null)
                },
                onFailure = { error ->
                    _aiState.value = AiGenerationUiState.Error(error.message ?: "Unable to generate item.")
                }
            )
        }
    }

    fun setGeneratedCorrectOption(index: Long) {
        val current = _aiState.value as? AiGenerationUiState.Generated ?: return
        val updated = current.item.copy(correctOptionIndex = index)
        _aiState.value = current.copy(item = updated)
        val savedId = current.savedToBankId
        if (!savedId.isNullOrBlank()) {
            viewModelScope.launch {
                repository.updateCorrectOption(currentUserId, savedId, index)
            }
        }
    }

    fun updateGeneratedItemDraft(updated: QuestionItem) {
        val current = _aiState.value as? AiGenerationUiState.Generated ?: return
        _aiState.value = current.copy(item = updated)
    }

    fun saveGeneratedItemToBank() {
        val current = _aiState.value as? AiGenerationUiState.Generated ?: return
        if (current.savedToBankId != null || _isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.createQuestion(currentUserId, current.item)
            _isSaving.value = false
            result.fold(
                onSuccess = { newId ->
                    _aiState.value = current.copy(savedToBankId = newId)
                    _snackbarEvents.emit("Item saved to Item Bank")
                },
                onFailure = { error ->
                    _snackbarEvents.emit("Save failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            )
        }
    }

    fun createManualQuestion(item: QuestionItem) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.createQuestion(currentUserId, item.copy(userId = currentUserId))
            _isSaving.value = false
            result.fold(
                onSuccess = { _snackbarEvents.emit("New item added to Item Bank") },
                onFailure = { err -> _snackbarEvents.emit("Create failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun updateExistingQuestion(item: QuestionItem) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateQuestion(currentUserId, item)
            _isSaving.value = false
            result.fold(
                onSuccess = { _snackbarEvents.emit("Item updated") },
                onFailure = { err -> _snackbarEvents.emit("Update failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun updateQuestionStatus(questionId: String, status: ReviewStatus) {
        viewModelScope.launch {
            repository.updateQuestionStatus(currentUserId, questionId, status).fold(
                onSuccess = { _snackbarEvents.emit("Status updated to ${status.displayLabel}") },
                onFailure = { err -> _snackbarEvents.emit("Status update failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun updateBankQuestionCorrectOption(questionId: String, index: Long) {
        viewModelScope.launch {
            repository.updateCorrectOption(currentUserId, questionId, index).fold(
                onSuccess = { _snackbarEvents.emit("Answer key updated") },
                onFailure = { err -> _snackbarEvents.emit("Key update failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            repository.deleteQuestion(currentUserId, questionId).fold(
                onSuccess = {
                    if (_selectedBankQuestionId.value == questionId) {
                        _selectedBankQuestionId.value = null
                    }
                    _snackbarEvents.emit("Item removed from bank")
                },
                onFailure = { err -> _snackbarEvents.emit("Delete failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun selectBankQuestion(questionId: String?) {
        _selectedBankQuestionId.value = questionId
    }

    fun updateSearchQuery(query: String) = _bankFilter.update { it.copy(searchQuery = query) }
    fun updateBloomTierFilter(tier: String) = _bankFilter.update { it.copy(selectedBloomTier = tier) }
    fun updateStatusFilter(status: ReviewStatus?) = _bankFilter.update { it.copy(selectedStatus = status) }

    fun selectAssessment(assessmentId: String?) {
        _selectedAssessmentId.value = assessmentId
    }

    fun createAssessment(title: String, subject: String, gradeLevel: String, description: String, initialQuestionIds: List<String>, questions: List<QuestionItem>) {
        viewModelScope.launch {
            val totalMins = initialQuestionIds.sumOf { id ->
                questions.firstOrNull { it.id == id }?.estimatedMinutes ?: 3L
            }.coerceIn(0L, 600L)
            val plan = AssessmentPlan(
                userId = currentUserId,
                title = title,
                subject = subject,
                gradeLevel = gradeLevel,
                description = description,
                questionIds = initialQuestionIds,
                totalMinutes = totalMins,
                status = AssessmentStatus.DRAFT.wireName
            )
            repository.createAssessment(currentUserId, plan).fold(
                onSuccess = { newId ->
                    _selectedAssessmentId.value = newId
                    _snackbarEvents.emit("Assessment matrix created")
                },
                onFailure = { err -> _snackbarEvents.emit("Create failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun toggleQuestionInAssessment(assessment: AssessmentPlan, questionId: String, allQuestions: List<QuestionItem>) {
        viewModelScope.launch {
            val updatedIds = if (questionId in assessment.questionIds) {
                assessment.questionIds - questionId
            } else {
                (assessment.questionIds + questionId).take(50)
            }
            val totalMins = updatedIds.sumOf { id ->
                allQuestions.firstOrNull { it.id == id }?.estimatedMinutes ?: 3L
            }.coerceIn(0L, 600L)
            val updatedPlan = assessment.copy(questionIds = updatedIds, totalMinutes = totalMins)
            repository.updateAssessment(currentUserId, updatedPlan).fold(
                onSuccess = {
                    val action = if (questionId in updatedIds) "added to" else "removed from"
                    _snackbarEvents.emit("Item $action ${assessment.title}")
                },
                onFailure = { err -> _snackbarEvents.emit("Update failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun updateAssessmentStatus(assessmentId: String, status: AssessmentStatus) {
        viewModelScope.launch {
            repository.updateAssessmentStatus(currentUserId, assessmentId, status).fold(
                onSuccess = { _snackbarEvents.emit("Assessment marked ${status.displayLabel}") },
                onFailure = { err -> _snackbarEvents.emit("Status update failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    fun deleteAssessment(assessmentId: String) {
        viewModelScope.launch {
            repository.deleteAssessment(currentUserId, assessmentId).fold(
                onSuccess = {
                    if (_selectedAssessmentId.value == assessmentId) {
                        _selectedAssessmentId.value = null
                    }
                    _snackbarEvents.emit("Assessment deleted")
                },
                onFailure = { err -> _snackbarEvents.emit("Delete failed: ${err.localizedMessage ?: "Error"}") }
            )
        }
    }

    private companion object {
        const val TAG = "EduSoalVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
