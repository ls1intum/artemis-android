package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIconPainter


@Composable
internal fun ExerciseScreenTopAppBar(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    onNavigateBack: () -> Unit
) {
    Column(modifier = modifier) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { TitleText(modifier = modifier, maxLines = 1, exerciseDataState = exerciseDataState) },
            navigationIcon = {
                TopAppBarNavigationIcon(onNavigateBack = onNavigateBack)
            }
        )
    }
}

@Composable
private fun TopAppBarNavigationIcon(onNavigateBack: () -> Unit) {
    IconButton(onClick = onNavigateBack) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
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
            .placeholder(exerciseDataState !is DataState.Success)
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
            Icon(
                painter = getExerciseTypeIconPainter(exerciseDataState.orNull()),
                contentDescription = null
            )
        }
    )

    text to inlineContent
}
