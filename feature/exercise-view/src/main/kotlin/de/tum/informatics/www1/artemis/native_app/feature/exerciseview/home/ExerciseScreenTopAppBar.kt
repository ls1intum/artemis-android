package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipData
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChip
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChipTextHorizontalPadding
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIconPainter
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.datetime.Instant
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope

/**
 * Display a collapsing top app bar with an incollapsible [TopAppBar] and [TopBarExerciseInformation] as the collapsible part in a column.
 */
@Composable
internal fun CollapsingToolbarScope.ExerciseScreenCollapsingTopBar(
    modifier: Modifier,
    state: CollapsingToolbarScaffoldState,
    isLongToolbar: Boolean,
    exercise: DataState<Exercise>,
    onNavigateBack: () -> Unit,
    onRequestRefresh: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TitleText(
                modifier = Modifier.graphicsLayer { alpha = 1f - state.toolbarState.progress },
                exerciseDataState = exercise,
                maxLines = 1
            )
        },
        navigationIcon = {
            TopAppBarNavigationIcon(onNavigateBack)
        },
        actions = {
            TopAppBarActions(onRequestRefresh = onRequestRefresh)
        }
    )

    TopBarExerciseInformation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
            .parallax(0f),
        titleTextAlpha = state.toolbarState.progress,
        exercise = exercise,
        isLongToolbar = isLongToolbar
    )
}

@Composable
private fun TopAppBarNavigationIcon(onNavigateBack: () -> Unit) {
    IconButton(onClick = onNavigateBack) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
    }
}

@Composable
private fun TopAppBarActions(onRequestRefresh: () -> Unit) {
    IconButton(onClick = onRequestRefresh) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
    }
}

private val placeholderCategoryChips = listOf(
    ExerciseCategoryChipData("WWWW", Color.Cyan),
    ExerciseCategoryChipData("WWWW", Color.Cyan),
    ExerciseCategoryChipData("WWWW", Color.Cyan)
)

/**
 * @param isLongToolbar if the deadline information is displayed on the right side of the toolbar.
 * If false, the information is instead displayed in the column
 */
