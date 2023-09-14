package de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachment

@Composable
internal fun LectureUnitAttachmentUi(modifier: Modifier, lectureUnit: LectureUnitAttachment, onClickOpenLink: () -> Unit) {
    LectureUnitWithLinkUi(
        modifier = modifier,
        name = lectureUnit.name.orEmpty(),
        text = lectureUnit.description,
        onClickOpenLink = onClickOpenLink
    )
}