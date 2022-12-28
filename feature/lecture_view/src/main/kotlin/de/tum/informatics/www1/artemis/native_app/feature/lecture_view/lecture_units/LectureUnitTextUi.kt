package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

@Composable
internal fun LectureUnitTextUi(modifier: Modifier, lectureUnit: LectureUnitText) {
    var isExpanded: Boolean by rememberSaveable(lectureUnit) { mutableStateOf(false) }

    Card(modifier = modifier, onClick = { isExpanded = !isExpanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = lectureUnit.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            MarkdownText(
                markdown = lectureUnit.content.orEmpty(),
                modifier = Modifier.fillMaxWidth(),
                maxLines = if (isExpanded) Integer.MAX_VALUE else 1
            )
        }
    }
}