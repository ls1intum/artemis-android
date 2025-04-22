package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIconPainter
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout


@Composable
internal fun ExerciseScreenTopAppBar(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    onSidebarToggle: () -> Unit = {},
) {
    val layout = getArtemisAppLayout()

    Column(modifier = modifier) {
        ArtemisTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = {
                TitleText(
                    modifier = modifier,
                    maxLines = 1,
                    exerciseDataState = exerciseDataState
                )
            },
            navigationIcon = {
                if (layout == ArtemisAppLayout.Tablet) {
                    IconButton(onClick = onSidebarToggle) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                            contentDescription = null
                        )
                    }
                } else NavigationBackButton()
            }
        )
    }
}

@Composable
private fun TitleText(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int
) {
    val fontSize = style.fontSize

    val (titleText, inlineContent) = rememberTitleTextWithInlineContent(exerciseDataState, fontSize)

    Text(
        text = titleText,
        inlineContent = inlineContent,
        modifier = modifier
            .placeholder(
                visible = exerciseDataState !is DataState.Success,
                color = MaterialTheme.colorScheme.surface
            )
            .semantics {
                set(
                    SemanticsProperties.Text,
                    listOf(
                        AnnotatedString(
                            exerciseDataState
                                .bind { it.title }
                                .orNull()
                                .orEmpty()
                        )
                    )
                )
            },
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun rememberTitleTextWithInlineContent(
    exerciseDataState: DataState<Exercise>,
    fontSize: TextUnit
) = remember(exerciseDataState) {
    val text = buildAnnotatedString {
        appendInlineContent("icon")
        append(" ")
        append(
            exerciseDataState.bind { it.title }.orNull().orEmpty()
        )
    }

    val inlineContent = mapOf(
        "icon" to InlineTextContent(
            Placeholder(
                fontSize,
                fontSize,
                PlaceholderVerticalAlign.TextCenter
            )
        ) {
            val painter = getExerciseTypeIconPainter(exerciseDataState.orNull())
            painter?.let {
                Icon(
                    painter = painter,
                    contentDescription = null
                )
            }
        }
    )

    text to inlineContent
}