@Composable
internal fun TopBarExerciseInformation(
    modifier: Modifier,
    titleTextAlpha: Float,
    exercise: DataState<Exercise>,
    isLongToolbar: Boolean
) {
    val dueDate = exercise.bind { it.dueDate }.orElse(null)
    val assessmentDueData = exercise.bind { it.assessmentDueDate }.orElse(null)
    val releaseData = exercise.bind { it.releaseDate }.orElse(null)

    var maxWidth: Int by remember { mutableIntStateOf(0) }
    val updateMaxWidth = { new: Int -> maxWidth = new }

    val dueDateTopBarTextInformation =
        @Composable { date: Instant, hintRes: @receiver:StringRes Int ->
            TopBarTextInformation(
                modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp),
                hintColumnWidth = maxWidth,
                hint = stringResource(id = hintRes),
                dataText = getRelativeTime(to = date).toString(),
                dataColor = getDueDateColor(date),
                updateHintColumnWidth = updateMaxWidth
            )
        }

    val exerciseInfoUi = @Composable {
        EmptyDataStateUi(
            dataState = exercise,
            otherwise = {
                ExerciseCategoryChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(true),
                    chips = placeholderCategoryChips
                )
            }
        ) { loadedExercise ->
            ExerciseCategoryChipRow(
                modifier = Modifier.fillMaxWidth(),
                exercise = loadedExercise
            )
        }

        val currentUserPoints = exercise.bind { exercise ->
            exercise.currentUserPoints?.let(ExercisePointsDecimalFormat::format)
        }.orElse(null)
        val maxPoints = exercise.bind { exercise ->
            exercise.maxPoints?.let(ExercisePointsDecimalFormat::format)
        }.orElse(null)

        val pointsHintText = when {
            currentUserPoints != null && maxPoints != null -> stringResource(
                id = R.string.exercise_view_overview_points_reached,
                currentUserPoints,
                maxPoints
            )

            maxPoints != null -> stringResource(
                id = R.string.exercise_view_overview_points_max,
                maxPoints
            )

            else -> stringResource(id = R.string.exercise_view_overview_points_none)
        }

        Text(
            modifier = Modifier
                .placeholder(exercise !is DataState.Success)
                .padding(bottom = 4.dp),
            text = pointsHintText,
            style = MaterialTheme.typography.bodyLarge
        )

        releaseData?.let {
            dueDateTopBarTextInformation(
                it,
                R.string.exercise_view_overview_hint_assessment_release_date
            )
        }

    }

    val dueDateColumnUi = @Composable { contentModifier: Modifier ->

        Column(
            modifier = contentModifier,
        ) {

            dueDate?.let {
                dueDateTopBarTextInformation(
                    it,
                    R.string.exercise_view_overview_hint_submission_due_date
                )
            }

            assessmentDueData?.let {
                dueDateTopBarTextInformation(
                    it,
                    R.string.exercise_view_overview_hint_assessment_due_date
                )
            }

            val complaintPossible = exercise.bind { exercise ->
                exercise.allowComplaintsForAutomaticAssessments
            }.orElse(false)

            Text(
                modifier = Modifier
                    .placeholder(exercise !is DataState.Success)
                    .padding(bottom = 4.dp),
                text = "Complaint possible: " + if (complaintPossible == true) "Yes" else "No",
                style = MaterialTheme.typography.bodyLarge
            )

        }
    }

    // Actual UI
    Column(
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Text(
            text = "Exercise Details",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 1.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Divider(
            color = Color.Black,
            thickness = 3.dp,
            modifier = Modifier.padding(vertical = 0.dp)
        )

        // Here we make the distinction in the layout between long toolbar and short toolbar

        if (isLongToolbar) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    exerciseInfoUi()
                }

                dueDateColumnUi(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.Bottom)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                exerciseInfoUi()
                dueDateColumnUi(Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Text information composable that achieves a table like layout, where the hint is the first column
 * and the data is the second column.
 */
@Composable
private fun TopBarTextInformation(
    modifier: Modifier,
    hintColumnWidth: Int,
    hint: String,
    dataText: String,
    dataColor: Color?,
    updateHintColumnWidth: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val assignedWidth = maxOf(hintColumnWidth, placeable.width)
                if (assignedWidth > hintColumnWidth) {
                    updateHintColumnWidth(assignedWidth)
                }

                layout(width = assignedWidth, height = placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
        )

        val dataModifier = Modifier

        if (dataColor != null) {
            ExerciseInfoChip(modifier = dataModifier, color = dataColor, text = dataText)
        } else {
            Text(
                modifier = dataModifier.padding(horizontal = ExerciseInfoChipTextHorizontalPadding),
                text = dataText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun getDueDateColor(dueDate: Instant): Color =
    if (dueDate.hasPassed()) Color.Red else Color.Green

@Composable
private fun TitleText(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int
) {
    val fontSize = style.fontSize

    val (titleText, inlineContent) = remember(exerciseDataState) {
        val text = buildAnnotatedString {
            appendInlineContent("icon")
            append(" ")
            append(
                exerciseDataState.bind { it.title }.orNull()
                    ?: "Exercise name placeholder"
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

    Text(
        text = titleText,
        inlineContent = inlineContent,
        modifier = modifier
            .placeholder(exerciseDataState !is DataState.Success)
            .semantics { set(SemanticsProperties.Text, listOf(AnnotatedString(exerciseDataState.bind { it.title }.orNull().orEmpty()))) },
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun StaticTopAppBar(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    isLongToolbar: Boolean,
    onNavigateBack: () -> Unit,
    onRequestReloadExercise: () -> Unit
) {
    Column(modifier = modifier) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { TitleText(modifier = modifier, maxLines = 1, exerciseDataState = exerciseDataState) },
            navigationIcon = {
                TopAppBarNavigationIcon(onNavigateBack = onNavigateBack)
            },
            actions = {
                TopAppBarActions(onRequestRefresh = onRequestReloadExercise)
            }
        )
        TopBarExerciseInformation(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
            titleTextAlpha = 1f,
            exercise = exerciseDataState,
            isLongToolbar = isLongToolbar
        )
    }
}