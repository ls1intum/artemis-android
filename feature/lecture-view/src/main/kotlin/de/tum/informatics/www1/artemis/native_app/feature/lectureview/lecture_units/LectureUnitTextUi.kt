package de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

@Composable
internal fun LectureUnitTextUi(
    modifier: Modifier,
    lectureUnit: LectureUnitText
) {
    LectureUnitBody(modifier = modifier, name = lectureUnit.name.orEmpty()) {
        MarkdownText(
            markdown = lectureUnit.content.orEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}