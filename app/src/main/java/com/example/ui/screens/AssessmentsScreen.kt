package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AssessmentPlan
import com.example.data.AssessmentStatus
import com.example.data.BloomLevel
import com.example.data.QuestionItem
import com.example.data.UiState
import com.example.ui.components.QuestionStimulusPreviewCard
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.BloomHighText
import com.example.ui.theme.BloomLowText
import com.example.ui.theme.BloomMidText
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.EmeraldKey
import com.example.ui.theme.EmeraldKeySurface
import com.example.ui.theme.EmeraldKeyText
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ErrorSurface
import com.example.ui.theme.OnErrorSurface
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceOffset

@Composable
fun AssessmentsScreen(
    assessmentsState: UiState<List<AssessmentPlan>>,
    questionsState: UiState<List<QuestionItem>>,
    selectedAssessmentId: String?,
    isWideScreen: Boolean,
    onSelectAssessment: (String?) -> Unit,
    onCreateAssessment: (String, String, String, String, List<String>, List<QuestionItem>) -> Unit,
    onToggleQuestion: (AssessmentPlan, String, List<QuestionItem>) -> Unit,
    onUpdateStatus: (String, AssessmentStatus) -> Unit,
    onDeleteAssessment: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showItemPickerFor by remember { mutableStateOf<AssessmentPlan?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    val allQuestions = (questionsState as? UiState.Success)?.data ?: emptyList()

    when (assessmentsState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AcademicBlue)
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ErrorSurface,
                    border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = assessmentsState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnErrorSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        is UiState.Success -> {
            val assessments = assessmentsState.data
            val selectedPlan = remember(assessments, selectedAssessmentId) {
                assessments.firstOrNull { it.id == selectedAssessmentId }
            }

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    ) {
                        AssessmentListPane(
                            assessments = assessments,
                            allQuestions = allQuestions,
                            selectedId = selectedPlan?.id,
                            onSelect = onSelectAssessment,
                            onOpenCreate = { showCreateDialog = true }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val active = selectedPlan ?: assessments.firstOrNull()
                        if (active != null) {
                            AssessmentDetailPane(
                                plan = active,
                                allQuestions = allQuestions,
                                showBackButton = false,
                                onBack = {},
                                onAddItems = { showItemPickerFor = active },
                                onRemoveItem = { qId -> onToggleQuestion(active, qId, allQuestions) },
                                onUpdateStatus = { st -> onUpdateStatus(active.id, st) },
                                onDelete = { pendingDeleteId = active.id }
                            )
                        } else {
                            EmptyAssessmentDetailPlaceholder()
                        }
                    }
                }
            } else {
                if (selectedPlan != null) {
                    BackHandler { onSelectAssessment(null) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AssessmentDetailPane(
                            plan = selectedPlan,
                            allQuestions = allQuestions,
                            showBackButton = true,
                            onBack = { onSelectAssessment(null) },
                            onAddItems = { showItemPickerFor = selectedPlan },
                            onRemoveItem = { qId -> onToggleQuestion(selectedPlan, qId, allQuestions) },
                            onUpdateStatus = { st -> onUpdateStatus(selectedPlan.id, st) },
                            onDelete = { pendingDeleteId = selectedPlan.id }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AssessmentListPane(
                            assessments = assessments,
                            allQuestions = allQuestions,
                            selectedId = null,
                            onSelect = onSelectAssessment,
                            onOpenCreate = { showCreateDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAssessmentDialog(
            availableQuestions = allQuestions,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, subject, grade, desc, ids ->
                onCreateAssessment(title, subject, grade, desc, ids, allQuestions)
                showCreateDialog = false
            }
        )
    }

    showItemPickerFor?.let { plan ->
        val freshPlan = (assessmentsState as? UiState.Success)?.data?.firstOrNull { it.id == plan.id } ?: plan
        PickQuestionsForAssessmentDialog(
            plan = freshPlan,
            allQuestions = allQuestions,
            onToggle = { qId -> onToggleQuestion(freshPlan, qId, allQuestions) },
            onDismiss = { showItemPickerFor = null }
        )
    }

    pendingDeleteId?.let { idToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            shape = RoundedCornerShape(8.dp),
            containerColor = SurfaceCard,
            title = { Text("Delete Assessment Matrix?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Your underlying questions in the Item Bank will remain preserved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAssessment(idToDelete)
                        pendingDeleteId = null
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson, contentColor = Color.White),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
private fun AssessmentListPane(
    assessments: List<AssessmentPlan>,
    allQuestions: List<QuestionItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onOpenCreate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Assessments",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SlateInk
                )
                Text(
                    text = "${assessments.size} exam matrices",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
            Button(
                onClick = onOpenCreate,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateInk, contentColor = Color.White),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_assessment_button")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Matrix", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (assessments.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FactCheck,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "No Assessment Matrices Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk
                    )
                    Text(
                        text = "Assemble saved items into balanced quizzes or exams with Bloom's cognitive distribution tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(assessments, key = { it.id }) { plan ->
                    val isSelected = plan.id == selectedId
                    val linkedQuestions = remember(plan.questionIds, allQuestions) {
                        plan.questionIds.mapNotNull { id -> allQuestions.firstOrNull { it.id == id } }
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceCard,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AcademicBlue else BorderHairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelect(plan.id) }
                            .testTag("assessment_card_${plan.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${plan.subject.uppercase()} · ${plan.gradeLevel.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcademicBlue
                                )
                                AssessmentStatusPill(plan.statusEnum)
                            }
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateInk,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            BloomDistributionBar(questions = linkedQuestions)
                            Text(
                                text = "${linkedQuestions.size} items · ${plan.totalMinutes} min total",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentDetailPane(
    plan: AssessmentPlan,
    allQuestions: List<QuestionItem>,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onAddItems: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onUpdateStatus: (AssessmentStatus) -> Unit,
    onDelete: () -> Unit
) {
    var showAnswerKeys by remember { mutableStateOf(true) }
    val linkedQuestions = remember(plan.questionIds, allQuestions) {
        plan.questionIds.mapNotNull { id -> allQuestions.firstOrNull { it.id == id } }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderHairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        if (showBackButton) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("assessment_back_button")) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = SlateInk)
                            }
                        }
                        Column {
                            Text(plan.title, style = MaterialTheme.typography.titleLarge, color = SlateInk)
                            Text(
                                "${plan.subject} · ${plan.gradeLevel} · ${linkedQuestions.size} items · ${plan.totalMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_assessment_button")) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete assessment", tint = ErrorCrimson)
                    }
                }

                if (plan.description.isNotBlank()) {
                    Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = SlateMuted)
                }

                // Cognitive Distribution Summary
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("BLOOM'S COGNITIVE BALANCE", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                    BloomDistributionBar(questions = linkedQuestions)
                }

                // Status Workflow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssessmentStatus.entries.forEach { st ->
                        val active = plan.statusEnum == st
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (active) EmeraldKeySurface else SurfaceOffset,
                            border = BorderStroke(1.dp, if (active) EmeraldKey else BorderHairline),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onUpdateStatus(st) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = st.displayLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) EmeraldKeyText else SlateMuted
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderHairline)

                // Student Preview vs Answer Key Mode + Add Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (showAnswerKeys) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = SlateInk,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (showAnswerKeys) "Answer Key Mode" else "Student Exam Mode",
                            style = MaterialTheme.typography.labelLarge,
                            color = SlateInk
                        )
                        Switch(
                            checked = showAnswerKeys,
                            onCheckedChange = { showAnswerKeys = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldKey),
                            modifier = Modifier.testTag("answer_key_mode_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = onAddItems,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BorderHairline),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("manage_matrix_items_button")
                    ) {
                        Icon(Icons.Outlined.PlaylistAdd, contentDescription = null, tint = AcademicBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Items", color = AcademicBlue, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (linkedQuestions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No Questions Linked Yet", style = MaterialTheme.typography.titleMedium, color = SlateInk)
                    Text(
                        "Tap Items above to attach calibrated questions from your Item Bank.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        } else {
            linkedQuestions.forEachIndexed { index, q ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateInk
                        )
                        TextButton(
                            onClick = { onRemoveItem(q.id) },
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) {
                            Text("Remove", color = ErrorCrimson, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    QuestionStimulusPreviewCard(
                        item = q,
                        showAnswerKey = showAnswerKeys,
                        interactiveKeySelection = false
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BloomDistributionBar(questions: List<QuestionItem>) {
    val total = questions.size.coerceAtLeast(1)
    val lowCount = questions.count { it.bloomEnum in listOf(BloomLevel.C1, BloomLevel.C2) }
    val midCount = questions.count { it.bloomEnum in listOf(BloomLevel.C3, BloomLevel.C4) }
    val highCount = questions.count { it.bloomEnum in listOf(BloomLevel.C5, BloomLevel.C6) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceOffset)
        ) {
            if (questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().background(BorderHairline))
            } else {
                if (lowCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(lowCount.toFloat() / total)
                            .fillMaxHeight()
                            .background(BloomLowText)
                    )
                }
                if (midCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(midCount.toFloat() / total)
                            .fillMaxHeight()
                            .background(Color(0xFFF59E0B))
                    )
                }
                if (highCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(highCount.toFloat() / total)
                            .fillMaxHeight()
                            .background(Color(0xFFEA580C))
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("C1–C2: $lowCount", style = MaterialTheme.typography.labelSmall, color = BloomLowText)
            Text("C3–C4: $midCount", style = MaterialTheme.typography.labelSmall, color = BloomMidText)
            Text("C5–C6: $highCount", style = MaterialTheme.typography.labelSmall, color = BloomHighText)
        }
    }
}

@Composable
private fun AssessmentStatusPill(status: AssessmentStatus) {
    val (bg, fg) = when (status) {
        AssessmentStatus.DRAFT -> SurfaceOffset to SlateMuted
        AssessmentStatus.READY -> Color(0xFFEFF6FF) to AcademicBlue
        AssessmentStatus.PUBLISHED -> EmeraldKeySurface to EmeraldKeyText
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = BorderStroke(1.dp, BorderHairline)
    ) {
        Text(
            text = status.displayLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyAssessmentDetailPlaceholder() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderHairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select an assessment matrix to inspect its Bloom's cognitive balance and preview exam items.",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )
        }
    }
}

@Composable
private fun CreateAssessmentDialog(
    availableQuestions: List<QuestionItem>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Biology") }
    var grade by remember { mutableStateOf("Grade 11") }
    var description by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderHairline),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Assessment Matrix", style = MaterialTheme.typography.titleMedium, color = SlateInk)
                    TextButton(onClick = onDismiss) { Text("Cancel", color = SlateMuted) }
                }
                HorizontalDivider(color = BorderHairline)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AcademicTextField("ASSESSMENT TITLE", title, { title = it }, placeholder = "e.g., Midterm Bioenergetics Matrix")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AcademicTextField("SUBJECT", subject, { subject = it }, modifier = Modifier.weight(1f))
                        AcademicTextField("GRADE / LEVEL", grade, { grade = it }, modifier = Modifier.weight(0.8f))
                    }
                    AcademicTextField("INSTRUCTIONS / SCOPE", description, { description = it }, minLines = 2, maxLines = 3)

                    if (availableQuestions.isNotEmpty()) {
                        Text("INCLUDE ITEMS FROM BANK (${selectedIds.size} SELECTED)", style = MaterialTheme.typography.labelMedium, color = SlateInk)
                        availableQuestions.forEach { q ->
                            val checked = q.id in selectedIds
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (checked) SurfaceOffset else SurfaceCard,
                                border = BorderStroke(1.dp, if (checked) AcademicBlue else BorderHairline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (checked) selectedIds.remove(q.id) else selectedIds.add(q.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            if (checked) selectedIds.remove(q.id) else selectedIds.add(q.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AcademicBlue)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(q.questionStem, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = SlateInk)
                                        Text("${q.bloomEnum.code} · ${q.subject}", style = MaterialTheme.typography.bodySmall, color = SlateMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderHairline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onCreate(
                                title.trim(),
                                subject.trim().ifBlank { "General" },
                                grade.trim().ifBlank { "Grade 11" },
                                description.trim(),
                                selectedIds.toList()
                            )
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateInk, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Create Matrix")
                    }
                }
            }
        }
    }
}

@Composable
private fun PickQuestionsForAssessmentDialog(
    plan: AssessmentPlan,
    allQuestions: List<QuestionItem>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SurfaceCard,
        title = { Text("Manage Matrix Items", style = MaterialTheme.typography.titleLarge) },
        text = {
            if (allQuestions.isEmpty()) {
                Text(
                    "Your Item Bank is empty. Generate or author questions first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allQuestions.forEach { q ->
                        val included = q.id in plan.questionIds
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (included) EmeraldKeySurface else SurfaceOffset,
                            border = BorderStroke(1.dp, if (included) EmeraldKey else BorderHairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onToggle(q.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = included,
                                    onCheckedChange = { onToggle(q.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldKey)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(q.questionStem, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = SlateInk)
                                    Text("${q.bloomEnum.code} · ${q.subject} · ${q.estimatedMinutes}m", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateInk, contentColor = Color.White),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Done")
            }
        }
    )
}
