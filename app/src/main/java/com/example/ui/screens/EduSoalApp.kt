package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.AiQuestionRepository
import com.example.data.EduSoalRepository
import com.example.firebase.authStateFlow
import com.example.firebase.signOutUser
import com.example.ui.theme.AcademicBlue
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceOffset
import com.example.viewmodel.EduSoalViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class EduSoalTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    GENERATOR("Generator", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    ITEM_BANK("Item Bank", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks),
    ASSESSMENTS("Assessments", Icons.Filled.FactCheck, Icons.Outlined.FactCheck)
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow updates automatically */ })
    } else {
        key(user.uid) {
            EduSoalWorkspace(user = user)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EduSoalWorkspace(user: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: EduSoalViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                EduSoalViewModel(
                    aiRepository = AiQuestionRepository(),
                    repository = EduSoalRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val questionsState by viewModel.questionsState.collectAsStateWithLifecycle()
    val assessmentsState by viewModel.assessmentsState.collectAsStateWithLifecycle()
    val bankFilter by viewModel.bankFilter.collectAsStateWithLifecycle()
    val selectedBankQuestionId by viewModel.selectedBankQuestionId.collectAsStateWithLifecycle()
    val selectedAssessmentId by viewModel.selectedAssessmentId.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(EduSoalTab.GENERATOR) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCanvas)
    ) {
        val isWideScreen = maxWidth >= 840.dp

        Scaffold(
            containerColor = SurfaceCanvas,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SurfaceCard,
                            titleContentColor = SlateInk
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.edusoal_icon_fg_1795019912982),
                                    contentDescription = "EduSoal AI emblem",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Column {
                                    Text(
                                        text = "EduSoal AI",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = SlateInk
                                    )
                                    Text(
                                        text = "ASSESSMENT SUITE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AcademicBlue
                                    )
                                }
                            }
                        },
                        actions = {
                            val educatorLabel = user.displayName?.takeIf { it.isNotBlank() }
                                ?: user.email?.substringBefore("@")
                                ?: "Educator"
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceOffset,
                                border = BorderStroke(1.dp, BorderHairline),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = educatorLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    signOutUser(
                                        credentialManager = credentialManager,
                                        scope = scope
                                    )
                                },
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Sign out",
                                    tint = SlateMuted
                                )
                            }
                        }
                    )
                    HorizontalDivider(color = BorderHairline)
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    Column {
                        HorizontalDivider(color = BorderHairline)
                        NavigationBar(
                            containerColor = SurfaceCard,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            EduSoalTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AcademicBlue,
                                        selectedTextColor = AcademicBlue,
                                        unselectedIconColor = SlateMuted,
                                        unselectedTextColor = SlateMuted,
                                        indicatorColor = SurfaceContainerLow
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = SurfaceCard,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.size(12.dp))
                        EduSoalTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(tab.label, style = MaterialTheme.typography.labelSmall)
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = AcademicBlue,
                                    selectedTextColor = AcademicBlue,
                                    unselectedIconColor = SlateMuted,
                                    unselectedTextColor = SlateMuted,
                                    indicatorColor = SurfaceContainerLow
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(BorderHairline)
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentTab) {
                        EduSoalTab.GENERATOR -> GeneratorScreen(
                            formState = formState,
                            aiState = aiState,
                            isSaving = isSaving,
                            isWideScreen = isWideScreen,
                            onSubjectChange = viewModel::updateSubject,
                            onGradeChange = viewModel::updateGradeLevel,
                            onTopicChange = viewModel::updateTopic,
                            onBloomChange = viewModel::updateBloomLevel,
                            onFormatChange = viewModel::updateFormat,
                            onDifficultyChange = viewModel::updateDifficulty,
                            onCustomStimulusChange = viewModel::updateCustomStimulus,
                            onApplyPreset = viewModel::applyQuickPreset,
                            onGenerate = viewModel::generateItem,
                            onSelectCorrectOption = viewModel::setGeneratedCorrectOption,
                            onSaveToBank = viewModel::saveGeneratedItemToBank,
                            onUpdateDraftItem = viewModel::updateGeneratedItemDraft
                        )

                        EduSoalTab.ITEM_BANK -> ItemBankScreen(
                            questionsState = questionsState,
                            assessmentsState = assessmentsState,
                            filterState = bankFilter,
                            selectedQuestionId = selectedBankQuestionId,
                            isWideScreen = isWideScreen,
                            onSearchChange = viewModel::updateSearchQuery,
                            onBloomTierChange = viewModel::updateBloomTierFilter,
                            onStatusFilterChange = viewModel::updateStatusFilter,
                            onSelectQuestion = viewModel::selectBankQuestion,
                            onUpdateCorrectOption = viewModel::updateBankQuestionCorrectOption,
                            onUpdateStatus = viewModel::updateQuestionStatus,
                            onUpdateQuestion = viewModel::updateExistingQuestion,
                            onCreateManualQuestion = viewModel::createManualQuestion,
                            onDeleteQuestion = viewModel::deleteQuestion,
                            onToggleInAssessment = viewModel::toggleQuestionInAssessment
                        )

                        EduSoalTab.ASSESSMENTS -> AssessmentsScreen(
                            assessmentsState = assessmentsState,
                            questionsState = questionsState,
                            selectedAssessmentId = selectedAssessmentId,
                            isWideScreen = isWideScreen,
                            onSelectAssessment = viewModel::selectAssessment,
                            onCreateAssessment = viewModel::createAssessment,
                            onToggleQuestion = viewModel::toggleQuestionInAssessment,
                            onUpdateStatus = viewModel::updateAssessmentStatus,
                            onDeleteAssessment = viewModel::deleteAssessment
                        )
                    }
                }
            }
        }
    }
}
