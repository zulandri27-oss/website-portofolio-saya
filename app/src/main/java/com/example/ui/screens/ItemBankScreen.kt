package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.BloomLevel
import com.example.data.DifficultyLevel
import com.example.data.QuestionFormat
import com.example.data.QuestionItem
import com.example.data.ReviewStatus
import com.example.data.UiState
import com.example.ui.components.BloomBadge
import com.example.ui.components.PedagogicalMetadataInspector
import com.example.ui.components.QuestionStimulusPreviewCard
import com.example.ui.components.ReviewStatusBadge
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BorderInput
import com.example.ui.theme.EmeraldKey
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ErrorSurface
import com.example.ui.theme.OnErrorSurface
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceOffset
import com.example.viewmodel.BankFilterState

@Composable
fun ItemBankScreen(
    questionsState: UiState<List<QuestionItem>>,
    assessmentsState: UiState<List<AssessmentPlan>>,
    filterState: BankFilterState,
    selectedQuestionId: String?,
    isWideScreen: Boolean,
    onSearchChange: (String) -> Unit,
    onBloomTierChange: (String) -> Unit,
    onStatusFilterChange: (ReviewStatus?) -> Unit,
    onSelectQuestion: (String?) -> Unit,
    onUpdateCorrectOption: (String, Long) -> Unit,
    onUpdateStatus: (String, ReviewStatus) -> Unit,
    onUpdateQuestion: (QuestionItem) -> Unit,
    onCreateManualQuestion: (QuestionItem) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onToggleInAssessment: (AssessmentPlan, String, List<QuestionItem>) -> Unit
) {
    var showCreateManualDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionItem?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var assignToAssessmentQuestionId by remember { mutableStateOf<String?>(null) }

    when (questionsState) {
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
                        text = questionsState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnErrorSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        is UiState.Success -> {
            val allQuestions = questionsState.data
            val filteredQuestions = remember(allQuestions, filterState) {
                allQuestions.filter { item ->
                    val matchesQuery = filterState.searchQuery.isBlank() ||
                        item.questionStem.contains(filterState.searchQuery, ignoreCase = true) ||
                        item.topic.contains(filterState.searchQuery, ignoreCase = true) ||
                        item.subject.contains(filterState.searchQuery, ignoreCase = true)
                    val matchesBloom = when (filterState.selectedBloomTier) {
                        "LOW" -> item.bloomEnum in listOf(BloomLevel.C1, BloomLevel.C2)
                        "MID" -> item.bloomEnum in listOf(BloomLevel.C3, BloomLevel.C4)
                        "HIGH" -> item.bloomEnum in listOf(BloomLevel.C5, BloomLevel.C6)
                        else -> true
                    }
                    val matchesStatus = filterState.selectedStatus == null ||
                        item.reviewStatusEnum == filterState.selectedStatus
                    matchesQuery && matchesBloom && matchesStatus
                }
            }

            val selectedQuestion = remember(allQuestions, selectedQuestionId) {
                allQuestions.firstOrNull { it.id == selectedQuestionId }
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
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        ItemBankListPane(
                            questions = filteredQuestions,
                            totalCount = allQuestions.size,
                            filterState = filterState,
                            selectedQuestionId = selectedQuestion?.id,
                            onSearchChange = onSearchChange,
                            onBloomTierChange = onBloomTierChange,
                            onStatusFilterChange = onStatusFilterChange,
                            onSelectQuestion = { onSelectQuestion(it) },
                            onOpenCreateManual = { showCreateManualDialog = true }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val active = selectedQuestion ?: filteredQuestions.firstOrNull()
                        if (active != null) {
                            ItemDetailPane(
                                item = active,
                                showBackButton = false,
                                onBack = {},
                                onSelectOption = { idx -> onUpdateCorrectOption(active.id, idx.toLong()) },
                                onUpdateStatus = { status -> onUpdateStatus(active.id, status) },
                                onEdit = { editingQuestion = active },
                                onAssignToAssessment = { assignToAssessmentQuestionId = active.id },
                                onDelete = { pendingDeleteId = active.id }
                            )
                        } else {
                            EmptyDetailPlaceholder()
                        }
                    }
                }
            } else {
                if (selectedQuestion != null) {
                    BackHandler { onSelectQuestion(null) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        ItemDetailPane(
                            item = selectedQuestion,
                            showBackButton = true,
                            onBack = { onSelectQuestion(null) },
                            onSelectOption = { idx -> onUpdateCorrectOption(selectedQuestion.id, idx.toLong()) },
                            onUpdateStatus = { status -> onUpdateStatus(selectedQuestion.id, status) },
                            onEdit = { editingQuestion = selectedQuestion },
                            onAssignToAssessment = { assignToAssessmentQuestionId = selectedQuestion.id },
                            onDelete = { pendingDeleteId = selectedQuestion.id }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        ItemBankListPane(
                            questions = filteredQuestions,
                            totalCount = allQuestions.size,
                            filterState = filterState,
                            selectedQuestionId = null,
                            onSearchChange = onSearchChange,
                            onBloomTierChange = onBloomTierChange,
                            onStatusFilterChange = onStatusFilterChange,
                            onSelectQuestion = { onSelectQuestion(it) },
                            onOpenCreateManual = { showCreateManualDialog = true }
                        )
                    }
                }
            }

            if (assignToAssessmentQuestionId != null) {
                val qId = assignToAssessmentQuestionId!!
                val assessments = (assessmentsState as? UiState.Success)?.data ?: emptyList()
                AssignToAssessmentDialog(
                    assessments = assessments,
                    questionId = qId,
                    onToggle = { plan -> onToggleInAssessment(plan, qId, allQuestions) },
                    onDismiss = { assignToAssessmentQuestionId = null }
                )
            }
        }
    }

    editingQuestion?.let { q ->
        EditQuestionDialog(
            initialItem = q,
            onDismiss = { editingQuestion = null },
            onSave = { updated ->
                onUpdateQuestion(updated)
                editingQuestion = null
            }
        )
    }

    if (showCreateManualDialog) {
        CreateManualQuestionDialog(
            onDismiss = { showCreateManualDialog = false },
            onCreate = { newItem ->
                onCreateManualQuestion(newItem)
                showCreateManualDialog = false
            }
        )
    }

    pendingDeleteId?.let { idToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            shape = RoundedCornerShape(8.dp),
            containerColor = SurfaceCard,
            title = { Text("Delete Question Item?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "This removes the item permanently from your synchronized Item Bank.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(idToDelete)
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
private fun ItemBankListPane(
    questions: List<QuestionItem>,
    totalCount: Int,
    filterState: BankFilterState,
    selectedQuestionId: String?,
    onSearchChange: (String) -> Unit,
    onBloomTierChange: (String) -> Unit,
    onStatusFilterChange: (ReviewStatus?) -> Unit,
    onSelectQuestion: (String) -> Unit,
    onOpenCreateManual: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Manual Add
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Item Bank",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SlateInk
                )
                Text(
                    text = "$totalCount calibrated items stored",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
            OutlinedButton(
                onClick = onOpenCreateManual,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("add_manual_item_button")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = SlateInk, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Item", color = SlateInk, style = MaterialTheme.typography.labelLarge)
            }
        }

        // Search Box
        OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search items", tint = SlateMuted) },
            placeholder = { Text("Search topic, subject, or stem...", style = MaterialTheme.typography.bodyMedium, color = SlateMuted) },
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AcademicBlue,
                unfocusedBorderColor = BorderInput,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bank_search_input")
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChipBox("All Tiers", filterState.selectedBloomTier == "ALL") { onBloomTierChange("ALL") }
            FilterChipBox("C1–C2 Recall", filterState.selectedBloomTier == "LOW") { onBloomTierChange("LOW") }
            FilterChipBox("C3–C4 Apply", filterState.selectedBloomTier == "MID") { onBloomTierChange("MID") }
            FilterChipBox("C5–C6 HOTS", filterState.selectedBloomTier == "HIGH") { onBloomTierChange("HIGH") }
            ReviewStatus.entries.forEach { st ->
                val active = filterState.selectedStatus == st
                FilterChipBox(st.displayLabel, active) {
                    onStatusFilterChange(if (active) null else st)
                }
            }
        }

        if (questions.isEmpty()) {
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
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (totalCount == 0) "Your Item Bank is Empty" else "No Matching Items",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk
                    )
                    Text(
                        text = if (totalCount == 0) {
                            "Generate an AI assessment item in the Generator tab or tap New Item to author one directly."
                        } else {
                            "Try clearing your search query or taxonomic filter."
                        },
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
                items(questions, key = { it.id }) { item ->
                    val isSelected = item.id == selectedQuestionId
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceCard,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AcademicBlue else BorderHairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectQuestion(item.id) }
                            .testTag("bank_item_card_${item.id}")
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
                                    text = "${item.subject.uppercase()} · ${item.gradeLevel.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcademicBlue
                                )
                                ReviewStatusBadge(status = item.reviewStatusEnum)
                            }
                            Text(
                                text = item.questionStem,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SlateInk,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BloomBadge(bloomLevel = item.bloomEnum)
                                Text(
                                    text = "${item.estimatedMinutes} min · ${item.formatEnum.displayLabel}",
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
}

@Composable
private fun ItemDetailPane(
    item: QuestionItem,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSelectOption: (Int) -> Unit,
    onUpdateStatus: (ReviewStatus) -> Unit,
    onEdit: () -> Unit,
    onAssignToAssessment: () -> Unit,
    onDelete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Detail Header Bar
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderHairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("detail_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back to Item Bank",
                                    tint = SlateInk
                                )
                            }
                        }
                        Column {
                            Text(
                                text = item.topic,
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.subject} · ${item.gradeLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_item_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete item",
                            tint = ErrorCrimson
                        )
                    }
                }

                // Review Status Workflow Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReviewStatus.entries.forEach { status ->
                        val active = item.reviewStatusEnum == status
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (active) EmeraldKey.copy(alpha = 0.12f) else SurfaceOffset,
                            border = BorderStroke(1.dp, if (active) EmeraldKey else BorderHairline),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onUpdateStatus(status) }
                                .testTag("status_step_${status.wireName}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = status.displayLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) EmeraldKey else SlateMuted
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onAssignToAssessment,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, BorderHairline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("assign_to_assessment_button")
                ) {
                    Icon(Icons.Outlined.AssignmentTurnedIn, contentDescription = null, tint = AcademicBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Include in Assessment Matrix", color = AcademicBlue, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        QuestionStimulusPreviewCard(
            item = item,
            showAnswerKey = true,
            interactiveKeySelection = true,
            onSelectOption = onSelectOption,
            onEditItem = onEdit
        )

        PedagogicalMetadataInspector(item = item)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyDetailPlaceholder() {
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
                text = "Select an item from the bank to inspect its stimulus, answer key, and pedagogical metadata.",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )
        }
    }
}

