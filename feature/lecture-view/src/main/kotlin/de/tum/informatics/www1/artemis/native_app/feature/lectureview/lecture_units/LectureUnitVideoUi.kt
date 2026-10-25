package de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.LectureVideoPlayer
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.media.classifyMediaSource
import de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaSourceKind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.R

@Composable
internal fun LectureUnitVideoUi(
    modifier: Modifier,
    lectureUnit: LectureUnitAttachmentVideo,
    onClickOpenLink: () -> Unit,
    onClickOpenAttachment: (Attachment) -> Unit,
) {
    val linkOpener = LocalLinkOpener.current
    var showPlayer by rememberSaveable(lectureUnit.id) { mutableStateOf(false) }
    if (showPlayer) LectureVideoPlayer(lectureUnit.videoSource.orEmpty(), "unit/${lectureUnit.id}") { showPlayer = false }
    LectureUnitBody(
        modifier = modifier,
        name = lectureUnit.name.orEmpty(),
    ) {
        val description = lectureUnit.description
        if (!description.isNullOrEmpty()) {
            MarkdownText(
                markdown = description,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (lectureUnit.hasVideo) {
            OpenLinkButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClickOpenLink = {
                    val url = lectureUnit.videoSource.orEmpty()
                    if (classifyMediaSource(url) == MediaSourceKind.WEB) {
                        linkOpener.openLectureVideoPage(url)
                    } else showPlayer = true
                },
                text = stringResource(R.string.lecture_view_open_video_link_button),
            )
        }

        if (lectureUnit.hasAttachment) {
            OpenLinkButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClickOpenLink = { onClickOpenAttachment(lectureUnit.attachment!!) },
                icon = Icons.Default.Description,
                text = stringResource(R.string.lecture_view_open_attachment_button)
            )
        }
    }
}