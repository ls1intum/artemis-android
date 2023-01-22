package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SideBarMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.canDisplayMetisOnDisplaySide
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

const val METIS_RATIO = 0.3f

fun NavController.navigateToLecture(
    lectureId: Long,
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("lecture/$lectureId/$courseId", builder)
}

fun NavGraphBuilder.lecture(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onRequestOpenLink: (String) -> Unit,
    onViewExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
) {
    composable(
        route = "lecture/{lectureId}/{courseId}",
        arguments = listOf(
            navArgument("lectureId") {
                type = NavType.LongType
                nullable = false
            },
            navArgument("courseId") {
                type = NavType.LongType
                nullable = false
            }
        )
    ) { backStackEntry ->
        val lectureId =
            backStackEntry.arguments?.getLong("lectureId")
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(lectureId)
        checkNotNull(courseId)

        val viewModel: LectureViewModel = koinViewModel { parametersOf(lectureId) }
        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            lectureId = lectureId,
            viewModel = viewModel,
            navController = navController,
            onNavigateBack = onNavigateBack,
            onRequestOpenLink = onRequestOpenLink,
            onViewExercise = onViewExercise,
            onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
            onParticipateInQuiz = { exerciseId, isPractice ->
                onParticipateInQuiz(
                    courseId,
                    exerciseId,
                    isPractice
                )
            },
            onNavigateToExerciseResultView = onNavigateToExerciseResultView
        )
    }
}

