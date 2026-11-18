package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BloomLevel
import com.example.data.QuestionFormat
import com.example.data.QuestionItem
import com.example.data.ReviewStatus
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
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.EmeraldKey
import com.example.ui.theme.EmeraldKeySurface
import com.example.ui.theme.EmeraldKeyText
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceOffset

@Composable
fun BloomBadge(
    bloomLevel: BloomLevel,
    modifier: Modifier = Modifier
) {
    val (bg, fg, border) = when (bloomLevel) {
        BloomLevel.C1, BloomLevel.C2 -> Triple(BloomLowBg, BloomLowText, BloomLowBorder)
        BloomLevel.C3, BloomLevel.C4 -> Triple(BloomMidBg, BloomMidText, BloomMidBorder)
        BloomLevel.C5, BloomLevel.C6 -> Triple(BloomHighBg, BloomHighText, BloomHighBorder)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text = "${bloomLevel.code} · ${bloomLevel.shortTitle.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ReviewStatusBadge(
    status: ReviewStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg, border) = when (status) {
        ReviewStatus.DRAFT -> Triple(BloomLowBg, BloomLowText, BloomLowBorder)
        ReviewStatus.VALIDATED -> Triple(Color(0xFFEFF6FF), AcademicBlue, Color(0xFFBFDBFE))
        ReviewStatus.APPROVED -> Triple(EmeraldKeySurface, EmeraldKeyText, Color(0xFFA7F3D0))
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            if (status == ReviewStatus.APPROVED) {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = status.displayLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuestionStimulusPreviewCard(
    item: QuestionItem,
    showAnswerKey: Boolean = true,
    interactiveKeySelection: Boolean = true,
    onSelectOption: (Int) -> Unit = {},
    onOpenInspector: (() -> Unit)? = null,
    onEditItem: (() -> Unit)? = null,
    onSaveToBank: (() -> Unit)? = null,
    isSavedToBank: Boolean = false,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderHairline),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar with taxonomic tags and time estimate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceOffset)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    BloomBadge(bloomLevel = item.bloomEnum)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceCard,
                        border = BorderStroke(1.dp, BorderHairline)
                    ) {
                        Text(
                            text = item.formatEnum.displayLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Estimated time",
                        tint = SlateMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${item.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }
            }

            HorizontalDivider(color = BorderHairline, thickness = 1.dp)

            // Upper Quadrant: Context Stimulus
            if (item.stimulusContent.isNotBlank() || item.stimulusTitle.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(SurfaceOffset.copy(alpha = 0.65f))
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(AcademicBlue)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = AcademicBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "CONTEXT STIMULUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = AcademicBlue
                            )
                        }
                        if (item.stimulusTitle.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.stimulusTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateInk
                            )
                        }
                        if (item.stimulusContent.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.stimulusContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(color = BorderHairline, thickness = 1.dp)
            }

            // Lower Quadrant: Question Stem & Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "ITEM STEM",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.questionStem,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SlateInk
                )

                if (item.formatEnum != QuestionFormat.ESSAY && item.options.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.options.forEachIndexed { index, optionText ->
                            val isKey = showAnswerKey && index.toLong() == item.correctOptionIndex
                            val letter = ('A' + index).toString()
                            OptionRow(
                                letter = letter,
                                text = optionText,
                                isSelectedKey = isKey,
                                clickable = interactiveKeySelection,
                                onClick = { onSelectOption(index) },
                                modifier = Modifier.testTag("option_row_$letter")
                            )
                        }
                    }
                    if (interactiveKeySelection && showAnswerKey) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap an option to reassign the answer key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                } else if (item.scoringRubric.isNotBlank() && showAnswerKey) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EmeraldKeySurface,
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CONSTRUCTED RESPONSE RUBRIC",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldKeyText
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.scoringRubric,
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldKeyText
                            )
                        }
                    }
                }
            }

            // Action Strip
            if (onSaveToBank != null || onOpenInspector != null || onEditItem != null) {
                HorizontalDivider(color = BorderHairline, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenInspector != null) {
                        OutlinedButton(
                            onClick = onOpenInspector,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, BorderHairline),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("inspect_metadata_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = null,
                                tint = SlateInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Metadata", color = SlateInk, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (onEditItem != null) {
                        OutlinedButton(
                            onClick = onEditItem,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, BorderHairline),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("edit_item_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = SlateInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Edit", color = SlateInk, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (onSaveToBank != null) {
                        Button(
                            onClick = onSaveToBank,
                            enabled = !isSavedToBank && !isSaving,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSavedToBank) EmeraldKey else SlateInk,
                                contentColor = Color.White,
                                disabledContainerColor = if (isSavedToBank) EmeraldKeySurface else BorderHairline,
                                disabledContentColor = if (isSavedToBank) EmeraldKeyText else SlateMuted
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("save_to_bank_button")
                        ) {
                            Icon(
                                imageVector = if (isSavedToBank) Icons.Outlined.CheckCircle else Icons.Outlined.BookmarkAdded,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isSaving -> "Saving..."
                                    isSavedToBank -> "Saved"
                                    else -> "Save to Bank"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    letter: String,
    text: String,
    isSelectedKey: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelectedKey) EmeraldKeySurface else SurfaceCard
    val borderColor = if (isSelectedKey) EmeraldKey else BorderHairline
    val letterBg = if (isSelectedKey) EmeraldKey else SurfaceOffset
    val letterFg = if (isSelectedKey) Color.White else SlateInk

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(letterBg, RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = if (isSelectedKey) EmeraldKey else BorderHairline,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelMedium,
                    color = letterFg
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelectedKey) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelectedKey) EmeraldKeyText else SlateInk,
                modifier = Modifier.weight(1f)
            )
            if (isSelectedKey) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Answer Key",
                    tint = EmeraldKey,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.5.dp, BorderHairline, CircleShape)
                )
            }
        }
    }
}

@Composable
fun PedagogicalMetadataInspector(
    item: QuestionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Key-Value Metadata Matrix
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderHairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceOffset)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PEDAGOGICAL METADATA",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                    ReviewStatusBadge(status = item.reviewStatusEnum)
                }
                HorizontalDivider(color = BorderHairline)
                MetadataKeyValueRow("Subject & Level", "${item.subject} · ${item.gradeLevel}")
                MetadataKeyValueRow("Curriculum Alignment", item.curriculumAlignment.ifBlank { "General Competency" })
                MetadataKeyValueRow("Bloom's Taxonomy", item.bloomEnum.title)
                MetadataKeyValueRow("Difficulty Tier", item.difficultyEnum.displayLabel)
                MetadataKeyValueRow("Est. Completion Time", "${item.estimatedMinutes} minutes")
                MetadataKeyValueRow(
                    label = "Discrimination Index",
                    value = String.format("%.2f (Psychometric Target)", item.discriminationIndex),
                    showDivider = false
                )
            }
        }

        // Pedagogical Rationale
        if (item.pedagogicalRationale.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PEDAGOGICAL RATIONALE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AcademicBlue
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.pedagogicalRationale,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateInk
                    )
                }
            }
        }

        // Scoring Rubric
        if (item.scoringRubric.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderHairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SCORING RUBRIC",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldKey
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.scoringRubric,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateInk
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataKeyValueRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted,
                modifier = Modifier.weight(0.44f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SlateInk,
                modifier = Modifier.weight(0.56f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = DividerSubtle)
        }
    }
}
