package de.tum.informatics.www1.artemis.native_app.feature.lectureview

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
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
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.ui.AttachmentHandler
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.AdaptiveNavigationIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.AttachmentUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkwon
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownRenderFactory
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import io.github.fornewid.placeholder.material3.placeholder
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
private data class LectureScreenRoute(val lectureId: Long)

fun NavController.navigateToLecture(
    lectureId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(LectureScreenRoute(lectureId), builder)
}

fun NavGraphBuilder.lecture(
    onViewExercise: (exerciseId: Long) -> Unit,
) {
    animatedComposable<LectureScreenRoute>(
        deepLinks = LectureDeeplinks.ToLecture.generateLinks() +
                LectureDeeplinks.ToLectureCourseAgnostic.generateLinks()
    ) { backStackEntry ->
        val route: LectureScreenRoute = backStackEntry.toRoute()
        val lectureId = route.lectureId

        LectureDetailContent(
            lectureId = lectureId,
            onViewExercise = onViewExercise,
        )
    }
}

@Composable
fun LectureDetailContent(
    lectureId: Long,
    onViewExercise: (exerciseId: Long) -> Unit,
    onSidebarToggle: () -> Unit = {}
) {
    val viewModel: LectureViewModel = koinViewModel(key = "lecture|$lectureId") {
        parametersOf(lectureId)
    }

    LectureScreen(
        modifier = Modifier.fillMaxSize(),
        viewModel = viewModel,
        onViewExercise = onViewExercise,
        onSidebarToggle = onSidebarToggle
    )
}

@Composable
internal fun LectureScreen(
    modifier: Modifier,
    viewModel: LectureViewModel,
    onViewExercise: (exerciseId: Long) -> Unit,
    onSidebarToggle: () -> Unit = {},
) {
    val lectureDataState by viewModel.lectureDataState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val lectureChannel by viewModel.channelDataState.collectAsState()
    val lectureUnits by viewModel.lectureUnits.collectAsState()

    LectureScreen(
        modifier = modifier,
        serverUrl = serverUrl,
        lectureDataState = lectureDataState,
        lectureChannel = lectureChannel,
        lectureUnits = lectureUnits,
        onViewExercise = onViewExercise,
        onReloadLecture = viewModel::onRequestReload,
        onUpdateLectureUnitIsComplete = viewModel::updateLectureUnitIsComplete,
        onSidebarToggle = onSidebarToggle
    )
}

@Composable
internal fun LectureScreen(
    lectureDataState: DataState<Lecture>,
    modifier: Modifier,
    serverUrl: String,
    lectureChannel: DataState<ChannelChat>,
    lectureUnits: List<LectureUnit>,
    onViewExercise: (exerciseId: Long) -> Unit,
    onReloadLecture: () -> Unit,
    onUpdateLectureUnitIsComplete: (lectureUnitId: Long, isCompleted: Boolean) -> Deferred<Boolean>,
    onSidebarToggle: () -> Unit
) {
    val context = LocalContext.current
    val linkOpener = LocalLinkOpener.current

    val lectureTitle = lectureDataState.bind<String?> { it.title }.orElse(null)

    // Set if the user clicked on a file attachment.
    var pendingOpenFileAttachment: Attachment? by remember { mutableStateOf(null) }
    var pendingOpenFileAttachmentByUrl: String? by remember { mutableStateOf(null) }

    var pendingOpenLink: String? by remember { mutableStateOf(null) }

    var displaySetCompletedFailureDialog: Boolean by remember { mutableStateOf(false) }

    val overviewListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
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
                navigationIcon = {
                    AdaptiveNavigationIcon(onSidebarToggle = onSidebarToggle)
                },
                isElevated = false
            )
        }
    ) { padding ->
        val markdownTransformer = rememberLectureArtemisMarkdownTransformer(serverUrl)
        val imageLoader = LocalArtemisImageProvider.current.rememberArtemisImageLoader()

        val linkResolver = remember(serverUrl) {
            LectureLinkResolver(
                serverUrl = serverUrl,
                onRequestOpenAttachment = { pendingOpenFileAttachmentByUrl = it },
                onRequestOpenLink = { pendingOpenLink = it }
            )
        }

        val markwon = remember(linkResolver, imageLoader) {
            MarkdownRenderFactory.create(context, imageLoader, linkResolver)
        }

        CompositionLocalProvider(
            LocalMarkdownTransformer provides markdownTransformer,
            LocalMarkwon provides markwon
        ) {
            LectureScreenBody(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                lectureDataState = lectureDataState,
                lectureChannel = lectureChannel,
                lectureUnits = lectureUnits,
                onViewExercise = onViewExercise,
                overviewListState = overviewListState,
                onRequestViewLink = { pendingOpenLink = it },
                onRequestOpenAttachment = { pendingOpenFileAttachment = it },
                onDisplaySetCompletedFailureDialog = {
                    displaySetCompletedFailureDialog = true
                },
                onReloadLecture = onReloadLecture,
                onUpdateLectureUnitIsComplete = onUpdateLectureUnitIsComplete,
                linkResolver = linkResolver
            )

            AttachmentHandler(
                url = pendingOpenFileAttachmentByUrl,
                onDismiss = { pendingOpenFileAttachmentByUrl = null }
            )

            val currentPendingOpenFileAttachment = pendingOpenFileAttachment
            if (currentPendingOpenFileAttachment != null) {
                val fileName = currentPendingOpenFileAttachment.name.orEmpty()
                val link = currentPendingOpenFileAttachment.link.orEmpty()

                val url = AttachmentUtil.buildOpenAttachmentLink(serverUrl, link)
                val formattedUrl =
                    AttachmentUtil.createAttachmentFileUrl(url, fileName, true)

                AttachmentHandler(
                    url = formattedUrl,
                    onDismiss = { pendingOpenFileAttachment = null }
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