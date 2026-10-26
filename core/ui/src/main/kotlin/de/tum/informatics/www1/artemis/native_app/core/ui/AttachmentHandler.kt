package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.AttachmentUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaProgressKey
import de.tum.informatics.www1.artemis.native_app.core.ui.media.sameMediaOrigin
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.ImageFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AttachmentHandler(
    url: String?,
    onDismiss: () -> Unit,
    initialPage: Int? = null,
    mediaIdentity: String = "attachment",
    onPageOpened: (() -> Unit)? = null,
    displayName: String? = null,
) {
    if (url == null) return

    val context = LocalContext.current
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

    if (mediaHttpUri(url) == null) {
        TextAlertDialog(
            title = stringResource(R.string.lecture_media_invalid),
            text = stringResource(R.string.lecture_media_failed),
            confirmButtonText = stringResource(R.string.lecture_media_close),
            dismissButtonText = null,
            onPressPositiveButton = onDismiss,
            onDismissRequest = onDismiss,
        )
        return
    }

    val fileName = mediaHttpUri(url)?.path?.substringAfterLast('/').orEmpty()
    val account = (artemisContext as? ArtemisContext.LoggedIn)?.loginName.orEmpty()
    val progressKey = mediaProgressKey(account, artemisContext.serverUrl, mediaIdentity, url)
    val openedScope = remember(url) { account to artemisContext.serverUrl }
    if (openedScope != (account to artemisContext.serverUrl)) {
        androidx.compose.runtime.LaunchedEffect(openedScope) { onDismiss() }
        return
    }
    val token =
        if (sameMediaOrigin(artemisContext.serverUrl, url)) artemisContext.authTokenOrEmptyString
        else ""
    val type = AttachmentUtil.detectAttachmentType(url)

    when (type) {
        is AttachmentUtil.AttachmentType.PDF -> {
            val opener = LocalLinkOpener.current
            var failed by remember(url, artemisContext) { mutableStateOf(false) }
            var attempt by remember(url, artemisContext) { mutableStateOf(0) }
            androidx.compose.runtime.LaunchedEffect(url, progressKey, attempt) {
                val request =
                    de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageRequest(
                        url,
                        displayName?.takeIf { it.isNotBlank() } ?: fileName,
                        mediaIdentity,
                        de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaPageScope(
                            artemisContext
                        ),
                        de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageKind.PDF,
                        initialPage,
                    )
                if (opener.openMediaPage(request)) {
                    onDismiss()
                    onPageOpened?.invoke()
                } else failed = true
            }
            if (failed)
                TextAlertDialog(
                    title = stringResource(R.string.lecture_media_failed),
                    text = stringResource(R.string.lecture_media_access_recovery),
                    confirmButtonText = stringResource(R.string.lecture_media_retry),
                    dismissButtonText = stringResource(R.string.lecture_media_close),
                    onPressPositiveButton = {
                        failed = false
                        attempt++
                    },
                    onDismissRequest = onDismiss,
                )
        }

        is AttachmentUtil.AttachmentType.Image -> {
            val imageFile =
                ImageFile(
                    url,
                    token,
                    fileName,
                )
            LinkBottomSheet(
                modifier = Modifier.fillMaxSize(),
                state = LinkBottomSheetState.IMAGEVIEWSTATE(imageFile),
                onDismissRequest = onDismiss,
            )
        }

        is AttachmentUtil.AttachmentType.Other -> {
            val scope = rememberCoroutineScope()
            var downloading by remember(url, artemisContext) { mutableStateOf(false) }
            var failed by remember(url, artemisContext) { mutableStateOf(false) }
            TextAlertDialog(
                title =
                    stringResource(
                        if (failed) R.string.lecture_media_failed
                        else R.string.download_attachment_dialog_title
                    ),
                text =
                    stringResource(
                        if (downloading) R.string.lecture_media_loading
                        else if (failed) R.string.lecture_media_access_recovery
                        else R.string.download_attachment_dialog_message
                    ),
                confirmButtonText =
                    if (downloading) null
                    else
                        stringResource(
                            if (failed) R.string.lecture_media_retry
                            else R.string.download_file_attachment_dialog_positive
                        ),
                dismissButtonText = stringResource(R.string.lecture_media_close),
                onPressPositiveButton = {
                    if (!downloading)
                        scope.launch {
                            downloading = true
                            failed = false
                            try {
                                AttachmentUtil.downloadAttachment(
                                    context,
                                    artemisContext,
                                    fileName,
                                    url,
                                )
                                onDismiss()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                failed = true
                            } finally {
                                downloading = false
                            }
                        }
                },
                onDismissRequest = onDismiss,
            )
        }
    }
}
