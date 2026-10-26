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
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.media.normalizedMediaOrigin
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri
import de.tum.informatics.www1.artemis.native_app.core.ui.media.sameMediaOrigin
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR
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
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
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
private data class LectureScreenRoute(
    val lectureId: Long,
    val lectureUnitId: Long? = null,
    val attachmentId: Int? = null,
    val page: Int? = null,
    val courseId: Long? = null
)

fun NavController.navigateToLecture(
    lectureId: Long,
    courseId: Long? = null,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(LectureScreenRoute(lectureId, courseId = courseId?.takeIf { it > 0 }), builder)
}

fun NavGraphBuilder.lecture(
    onViewExercise: (exerciseId: Long) -> Unit,
) {
    lectureMediaReferences(onViewExercise)
    animatedComposable<LectureScreenRoute>(
        deepLinks = LectureDeeplinks.ToLecture.generateLinks() +
                LectureDeeplinks.ToLectureCourseAgnostic.generateLinks() +
                LectureDeeplinks.ToLectureUnit.generateLinks()
    ) { backStackEntry ->
        val route: LectureScreenRoute = backStackEntry.toRoute()
        val lectureId = route.lectureId

        LectureDetailContent(
            lectureId = lectureId,
            onViewExercise = onViewExercise,
            initialUnitId = route.lectureUnitId,
            initialAttachmentId = route.attachmentId,
            initialPage = route.page,
            displayedCourseId = route.courseId
        )
    }
}

@Composable
fun LectureDetailContent(
    lectureId: Long,
    onViewExercise: (exerciseId: Long) -> Unit,
    onSidebarToggle: () -> Unit = {},
    initialUnitId: Long? = null,
    initialAttachmentId: Int? = null,
    initialPage: Int? = null,
    displayedCourseId: Long? = null,
    initialResolvedAttachment: Attachment? = null
) {
    val session by LocalArtemisContextProvider.current.collectArtemisContextAsState()
    val account = (session as? ArtemisContext.LoggedIn)?.loginName.orEmpty()
    val viewModel: LectureViewModel = koinViewModel(key = "lecture|$lectureId|$account|${normalizedMediaOrigin(session.serverUrl)}") {
        parametersOf(lectureId)
    }

    val lectureState by viewModel.lectureDataState.collectAsState()
    val actualCourseId = (lectureState as? DataState.Success)?.data?.course?.id?.takeIf { it > 0 }
        ?: displayedCourseId?.takeIf { it > 0 }
    val contextProvider = LocalArtemisContextProvider.current
    LaunchedEffect(actualCourseId, account, session.serverUrl) {
        if (actualCourseId != null && (session as? ArtemisContext.Course)?.courseId != actualCourseId) {
            contextProvider.setCourseId(actualCourseId)
        }
    }

    key(account, normalizedMediaOrigin(session.serverUrl)) {
        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onViewExercise = onViewExercise,
            onSidebarToggle = onSidebarToggle,
            initialUnitId = initialUnitId,
            initialAttachmentId = initialAttachmentId,
            initialPage = initialPage,
            displayedCourseId = displayedCourseId,
            initialResolvedAttachment = initialResolvedAttachment
        )
    }
}

