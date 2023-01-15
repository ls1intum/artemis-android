package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.R

@Composable
internal fun LectureUnitOnlineUi(
    modifier: Modifier,
    lectureUnit: LectureUnitOnline,
    onClickOpenLink: () -> Unit
) {
    LectureUnitWithLinkUi(
        modifier = modifier,
        name = lectureUnit.name.orEmpty(),
        text = lectureUnit.description,
        onClickOpenLink = onClickOpenLink
    )
}

@Composable
internal fun LectureUnitWithLinkUi(
    modifier: Modifier,
    name: String,
    text: String?,
    onClickOpenLink: () -> Unit
) {
    LectureUnitBody(modifier = modifier, name = name) {
        if (text != null) {
            MarkdownText(
                markdown = text,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onClickOpenLink
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null
                )

                Text(
                    text = stringResource(id = R.string.lecture_view_open_link_button)
                )
            }
        }
    }
}