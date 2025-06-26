package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.AttachmentUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.ImageFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

@Composable
fun AttachmentHandler(
    url: String?,
    onDismiss: () -> Unit
) {
    if (url == null) return

    val context = LocalContext.current
    val artemisContext by LocalArtemisContextProvider.current.collectArtemisContextAsState()

    val fileName = url.substringAfterLast("/")
    val type = AttachmentUtil.detectAttachmentType(url)

    when (type) {
        is AttachmentUtil.AttachmentType.PDF -> {
            val pdfFile = PdfFile(
                url,
                artemisContext.authTokenOrEmptyString,
                fileName
            )
            LinkBottomSheet(
                modifier = Modifier.fillMaxSize(),
                state = LinkBottomSheetState.PDFVIEWSTATE(pdfFile),
                onDismissRequest = onDismiss
            )
        }

        is AttachmentUtil.AttachmentType.Image -> {
            val imageFile = ImageFile(
                url,
                artemisContext.authTokenOrEmptyString,
                fileName
            )
            LinkBottomSheet(
                modifier = Modifier.fillMaxSize(),
                state = LinkBottomSheetState.IMAGEVIEWSTATE(imageFile),
                onDismissRequest = onDismiss
            )
        }

        is AttachmentUtil.AttachmentType.Other -> {
            DownloadAttachmentDialog(
                onDismissRequest = onDismiss,
                onDownload = {
                    AttachmentUtil.downloadAttachment(
                        context = context,
                        artemisContext = artemisContext,
                        link = url,
                        name = fileName
                    )
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DownloadAttachmentDialog(
    onDismissRequest: () -> Unit,
    onDownload: () -> Unit
) {
    TextAlertDialog(
        title = stringResource(id = R.string.download_attachment_dialog_title),
        text = stringResource(id = R.string.download_attachment_dialog_message),
        confirmButtonText = stringResource(id = R.string.download_file_attachment_dialog_positive),
        dismissButtonText = stringResource(id = R.string.download__file_attachment_dialog_negative),
        onPressPositiveButton = onDownload,
        onDismissRequest = onDismissRequest
    )
} 