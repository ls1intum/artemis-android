package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.canDisplayMetisOnDisplaySide
import io.github.fornewid.placeholder.material3.placeholder
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLEncoder

const val METIS_RATIO = 0.3f

@Serializable
private data class LectureScreenUi(val lectureId: Long)

fun NavController.navigateToLecture(
    lectureId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(LectureScreenUi(lectureId), builder)
}

fun NavGraphBuilder.lecture(
    navController: NavController,
    onViewExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
) {
    animatedComposable<LectureScreenUi>(
        deepLinks = LectureDeeplinks.ToLecture.generateLinks() +
                LectureDeeplinks.ToLectureCourseAgnostic.generateLinks()
    ) { backStackEntry ->
        val route: LectureScreenUi = backStackEntry.toRoute()

        val lectureId = route.lectureId

        val viewModel: LectureViewModel = koinViewModel { parametersOf(lectureId) }
        val lectureDataState by viewModel.lectureDataState.collectAsState()
        val courseId by remember(lectureDataState) {
            derivedStateOf { lectureDataState.bind { it.course?.id ?: 0 }.orElse(0) }
        }

        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            lectureId = lectureId,
            viewModel = viewModel,
            navController = navController,
            onViewExercise = onViewExercise,
            onNavigateToExerciseResultView = onNavigateToExerciseResultView,
            onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
            onParticipateInQuiz = { exerciseId, isPractice ->
                onParticipateInQuiz(
                    courseId,
                    exerciseId,
                    isPractice
                )
            },
            onClickViewQuizResults = onClickViewQuizResults
        )
    }
}

@Composable
internal fun LectureScreen(
    modifier: Modifier,
    courseId: Long,
    lectureId: Long,
    viewModel: LectureViewModel,
    navController: NavController,
    onViewExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit
) {
    val linkOpener = LocalLinkOpener.current

    val lectureDataState by viewModel.lectureDataState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ArtemisTopAppBar(
                    title = {
                        Text(
                            text = lectureTitle.orEmpty(),
                            modifier = Modifier.placeholder(lectureTitle == null),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = { NavigationBackButton() },
                    isElevated = false
                )
            }
        ) { padding ->
            LectureScreenBody(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                displayCommunicationOnSide = displayCommunicationOnSide,
                lectureDataState = lectureDataState,
                viewModel = viewModel,
                onViewExercise = onViewExercise,
                onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
                onParticipateInQuiz = onParticipateInQuiz,
                onNavigateToExerciseResultView = onNavigateToExerciseResultView,
                onClickViewQuizResults = onClickViewQuizResults,
                courseId = courseId,
                overviewListState = overviewListState,
                metisContext = metisContext,
                navController = navController,
                onDisplaySetCompletedFailureDialog = {
                    displaySetCompletedFailureDialog = true
                },
                onRequestOpenAttachment = { pendingOpenFileAttachment = it },
                onRequestViewLink = { pendingOpenLink = it }
            )

            val currentPendingOpenFileAttachment = pendingOpenFileAttachment
            if (currentPendingOpenFileAttachment != null) {
                val fileName = currentPendingOpenFileAttachment.name.orEmpty()
                val url = buildOpenAttachmentLink(
                    serverUrl,
                    currentPendingOpenFileAttachment.link.orEmpty()
                )
                val formattedUrl = createAttachmentFileUrl(url, fileName, true)

                LinkBottomSheet(
                    modifier = Modifier.fillMaxSize(),
                    artemisContext = artemisContext,
                    link = formattedUrl,
                    fileName = currentPendingOpenFileAttachment.name.orEmpty(),
                    state = LinkBottomSheetState.PDFVIEWSTATE,
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
                        linkOpener.openLink(pendingOpenLink.orEmpty())
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
    attachmentLink: String
): String {
    return URLBuilder(serverUrl).apply {
        appendPathSegments(*Api.Core.UploadedFile.path)
        appendPathSegments(attachmentLink)
    }.buildString()
}

// Necessary to encode the file name for the attachment URL, see
// https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/shared/http/file.service.ts
private fun createAttachmentFileUrl(downloadUrl: String, downloadName: String, encodeName: Boolean): String {
    val downloadUrlComponents = downloadUrl.split("/")
    val extension = downloadUrlComponents.lastOrNull()?.substringAfterLast('.', "") ?: ""
    val restOfUrl = downloadUrlComponents.dropLast(1).joinToString("/")
    val encodedDownloadName = if (encodeName) {
        URLEncoder.encode("$downloadName.$extension", "UTF-8").replace("+", "%20")
    } else {
        "$downloadName.$extension"
    }
    return "$restOfUrl/$encodedDownloadName"
}

