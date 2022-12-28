package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitOnline
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitText
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitUnknown
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture_units.LectureUnitTextUi

@Composable
internal fun OverviewTab(
    modifier: Modifier,
    description: String?,
    lectureUnits: List<LectureUnit>
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (description != null) {
            item {
                DescriptionSection(
                    modifier = Modifier.fillMaxWidth(),
                    description = description
                )
            }
        }

        if (lectureUnits.isNotEmpty()) {
            lectureUnitSection(
                modifier = Modifier.fillMaxWidth(),
                lectureUnits = lectureUnits
            )
        }
    }
}

@Composable
private fun DescriptionSection(modifier: Modifier, description: String) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.lecture_view_overview_section_description),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        MarkdownText(
            markdown = description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun LazyListScope.lectureUnitSection(modifier: Modifier, lectureUnits: List<LectureUnit>) {
    item {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.lecture_view_overview_section_lecture_units),
            style = MaterialTheme.typography.headlineMedium
        )
    }

    items(lectureUnits) { lectureUnit ->
        LectureUnitListItem(modifier = modifier, lectureUnit = lectureUnit)
    }
}

@Composable
private fun LectureUnitListItem(modifier: Modifier, lectureUnit: LectureUnit) {
    when (lectureUnit) {
        is LectureUnitAttachment -> TODO()
        is LectureUnitExercise -> TODO()
        is LectureUnitOnline -> TODO()
        is LectureUnitText -> {
            LectureUnitTextUi(
                modifier = modifier,
                lectureUnit = lectureUnit
            )
        }

        is LectureUnitUnknown -> TODO()
        is LectureUnitVideo -> TODO()
    }
}