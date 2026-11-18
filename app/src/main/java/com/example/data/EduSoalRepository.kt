package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EduSoalRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireVerifiedUserId(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user access." }
        return uid
    }

    fun observeQuestions(userId: String): Flow<List<QuestionItem>> {
        val path = "users/$userId/questions"
        return db.collection("users").document(userId).collection("questions")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(QuestionItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt ?: it.createdAt }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAssessments(userId: String): Flow<List<AssessmentPlan>> {
        val path = "users/$userId/assessments"
        return db.collection("users").document(userId).collection("assessments")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AssessmentPlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt ?: it.createdAt }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createQuestion(userId: String, item: QuestionItem): Result<String> {
        var docPath = "users/$userId/questions"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanSubject = item.subject.trim().take(60)
            val cleanGrade = item.gradeLevel.trim().take(40)
            val cleanTopic = item.topic.trim().take(150)
            val cleanStem = item.questionStem.trim().take(2000)

            require(cleanSubject.isNotEmpty()) { "Subject must not be empty." }
            require(cleanGrade.isNotEmpty()) { "Grade level must not be empty." }
            require(cleanTopic.isNotEmpty()) { "Topic must not be empty." }
            require(cleanStem.isNotEmpty()) { "Question stem must not be empty." }

            val docRef = db.collection("users").document(uid).collection("questions").document()
            docPath = docRef.path

            val safeOptions = item.options.take(6).map { it.trim() }.filter { it.isNotEmpty() }
            val safeCorrectIndex = if (safeOptions.isEmpty()) -1L else item.correctOptionIndex.coerceIn(0L, safeOptions.lastIndex.toLong())

            val payload = mapOf(
                "userId" to uid,
                "subject" to cleanSubject,
                "gradeLevel" to cleanGrade,
                "topic" to cleanTopic,
                "bloomLevel" to BloomLevel.fromCode(item.bloomLevel).code,
                "questionFormat" to QuestionFormat.fromWire(item.questionFormat).wireName,
                "difficulty" to DifficultyLevel.fromWire(item.difficulty).wireName,
                "stimulusTitle" to item.stimulusTitle.trim().take(160),
                "stimulusContent" to item.stimulusContent.trim().take(4000),
                "questionStem" to cleanStem,
                "options" to safeOptions,
                "correctOptionIndex" to safeCorrectIndex,
                "pedagogicalRationale" to item.pedagogicalRationale.trim().take(2000),
                "scoringRubric" to item.scoringRubric.trim().take(2000),
                "curriculumAlignment" to item.curriculumAlignment.trim().take(200),
                "estimatedMinutes" to item.estimatedMinutes.coerceIn(1L, 120L),
                "discriminationIndex" to item.discriminationIndex.coerceIn(0.0, 1.0),
                "reviewStatus" to ReviewStatus.fromWire(item.reviewStatus).wireName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateQuestion(userId: String, item: QuestionItem): Result<Unit> {
        val docPath = "users/$userId/questions/${item.id}"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(item.id.isNotBlank()) { "Question ID is required for update." }
            val cleanStem = item.questionStem.trim().take(2000)
            require(cleanStem.isNotEmpty()) { "Question stem must not be empty." }

            val safeOptions = item.options.take(6).map { it.trim() }.filter { it.isNotEmpty() }
            val safeCorrectIndex = if (safeOptions.isEmpty()) -1L else item.correctOptionIndex.coerceIn(0L, safeOptions.lastIndex.toLong())

            val updates = mapOf(
                "subject" to item.subject.trim().take(60).ifEmpty { "General" },
                "gradeLevel" to item.gradeLevel.trim().take(40).ifEmpty { "Grade 11" },
                "topic" to item.topic.trim().take(150).ifEmpty { "Assessment Item" },
                "bloomLevel" to BloomLevel.fromCode(item.bloomLevel).code,
                "questionFormat" to QuestionFormat.fromWire(item.questionFormat).wireName,
                "difficulty" to DifficultyLevel.fromWire(item.difficulty).wireName,
                "stimulusTitle" to item.stimulusTitle.trim().take(160),
                "stimulusContent" to item.stimulusContent.trim().take(4000),
                "questionStem" to cleanStem,
                "options" to safeOptions,
                "correctOptionIndex" to safeCorrectIndex,
                "pedagogicalRationale" to item.pedagogicalRationale.trim().take(2000),
                "scoringRubric" to item.scoringRubric.trim().take(2000),
                "curriculumAlignment" to item.curriculumAlignment.trim().take(200),
                "estimatedMinutes" to item.estimatedMinutes.coerceIn(1L, 120L),
                "discriminationIndex" to item.discriminationIndex.coerceIn(0.0, 1.0),
                "reviewStatus" to ReviewStatus.fromWire(item.reviewStatus).wireName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("questions").document(item.id)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateQuestionStatus(userId: String, questionId: String, status: ReviewStatus): Result<Unit> {
        val docPath = "users/$userId/questions/$questionId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(questionId.isNotBlank()) { "Question ID is required." }
            db.collection("users").document(uid).collection("questions").document(questionId)
                .update(
                    mapOf(
                        "reviewStatus" to status.wireName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCorrectOption(userId: String, questionId: String, correctOptionIndex: Long): Result<Unit> {
        val docPath = "users/$userId/questions/$questionId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(questionId.isNotBlank()) { "Question ID is required." }
            require(correctOptionIndex in -1L..5L) { "Option index out of bounds." }
            db.collection("users").document(uid).collection("questions").document(questionId)
                .update(
                    mapOf(
                        "correctOptionIndex" to correctOptionIndex,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteQuestion(userId: String, questionId: String): Result<Unit> {
        val docPath = "users/$userId/questions/$questionId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(questionId.isNotBlank()) { "Question ID is required." }
            db.collection("users").document(uid).collection("questions").document(questionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createAssessment(userId: String, plan: AssessmentPlan): Result<String> {
        var docPath = "users/$userId/assessments"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanTitle = plan.title.trim().take(120)
            val cleanSubject = plan.subject.trim().take(60).ifEmpty { "General" }
            val cleanGrade = plan.gradeLevel.trim().take(40).ifEmpty { "Grade 11" }
            require(cleanTitle.isNotEmpty()) { "Assessment title must not be empty." }

            val docRef = db.collection("users").document(uid).collection("assessments").document()
            docPath = docRef.path
            val safeIds = plan.questionIds.distinct().take(50)

            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "subject" to cleanSubject,
                "gradeLevel" to cleanGrade,
                "description" to plan.description.trim().take(600),
                "questionIds" to safeIds,
                "totalMinutes" to plan.totalMinutes.coerceIn(0L, 600L),
                "status" to AssessmentStatus.fromWire(plan.status).wireName,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateAssessment(userId: String, plan: AssessmentPlan): Result<Unit> {
        val docPath = "users/$userId/assessments/${plan.id}"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(plan.id.isNotBlank()) { "Assessment ID is required." }
            val cleanTitle = plan.title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Assessment title must not be empty." }

            val updates = mapOf(
                "title" to cleanTitle,
                "subject" to plan.subject.trim().take(60).ifEmpty { "General" },
                "gradeLevel" to plan.gradeLevel.trim().take(40).ifEmpty { "Grade 11" },
                "description" to plan.description.trim().take(600),
                "questionIds" to plan.questionIds.distinct().take(50),
                "totalMinutes" to plan.totalMinutes.coerceIn(0L, 600L),
                "status" to AssessmentStatus.fromWire(plan.status).wireName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("assessments").document(plan.id)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateAssessmentStatus(userId: String, assessmentId: String, status: AssessmentStatus): Result<Unit> {
        val docPath = "users/$userId/assessments/$assessmentId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(assessmentId.isNotBlank()) { "Assessment ID is required." }
            db.collection("users").document(uid).collection("assessments").document(assessmentId)
                .update(
                    mapOf(
                        "status" to status.wireName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteAssessment(userId: String, assessmentId: String): Result<Unit> {
        val docPath = "users/$userId/assessments/$assessmentId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(assessmentId.isNotBlank()) { "Assessment ID is required." }
            db.collection("users").document(uid).collection("assessments").document(assessmentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
