package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiGenerationUiState
import com.example.data.BloomLevel
import com.example.data.DifficultyLevel
import com.example.data.QuestionFormat
import com.example.data.QuestionItem
import com.example.ui.components.PedagogicalMetadataInspector
import com.example.ui.components.QuestionStimulusPreviewCard
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.BloomHighBg
import com.example.ui.theme.BloomHighBorder
import com.example.ui.theme.BloomHighText
import com.example.ui.theme.BloomLowBg
import com.example.ui.theme.BloomLowBorder
import com.example.ui.theme.BloomLowText
import com.example.ui.theme.BloomMidBg
import com.example.ui.theme.BloomMidBorder
import com.example.ui.theme.BloomMidText
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.BorderInput
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.ErrorSurface
import com.example.ui.theme.OnErrorSurface
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceOffset
import com.example.viewmodel.GeneratorFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    formState: GeneratorFormState,
    aiState: AiGenerationUiState,
    isSaving: Boolean,
    isWideScreen: Boolean,
    onSubjectChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onBloomChange: (BloomLevel) -> Unit,
    onFormatChange: (QuestionFormat) -> Unit,
    onDifficultyChange: (DifficultyLevel) -> Unit,
    onCustomStimulusChange: (String) -> Unit,
    onApplyPreset: (String, String, String, BloomLevel, QuestionFormat) -> Unit,
    onGenerate: () -> Unit,
    onSelectCorrectOption: (Long) -> Unit,
    onSaveToBank: () -> Unit,
    onUpdateDraftItem: (QuestionItem) -> Unit
) {
    var showInspectorSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<QuestionItem?>(null) }

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GeneratorConfigPanel(
                    formState = formState,
                    isGenerating = aiState is AiGenerationUiState.Generating,
                    onSubjectChange = onSubjectChange,
                    onGradeChange = onGradeChange,
                    onTopicChange = onTopicChange,
                    onBloomChange = onBloomChange,
                    onFormatChange = onFormatChange,
                    onDifficultyChange = onDifficultyChange,
                    onCustomStimulusChange = onCustomStimulusChange,
                    onApplyPreset = onApplyPreset,
                    onGenerate = onGenerate
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GeneratorPreviewCanvas(
                    aiState = aiState,
                    isSaving = isSaving,
                    showInlineInspector = true,
                    onGenerate = onGenerate,
                    onSelectCorrectOption = onSelectCorrectOption,
                    onSaveToBank = onSaveToBank,
                    onOpenInspector = { showInspectorSheet = true },
                    onEditItem = { editingItem = it }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GeneratorConfigPanel(
                formState = formState,
                isGenerating = aiState is AiGenerationUiState.Generating,
                onSubjectChange = onSubjectChange,
                onGradeChange = onGradeChange,
                onTopicChange = onTopicChange,
                onBloomChange = onBloomChange,
                onFormatChange = onFormatChange,
                onDifficultyChange = onDifficultyChange,
                onCustomStimulusChange = onCustomStimulusChange,
                onApplyPreset = onApplyPreset,
                onGenerate = onGenerate
            )

            GeneratorPreviewCanvas(
                aiState = aiState,
                isSaving = isSaving,
                showInlineInspector = false,
                onGenerate = onGenerate,
                onSelectCorrectOption = onSelectCorrectOption,
                onSaveToBank = onSaveToBank,
                onOpenInspector = { showInspectorSheet = true },
                onEditItem = { editingItem = it }
            )

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showInspectorSheet && aiState is AiGenerationUiState.Generated) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showInspectorSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            containerColor = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pedagogical Inspector",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SlateInk
                    )
                    IconButton(onClick = { showInspectorSheet = false }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close inspector")
                    }
                }
                PedagogicalMetadataInspector(item = aiState.item)
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    editingItem?.let { itemToEdit ->
        EditQuestionDialog(
            initialItem = itemToEdit,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                onUpdateDraftItem(updated)
                editingItem = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneratorConfigPanel(
    formState: GeneratorFormState,
    isGenerating: Boolean,
    onSubjectChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onBloomChange: (BloomLevel) -> Unit,
    onFormatChange: (QuestionFormat) -> Unit,
    onDifficultyChange: (DifficultyLevel) -> Unit,
    onCustomStimulusChange: (String) -> Unit,
    onApplyPreset: (String, String, String, BloomLevel, QuestionFormat) -> Unit,
    onGenerate: () -> Unit
) {
    var showStimulusInput by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderHairline),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceOffset)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ITEM SPECIFICATION",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                    Text(
                        text = "Configure cognitive rigor, stimulus, and format",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    tint = AcademicBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            HorizontalDivider(color = BorderHairline)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Academic Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CURRICULUM TEMPLATES",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Biology · C4") {
                            onApplyPreset(
                                "Biology",
                                "Grade 11",
                                "Enzyme kinetics and thermal denaturation in cellular respiration",
                                BloomLevel.C4,
                                QuestionFormat.MULTIPLE_CHOICE
                            )
                        }
                        PresetChip("Economics · C5") {
                            onApplyPreset(
                                "Economics",
                                "Grade 12",
                                "Central bank interest rate policy vs. inflation targeting trade-offs",
                                BloomLevel.C5,
                                QuestionFormat.COMPLEX_MC
                            )
                        }
                        PresetChip("Physics · C3") {
                            onApplyPreset(
                                "Physics",
                                "Grade 11",
                                "Conservation of mechanical energy on an inclined plane with friction",
                                BloomLevel.C3,
                                QuestionFormat.MULTIPLE_CHOICE
                            )
                        }
                        PresetChip("Literature · C6") {
                            onApplyPreset(
                                "Literature",
                                "Grade 12",
                                "Evaluating unreliable narration in modernist short fiction",
                                BloomLevel.C6,
                                QuestionFormat.ESSAY
                            )
                        }
                    }
                }

                // Subject + Grade Level Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AcademicTextField(
                        label = "SUBJECT",
                        value = formState.subject,
                        onValueChange = onSubjectChange,
                        placeholder = "e.g., Biology",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_input")
                    )
                    AcademicTextField(
                        label = "GRADE / LEVEL",
                        value = formState.gradeLevel,
                        onValueChange = onGradeChange,
                        placeholder = "e.g., Grade 11",
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("grade_input")
                    )
                }

                // Topic / Learning Objective
                AcademicTextField(
                    label = "TOPIC / LEARNING OBJECTIVE",
                    value = formState.topic,
                    onValueChange = onTopicChange,
                    placeholder = "e.g., Analyze experimental data on enzyme saturation...",
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_input")
                )

                // Bloom Taxonomy Selector (C1-C6)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BLOOM'S COGNITIVE LEVEL",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BloomLevel.entries.forEach { level ->
                            val selected = formState.bloomLevel == level
                            val (bg, fg, border) = when {
                                !selected -> Triple(SurfaceCard, SlateMuted, BorderHairline)
                                level.ordinal <= 1 -> Triple(BloomLowBg, BloomLowText, SlateInk)
                                level.ordinal <= 3 -> Triple(BloomMidBg, BloomMidText, BloomMidBorder)
                                else -> Triple(BloomHighBg, BloomHighText, BloomHighBorder)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = bg,
                                border = BorderStroke(if (selected) 1.5.dp else 1.dp, border),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onBloomChange(level) }
                                    .testTag("bloom_chip_${level.code}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = level.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = fg
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = formState.bloomLevel.tierDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }

                // Question Format & Difficulty
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ITEM FORMAT",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuestionFormat.entries.forEach { fmt ->
                            val selected = formState.format == fmt
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (selected) SlateInk else SurfaceCard,
                                border = BorderStroke(1.dp, if (selected) SlateInk else BorderHairline),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onFormatChange(fmt) }
                                    .testTag("format_chip_${fmt.wireName}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = fmt.displayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else SlateInk,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DIFFICULTY TIER",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyLevel.entries.forEach { diff ->
                            val selected = formState.difficulty == diff
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (selected) AcademicBlue.copy(alpha = 0.1f) else SurfaceCard,
                                border = BorderStroke(1.dp, if (selected) AcademicBlue else BorderHairline),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 42.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onDifficultyChange(diff) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = diff.displayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) AcademicBlue else SlateMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Educator Context Stimulus Input
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showStimulusInput = !showStimulusInput }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "OPTIONAL SOURCE STIMULUS / PASSAGE",
                            style = MaterialTheme.typography.labelMedium,
                            color = AcademicBlue
                        )
                        Icon(
                            imageVector = if (showStimulusInput) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = "Toggle custom stimulus input",
                            tint = AcademicBlue
                        )
                    }
                    AnimatedVisibility(visible = showStimulusInput) {
                        AcademicTextField(
                            label = "PASTE EXCERPT OR DATA BRIEF (LEAVE BLANK TO AUTO-AUTHOR)",
                            value = formState.customStimulus,
                            onValueChange = onCustomStimulusChange,
                            placeholder = "Paste reading excerpt, lab table, or case notes...",
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }

                // Generate Button
                Button(
                    onClick = onGenerate,
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademicBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("generate_item_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Synthesizing Item & Stimulus...", style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Item with AI", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratorPreviewCanvas(
    aiState: AiGenerationUiState,
    isSaving: Boolean,
    showInlineInspector: Boolean,
    onGenerate: () -> Unit,
    onSelectCorrectOption: (Long) -> Unit,
    onSaveToBank: () -> Unit,
    onOpenInspector: () -> Unit,
    onEditItem: (QuestionItem) -> Unit
) {
    when (aiState) {
        is AiGenerationUiState.Idle -> {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceOffset,
                        border = BorderStroke(1.dp, BorderHairline),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = AcademicBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = "Stimulus & Item Canvas Ready",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SlateInk
                    )
                    Text(
                        text = "Select a curriculum template or enter your learning objective, choose a Bloom's cognitive level, and generate a calibrated assessment item.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                }
            }
        }

        is AiGenerationUiState.Generating -> {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = AcademicBlue, strokeWidth = 3.dp)
                    Text(
                        text = "Calibrating Stimulus & Distractors",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk
                    )
                    Text(
                        text = "Aligning stem complexity, answer key rationale, and scoring rubric via Gemini...",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        }

        is AiGenerationUiState.Error -> {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ErrorSurface,
                border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "GENERATION ERROR",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnErrorSurface
                    )
                    Text(
                        text = aiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnErrorSurface
                    )
                    OutlinedButton(
                        onClick = onGenerate,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, OnErrorSurface.copy(alpha = 0.4f)),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, tint = OnErrorSurface)
                        Spacer(Modifier.width(6.dp))
                        Text("Retry Generation", color = OnErrorSurface)
                    }
                }
            }
        }

        is AiGenerationUiState.Generated -> {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                QuestionStimulusPreviewCard(
                    item = aiState.item,
                    showAnswerKey = true,
                    interactiveKeySelection = true,
                    onSelectOption = { idx -> onSelectCorrectOption(idx.toLong()) },
                    onOpenInspector = if (showInlineInspector) null else onOpenInspector,
                    onEditItem = { onEditItem(aiState.item) },
                    onSaveToBank = onSaveToBank,
                    isSavedToBank = aiState.savedToBankId != null,
                    isSaving = isSaving
                )

                if (showInlineInspector) {
                    PedagogicalMetadataInspector(item = aiState.item)
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = SurfaceOffset,
        border = BorderStroke(1.dp, BorderHairline),
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
                color = SlateInk
            )
        }
    }
}