@Composable
private fun FilterChipBox(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (selected) SlateInk else SurfaceCard,
        border = BorderStroke(1.dp, if (selected) SlateInk else BorderHairline),
        modifier = Modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else SlateInk
            )
        }
    }
}

@Composable
private fun AssignToAssessmentDialog(
    assessments: List<AssessmentPlan>,
    questionId: String,
    onToggle: (AssessmentPlan) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SurfaceCard,
        title = { Text("Assign to Assessment Matrix", style = MaterialTheme.typography.titleLarge) },
        text = {
            if (assessments.isEmpty()) {
                Text(
                    "No assessment matrices created yet. Switch to the Assessments tab to create your first exam matrix.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assessments.forEach { plan ->
                        val included = questionId in plan.questionIds
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (included) Color(0xFFECFDF5) else SurfaceOffset,
                            border = BorderStroke(1.dp, if (included) EmeraldKey else BorderHairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onToggle(plan) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plan.title, style = MaterialTheme.typography.titleSmall, color = SlateInk)
                                    Text(
                                        "${plan.questionIds.size} items · ${plan.subject}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted
                                    )
                                }
                                Text(
                                    text = if (included) "INCLUDED" else "ADD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (included) EmeraldKey else AcademicBlue
                                )
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

@Composable
private fun CreateManualQuestionDialog(
    onDismiss: () -> Unit,
    onCreate: (QuestionItem) -> Unit
) {
    var subject by remember { mutableStateOf("Mathematics") }
    var grade by remember { mutableStateOf("Grade 11") }
    var topic by remember { mutableStateOf("") }
    var bloom by remember { mutableStateOf(BloomLevel.C3) }
    var stimulusTitle by remember { mutableStateOf("") }
    var stimulusContent by remember { mutableStateOf("") }
    var stem by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIdx by remember { mutableStateOf(0L) }
    var rationale by remember { mutableStateOf("") }

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
                    Text("Author Manual Item", style = MaterialTheme.typography.titleMedium, color = SlateInk)
                    TextButton(onClick = onDismiss) { Text("Cancel", color = SlateMuted) }
                }
                HorizontalDivider(color = BorderHairline)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AcademicTextField("SUBJECT", subject, { subject = it }, modifier = Modifier.weight(1f))
                        AcademicTextField("GRADE", grade, { grade = it }, modifier = Modifier.weight(0.8f))
                    }
                    AcademicTextField("TOPIC", topic, { topic = it }, placeholder = "e.g., Derivatives optimization")
                    AcademicTextField("STIMULUS TITLE (OPTIONAL)", stimulusTitle, { stimulusTitle = it })
                    AcademicTextField("CONTEXT STIMULUS (OPTIONAL)", stimulusContent, { stimulusContent = it }, minLines = 2, maxLines = 4)
                    AcademicTextField("QUESTION STEM", stem, { stem = it }, minLines = 2, maxLines = 4)
                    Text("CHOICES (SELECT KEY)", style = MaterialTheme.typography.labelMedium, color = SlateInk)
                    options.forEachIndexed { idx, value ->
                        val isKey = idx.toLong() == correctIdx
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isKey) EmeraldKey else SurfaceOffset,
                                border = BorderStroke(1.dp, BorderHairline),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { correctIdx = idx.toLong() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(('A' + idx).toString(), color = if (isKey) Color.White else SlateInk, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            OutlinedTextField(
                                value = value,
                                onValueChange = { options[idx] = it },
                                placeholder = { Text("Option ${'A' + idx}") },
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    AcademicTextField("PEDAGOGICAL RATIONALE", rationale, { rationale = it }, minLines = 2)
                }

                HorizontalDivider(color = BorderHairline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val cleanOptions = options.map { it.trim() }.filter { it.isNotEmpty() }
                    Button(
                        onClick = {
                            onCreate(
                                QuestionItem(
                                    subject = subject.trim().ifBlank { "General" },
                                    gradeLevel = grade.trim().ifBlank { "Grade 11" },
                                    topic = topic.trim().ifBlank { "Manual Item" },
                                    bloomLevel = bloom.code,
                                    questionFormat = QuestionFormat.MULTIPLE_CHOICE.wireName,
                                    difficulty = DifficultyLevel.STANDARD.wireName,
                                    stimulusTitle = stimulusTitle.trim(),
                                    stimulusContent = stimulusContent.trim(),
                                    questionStem = stem.trim(),
                                    options = cleanOptions,
                                    correctOptionIndex = if (cleanOptions.isEmpty()) -1L else correctIdx.coerceIn(0, cleanOptions.lastIndex.toLong()),
                                    pedagogicalRationale = rationale.trim(),
                                    scoringRubric = "1 pt for correct choice",
                                    curriculumAlignment = "$subject · $grade",
                                    estimatedMinutes = 3L,
                                    discriminationIndex = 0.40,
                                    reviewStatus = ReviewStatus.DRAFT.wireName
                                )
                            )
                        },
                        enabled = stem.isNotBlank(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateInk, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Create Item")
                    }
                }
            }
        }
    }
}
