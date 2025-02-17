package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.canDisplayMetisOnDisplaySide
import io.github.fornewid.placeholder.material3.placeholder
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
    onNavigateBack: () -> Unit,
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
            onNavigateBack = onNavigateBack,
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
    onNavigateBack: () -> Unit,
    onViewExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit
) {
    val linkOpener = LocalLinkOpener.current

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
            LectureScreenBody(
                modifier = modifier,
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
                onDisplaySetCompletedFailureDialog = { displaySetCompletedFailureDialog = true },
                onRequestOpenAttachment = { pendingOpenFileAttachment = it },
                onRequestViewLink = { pendingOpenLink = it }
            )
        }

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
                    navigationIcon = { NavigationBackButton(onNavigateBack) }
                )
            }
        ) { padding ->
            val bodyModifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))

            contentBody(
                bodyModifier
            )

            // Commented out as we may need to add this functionality again later if communication is also available for lectures.

//            if (displayCommunicationOnSide) {
//                Row(
//                    modifier = bodyModifier,
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    contentBody(
//                        Modifier
//                            .weight(1f - METIS_RATIO)
//                            .fillMaxHeight()
//                    )
//
//                    SideBarMetisUi(
//                        modifier = Modifier
//                            .weight(METIS_RATIO)
//                            .fillMaxHeight(),
//                        metisContext = metisContext,
//                        navController = navController,
//                        title = { Text(text = stringResource(id = R.string.lecture_view_tab_communication)) }
//                    )
//                }
//            } else {
//                contentBody(
//                    bodyModifier
//                )
//            }

            val currentPendingOpenFileAttachment = pendingOpenFileAttachment
            if (currentPendingOpenFileAttachment != null) {
                val context = LocalContext.current

                DownloadPendingAttachmentAlertDialog(
                    onDismissRequest = { pendingOpenFileAttachment = null },
                    onRequestDownloadFile = {
                        val success = downloadAttachment(
                            context = context,
                            attachment = currentPendingOpenFileAttachment,
                            serverUrl = viewModel.serverUrl.value,
                            authToken = viewModel.authToken.value
                        )

                        if (!success) {
                            Toast
                                .makeText(context, R.string.lecture_view_download_attachment_failed, Toast.LENGTH_SHORT)
                                .show()
                        }

                        pendingOpenFileAttachment = null
                    }
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

@Composable
private fun DownloadPendingAttachmentAlertDialog(
    onDismissRequest: () -> Unit,
    onRequestDownloadFile: () -> Unit
) {
    TextAlertDialog(
        title = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_title),
        text = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_message),
        confirmButtonText = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_positive),
        dismissButtonText = stringResource(id = R.string.lecture_view_open_file_attachment_dialog_negative),
        onPressPositiveButton = onRequestDownloadFile,
        onDismissRequest = onDismissRequest
    )
}

private fun downloadAttachment(
    context: Context,
    attachment: Attachment,
    serverUrl: String,
    authToken: String
): Boolean {
    try {
        val mimeType = getAttachmentMimeType(attachment)

        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri =
            Uri.parse(buildOpenAttachmentLink(serverUrl, attachment.link.orEmpty()))

        downloadManager
            .enqueue(
                DownloadManager.Request(downloadUri)
                    .addRequestHeader(HttpHeaders.Cookie, "jwt=$authToken")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setTitle(
                        attachment.name ?: "No name found"
                    )
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        downloadUri.lastPathSegment ?: return false
                    )
                    .setMimeType(mimeType)
            )
        return true
    } catch (e: Exception) {
        return false
    }
}

private fun getAttachmentMimeType(attachment: Attachment): String {
    val link = attachment.link ?: return "*/*"

    val extension = MimeTypeMap.getFileExtensionFromUrl(link) ?: return "*/*"
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return "*/*"
}

private fun buildOpenAttachmentLink(
    serverUrl: String,
    attachmentLink: String
): String {
    return URLBuilder(serverUrl).apply {
        appendPathSegments(attachmentLink)
    }.buildString()
}