@Composable
fun AcademicTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    minLines: Int = 1,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SlateInk
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = SlateMuted)
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = SlateInk),
            minLines = minLines,
            maxLines = maxLines,
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AcademicBlue,
                unfocusedBorderColor = BorderInput,
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                cursorColor = AcademicBlue
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EditQuestionDialog(
    initialItem: QuestionItem,
    onDismiss: () -> Unit,
    onSave: (QuestionItem) -> Unit
) {
    var stimulusTitle by remember(initialItem) { mutableStateOf(initialItem.stimulusTitle) }
    var stimulusContent by remember(initialItem) { mutableStateOf(initialItem.stimulusContent) }
    var questionStem by remember(initialItem) { mutableStateOf(initialItem.questionStem) }
    var rationale by remember(initialItem) { mutableStateOf(initialItem.pedagogicalRationale) }
    var rubric by remember(initialItem) { mutableStateOf(initialItem.scoringRubric) }
    val editableOptions = remember(initialItem) {
        mutableStateListOf<String>().apply { addAll(initialItem.options) }
    }
    var correctIdx by remember(initialItem) { mutableStateOf(initialItem.correctOptionIndex) }

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
                    Text(
                        text = "Edit Assessment Item",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close editor")
                    }
                }
                HorizontalDivider(color = BorderHairline)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AcademicTextField(
                        label = "STIMULUS TITLE",
                        value = stimulusTitle,
                        onValueChange = { stimulusTitle = it }
                    )
                    AcademicTextField(
                        label = "CONTEXT STIMULUS",
                        value = stimulusContent,
                        onValueChange = { stimulusContent = it },
                        minLines = 3,
                        maxLines = 6
                    )
                    AcademicTextField(
                        label = "QUESTION STEM",
                        value = questionStem,
                        onValueChange = { questionStem = it },
                        minLines = 2,
                        maxLines = 4
                    )

                    if (editableOptions.isNotEmpty()) {
                        Text(
                            text = "OPTIONS (TAP LETTER TO SET KEY)",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateInk
                        )
                        editableOptions.forEachIndexed { idx, opt ->
                            val isKey = idx.toLong() == correctIdx
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (isKey) Color(0xFF059669) else SurfaceOffset,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(1.dp, BorderHairline, RoundedCornerShape(4.dp))
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { correctIdx = idx.toLong() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + idx).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isKey) Color.White else SlateInk
                                    )
                                }
                                OutlinedTextField(
                                    value = opt,
                                    onValueChange = { editableOptions[idx] = it },
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    AcademicTextField(
                        label = "PEDAGOGICAL RATIONALE",
                        value = rationale,
                        onValueChange = { rationale = it },
                        minLines = 2,
                        maxLines = 4
                    )
                    AcademicTextField(
                        label = "SCORING RUBRIC",
                        value = rubric,
                        onValueChange = { rubric = it },
                        minLines = 2,
                        maxLines = 4
                    )
                }

                HorizontalDivider(color = BorderHairline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Cancel", color = SlateMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                initialItem.copy(
                                    stimulusTitle = stimulusTitle.trim(),
                                    stimulusContent = stimulusContent.trim(),
                                    questionStem = questionStem.trim().ifBlank { initialItem.questionStem },
                                    options = editableOptions.toList(),
                                    correctOptionIndex = correctIdx,
                                    pedagogicalRationale = rationale.trim(),
                                    scoringRubric = rubric.trim()
                                )
                            )
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateInk, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