@Composable
private fun LectureScreen(
    modifier: Modifier,
    courseId: Long,
    lectureId: Long,
    viewModel: LectureViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onRequestOpenLink: (String) -> Unit,
    onViewExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
) {
    val serverConfigurationService: ServerConfigurationService = get()
    val accountService: AccountService = get()
    val scope = rememberCoroutineScope()

    val lectureDataState by viewModel.lectureDataState.collectAsState()

    val lectureTitle = lectureDataState.bind<String?> { it.title }.orElse(null)

    // Set if the user clicked on a file attachment.
    var pendingOpenFileAttachment: Attachment? by remember { mutableStateOf(null) }
    var pendingOpenLink: String? by remember { mutableStateOf(null) }

    var displaySetCompletedFailureDialog: Boolean by remember { mutableStateOf(false) }

    val metisContext = remember(courseId, lectureId) {
        MetisContext.Lecture(courseId = courseId, lectureId = lectureId)
    }

    val overviewListState = rememberLazyListState()

    BoxWithConstraints(modifier = modifier) {
        val displayCommunicationOnSide = canDisplayMetisOnDisplaySide(
            parentWidth = maxWidth,
            metisContentRatio = METIS_RATIO
        )

        // The lecture UI with tabs
        val contentBody = @Composable { modifier: Modifier ->
            val selectedTabIndexState = rememberSaveable {
                mutableStateOf(0)
            }

            val overviewTabIndex = 0
            val attachmentsTabIndex = 1
            val qnaTabIndex = 2

            val selectedTabIndex =
                if (selectedTabIndexState.value == qnaTabIndex && displayCommunicationOnSide) {
                    overviewTabIndex
                } else selectedTabIndexState.value


            Column(modifier = modifier) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    DefaultTab(
                        index = overviewTabIndex,
                        icon = Icons.Default.ViewHeadline,
                        textRes = R.string.lecture_view_tab_overview,
                        selectedTabIndex = selectedTabIndexState
                    )

                    DefaultTab(
                        index = attachmentsTabIndex,
                        icon = Icons.Default.Attachment,
                        textRes = R.string.lecture_view_tab_attachments,
                        selectedTabIndex = selectedTabIndexState
                    )

                    if (!displayCommunicationOnSide) {
                        DefaultTab(
                            index = qnaTabIndex,
                            icon = Icons.Default.HelpCenter,
                            textRes = R.string.lecture_view_tab_communication,
                            selectedTabIndex = selectedTabIndexState
                        )
                    }
                }

                BasicDataStateUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    dataState = lectureDataState,
                    loadingText = stringResource(id = R.string.lecture_view_lecture_loading),
                    failureText = stringResource(id = R.string.lecture_view_lecture_loading_failure),
                    retryButtonText = stringResource(id = R.string.lecture_view_lecture_loading_try_again),
                    onClickRetry = viewModel::requestReloadLecture
                ) { lecture ->
                    when (selectedTabIndex) {
                        overviewTabIndex -> {
                            val lectureUnits by viewModel.lectureUnits.collectAsState()

                            OverviewTab(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                description = lecture.description,
                                lectureUnits = lectureUnits,
                                onViewExercise = onViewExercise,
                                onMarkAsCompleted = { lectureUnitId, isCompleted ->
                                    viewModel.updateLectureUnitIsComplete(
                                        lectureUnitId,
                                        isCompleted
                                    ) { isSuccessful ->
                                        if (!isSuccessful) {
                                            displaySetCompletedFailureDialog = true
                                        }
                                    }
                                },
                                onRequestViewLink = {
                                    pendingOpenLink = it
                                },
                                onRequestOpenAttachment = { attachment ->
                                    pendingOpenFileAttachment = attachment
                                },
                                exerciseActions = BoundExerciseActions(
                                    onClickStartTextExercise = { exerciseId ->
                                        viewModel.startExercise(exerciseId) { participationId ->
                                            onNavigateToTextExerciseParticipation(
                                                exerciseId,
                                                participationId
                                            )
                                        }
                                    },
                                    onClickOpenQuiz = { exerciseId ->
                                        onParticipateInQuiz(exerciseId, false)
                                    },
                                    onClickPracticeQuiz = { exerciseId ->
                                        onParticipateInQuiz(exerciseId, true)
                                    },
                                    onClickStartQuiz = { exerciseId ->
                                        onParticipateInQuiz(exerciseId, false)
                                    },
                                    onClickOpenTextExercise = onNavigateToTextExerciseParticipation,
                                    onClickViewResult = onNavigateToExerciseResultView
                                ),
                                state = overviewListState
                            )
                        }

                        qnaTabIndex -> {
                            SmartphoneMetisUi(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                metisContext = metisContext,
                                navController = navController
                            )
                        }

                        attachmentsTabIndex -> {
                            AttachmentsTab(
                                modifier = Modifier.fillMaxSize(),
                                attachments = lecture.attachments,
                                onClickFileAttachment = { fileAttachment ->
                                    pendingOpenFileAttachment = fileAttachment
                                },
                                onClickOpenLinkAttachment = { attachment ->
                                    pendingOpenFileAttachment = attachment
                                }
                            )
                        }
                    }
                }
            }

        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = lectureTitle ?: "Placeholder",
                            modifier = Modifier.placeholder(lectureTitle == null)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            val bodyModifier = Modifier
                .fillMaxSize()
                .padding(padding)

            if (displayCommunicationOnSide) {
                Row(
                    modifier = bodyModifier,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    contentBody(
                        Modifier
                            .weight(1f - METIS_RATIO)
                            .fillMaxHeight()
                    )

                    SideBarMetisUi(
                        modifier = Modifier
                            .weight(METIS_RATIO)
                            .fillMaxHeight(),
                        metisContext = metisContext,
                        navController = navController,
                        title = { Text(text = stringResource(id = R.string.lecture_view_tab_communication)) }
                    )
                }
            } else {
                contentBody(
                    bodyModifier
                )
            }

            if (pendingOpenFileAttachment != null) {
                TextAlertDialog(
                    title = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_title),
                    text = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_message),
                    confirmButtonText = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_positive),
                    dismissButtonText = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_negative),
                    onPressPositiveButton = {
                        scope.launch {
                            val url = buildOpenAttachmentLink(
                                serverUrl = serverConfigurationService.serverUrl.first(),
                                authToken = accountService.authToken.first(),
                                attachmentLink = pendingOpenFileAttachment?.link.orEmpty()
                            )
                            onRequestOpenLink(url)
                            pendingOpenFileAttachment = null
                        }
                    },
                    onDismissRequest = { pendingOpenFileAttachment = null }
                )
            }

            if (pendingOpenLink != null) {
                TextAlertDialog(
                    title = stringResource(id = R.string.lecture_view_open_link_dialog_title),
                    text = stringResource(
                        id = R.string.lecture_view_open_link_dialog_message,
                        pendingOpenLink.orEmpty()
                    ),
                    confirmButtonText = stringResource(id = R.string.lecture_view_open_link_dialog_positive),
                    dismissButtonText = stringResource(id = R.string.lecture_view_open_link_dialog_negative),
                    onPressPositiveButton = {
                        onRequestOpenLink(pendingOpenLink.orEmpty())
                        pendingOpenLink = null
                    },
                    onDismissRequest = { pendingOpenLink = null }
                )
            }

            if (displaySetCompletedFailureDialog) {
                TextAlertDialog(
                    title = stringResource(id = R.string.lecture_view_lecture_unit_set_completed_failed_dialog_title),
                    text = stringResource(id = R.string.lecture_view_lecture_unit_set_completed_failed_dialog_message),
                    confirmButtonText = stringResource(id = R.string.lecture_view_lecture_unit_set_completed_failed_dialog_positive),
                    dismissButtonText = null,
                    onPressPositiveButton = { displaySetCompletedFailureDialog = false },
                    onDismissRequest = { displaySetCompletedFailureDialog = false }
                )
            }
        }
    }
}

private fun buildOpenAttachmentLink(
    serverUrl: String,
    authToken: String,
    attachmentLink: String
): String {
    return URLBuilder(serverUrl).apply {
        appendPathSegments(attachmentLink)

        parameters.append("access_token", authToken)
    }.buildString()
}