@Composable
internal fun LectureScreen(
    modifier: Modifier,
    viewModel: LectureViewModel,
    onViewExercise: (exerciseId: Long) -> Unit,
    onSidebarToggle: () -> Unit = {},
    initialUnitId: Long? = null,
    initialAttachmentId: Int? = null,
    initialPage: Int? = null,
    displayedCourseId: Long? = null,
    initialResolvedAttachment: Attachment? = null
) {
    val lectureDataState by viewModel.lectureDataState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val lectureChannel by viewModel.channelDataState.collectAsState()
    val lectureUnits by viewModel.lectureUnits.collectAsState()
    val unitsLoaded by viewModel.lectureUnitsLoaded.collectAsState()

    LectureScreen(
        modifier = modifier,
        serverUrl = serverUrl,
        lectureDataState = lectureDataState,
        lectureChannel = lectureChannel,
        lectureUnits = lectureUnits,
        onViewExercise = onViewExercise,
        onReloadLecture = viewModel::onRequestReload,
        onUpdateLectureUnitIsComplete = viewModel::updateLectureUnitIsComplete,
        onSidebarToggle = onSidebarToggle,
        initialUnitId = initialUnitId,
        initialAttachmentId = initialAttachmentId,
        initialPage = initialPage,
        unitsLoaded = unitsLoaded,
        displayedCourseId = displayedCourseId,
        initialResolvedAttachment = initialResolvedAttachment
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
    onSidebarToggle: () -> Unit,
    initialUnitId: Long? = null,
    initialAttachmentId: Int? = null,
    initialPage: Int? = null,
    unitsLoaded: Boolean = true,
    displayedCourseId: Long? = null,
    initialResolvedAttachment: Attachment? = null
) {
    val context = LocalContext.current
    val linkOpener = LocalLinkOpener.current

    val lectureTitle = lectureDataState.bind<String?> { it.title }.orElse(null)

    // Set if the user clicked on a file attachment.
    var pendingAttachmentId: Int? by rememberSaveable { mutableStateOf(null) }
    val loadedLecture = (lectureDataState as? DataState.Success)?.data
    var useResolvedDocument by remember { mutableStateOf(false) }
    val pendingOpenFileAttachment = if (useResolvedDocument && initialResolvedAttachment?.id == pendingAttachmentId) initialResolvedAttachment else (loadedLecture?.attachments.orEmpty() + lectureUnits.filterIsInstance<LectureUnitAttachmentVideo>().mapNotNull { it.attachment })
        .firstOrNull { it.id == pendingAttachmentId }
    var pendingOpenFileAttachmentByUrl: String? by rememberSaveable { mutableStateOf(null) }
    var pendingPage: Int? by rememberSaveable { mutableStateOf(null) }
    var requestedUnitId: Long? by rememberSaveable { mutableStateOf(null) }
    var missingTarget by rememberSaveable { mutableStateOf(false) }
    var targetHandled by rememberSaveable(initialUnitId, initialAttachmentId, initialPage) { mutableStateOf(false) }
    LaunchedEffect(loadedLecture, lectureUnits, unitsLoaded, targetHandled) {
        if (!targetHandled && loadedLecture != null && unitsLoaded && (initialUnitId != null || initialAttachmentId != null)) {
            when (val target = resolveLectureMediaTarget(loadedLecture, lectureUnits, initialUnitId, initialAttachmentId, initialPage)) {
                is LectureMediaDestination.Unit -> requestedUnitId = target.unit.id
                is LectureMediaDestination.Slides -> { useResolvedDocument = initialResolvedAttachment != null; pendingAttachmentId = target.attachment.id; pendingPage = target.page }
                LectureMediaDestination.Missing -> missingTarget = true
            }
            targetHandled = true
        }
    }

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
        val markdownTransformer = rememberLectureArtemisMarkdownTransformer(serverUrl, loadedLecture?.course?.id?.takeIf { it > 0 } ?: displayedCourseId)
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
                onRequestOpenAttachment = {
                    if (it.link.isNullOrBlank() && it.studentVersion.isNullOrBlank()) missingTarget = true
                    else { useResolvedDocument = false; pendingAttachmentId = it.id; pendingPage = null }
                },
                onDisplaySetCompletedFailureDialog = {
                    displaySetCompletedFailureDialog = true
                },
                onReloadLecture = onReloadLecture,
                onUpdateLectureUnitIsComplete = onUpdateLectureUnitIsComplete,
                linkResolver = linkResolver,
                initialUnitId = requestedUnitId
            )

            AttachmentHandler(
                url = pendingOpenFileAttachmentByUrl,
                onDismiss = { pendingOpenFileAttachmentByUrl = null }
            )

            val currentPendingOpenFileAttachment = pendingOpenFileAttachment
            if (currentPendingOpenFileAttachment != null) {
                val fileName = currentPendingOpenFileAttachment.name.orEmpty()
                val link = currentPendingOpenFileAttachment.link.orEmpty()

                val actualLink = currentPendingOpenFileAttachment.studentVersion ?: link
                val url = if (mediaHttpUri(actualLink) != null) actualLink
                    else AttachmentUtil.buildOpenAttachmentLink(serverUrl, actualLink)
                val formattedUrl = if (currentPendingOpenFileAttachment.studentVersion != null || !sameMediaOrigin(serverUrl, url)) url
                    else AttachmentUtil.createAttachmentFileUrl(url, fileName, true)

                AttachmentHandler(
                    url = formattedUrl,
                    onDismiss = { pendingAttachmentId = null; pendingPage = null; useResolvedDocument = false },
                    initialPage = pendingPage,
                    mediaIdentity = "lecture/${loadedLecture?.id}/attachment/${currentPendingOpenFileAttachment.id}"
                )
            }

            if (missingTarget) {
                TextAlertDialog(
                    title = stringResource(CoreR.string.lecture_media_missing),
                    text = stringResource(CoreR.string.lecture_media_access_recovery),
                    confirmButtonText = stringResource(CoreR.string.lecture_media_retry),
                    dismissButtonText = stringResource(CoreR.string.lecture_media_close),
                    onPressPositiveButton = { missingTarget = false; targetHandled = false; onReloadLecture() },
                    onDismissRequest = { missingTarget = false }
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