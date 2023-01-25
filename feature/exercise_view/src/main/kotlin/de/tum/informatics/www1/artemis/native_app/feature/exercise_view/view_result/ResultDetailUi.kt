package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.BuildLogEntry
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.isPreliminary
import de.tum.informatics.www1.artemis.native_app.core.ui.date.isInFuture
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultBad
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@Composable
internal fun ResultDetailUi(
    modifier: Modifier,
    exercise: Exercise,
    latestResult: Result,
    feedbackItems: List<ExerciseViewModel.FeedbackItem>,
    latestIndividualDueDate: Instant?,
    buildLogs: List<BuildLogEntry>
) {
    val showMissingAutomaticFeedbackInformation = latestIndividualDueDate?.isInFuture() ?: false

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showMissingAutomaticFeedbackInformation) {
            MissingFeedbackInformation(
                modifier = Modifier.fillMaxWidth(),
                latestIndividualDueDate = latestIndividualDueDate
            )

            Divider()
        }

        if (latestResult.isPreliminary.collectAsState(initial = false).value) {
            ResultIsPreliminaryWarning(
                modifier = Modifier.fillMaxWidth(),
                assessmentType = exercise.assessmentType
            )

            Divider()
        }

        ScoreSection(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            latestResult = latestResult
        )

        if (exercise is ProgrammingExercise) {
            Divider()

            Text(
                text = stringResource(id = R.string.result_view_programming_exercise_chart_section_title),
                style = MaterialTheme.typography.headlineMedium
            )

            ScoreResultsCard(
                modifier = Modifier.fillMaxWidth(),
                exercise = exercise,
                feedbackItems = feedbackItems
            )
        }

        if (buildLogs.isNotEmpty()) {
            Divider()

            Text(
                text = stringResource(id = R.string.result_view_build_log_section_title),
                style = MaterialTheme.typography.headlineMedium
            )

            buildLogs.forEach { buildLog ->
                BuildLogCard(modifier = Modifier.fillMaxWidth(), buildLog = buildLog)
            }
        }

        if (feedbackItems.isNotEmpty()) {
            Divider()

            Text(
                text = stringResource(id = R.string.result_view_feedback_section_title),
                style = MaterialTheme.typography.headlineMedium
            )

            feedbackItems.forEach { feedbackItem ->
                FeedbackCard(modifier = Modifier.fillMaxWidth(), feedbackItem = feedbackItem)
            }
        }
    }
}

@Composable
private fun MissingFeedbackInformation(modifier: Modifier, latestIndividualDueDate: Instant?) {
    val formattedDate = remember(latestIndividualDueDate) {
        if (latestIndividualDueDate == null) {
            ""
        } else {
            SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.MEDIUM,
                SimpleDateFormat.SHORT
            )
                .format(Date.from(latestIndividualDueDate.toJavaInstant()))
        }
    }

    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.result_view_automatic_feedback_missing,
            formattedDate
        ),
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic
    )
}

@Composable
private fun ResultIsPreliminaryWarning(
    modifier: Modifier,
    assessmentType: Exercise.AssessmentType?
) {
    val text = stringResource(
        id = if (assessmentType == Exercise.AssessmentType.AUTOMATIC) {
            R.string.result_view_preliminary_result_semi_automatic
        } else R.string.result_view_preliminary_result_automatic
    )

    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ScoreSection(
    modifier: Modifier,
    exercise: Exercise,
    latestResult: Result
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.result_view_score_section_title),
            style = MaterialTheme.typography.headlineMedium
        )

        val userScore = latestResult.score
        val achievableScore = exercise.maxPoints
        val achievedPoints = userScore?.let { userScore / 100f * (achievableScore ?: 0f) }

        if (userScore != null && achievableScore != null) {
            val formattedAchievedPoints = remember(achievedPoints) {
                ExercisePointsDecimalFormat.format(achievedPoints)
            }

            val formattedAchievablePoints = remember(achievableScore) {
                ExercisePointsDecimalFormat.format(achievableScore)
            }

            val formattedAchievedPercent = remember(userScore, achievableScore) {
                val percent = userScore / 100f
                DecimalFormat.getPercentInstance().format(percent)
            }

            Text(
                text = stringResource(
                    id = R.string.result_view_score_result,
                    formattedAchievedPoints,
                    formattedAchievablePoints,
                    formattedAchievedPercent
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScoreResultsCard(
    modifier: Modifier,
    exercise: Exercise,
    feedbackItems: List<ExerciseViewModel.FeedbackItem>
) {
    val chartValues = remember(feedbackItems) {
        val maxPoints = exercise.maxPoints ?: 0f
        val maxPointsWithBonus = maxPoints + (exercise.bonusPoints ?: 0f)

        val positiveCredits =
            feedbackItems
                .filter { it.type != ExerciseViewModel.FeedbackItemType.Test && it.credits != null && it.credits > 0 }
                .sumOf { it.creditsOrZero }

        val maxCodeIssueCredits =
            if (exercise is ProgrammingExercise) {
                val maxStaticCodeAnalysisPenalty = exercise.maxStaticCodeAnalysisPenalty

                if (exercise.staticCodeAnalysisEnabled && maxStaticCodeAnalysisPenalty != null) {
                    maxPoints.toDouble() * maxStaticCodeAnalysisPenalty.toDouble() / 100.0
                } else Double.MAX_VALUE
            } else Double.MAX_VALUE

        val codeIssueCredits =
            -feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue }
                .sumOf { it.actualCreditsOrZero }
                .coerceAtMost(maxCodeIssueCredits)
        val codeIssuePenalties =
            feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue }
                .sumOf { it.creditsOrZero }
        val negativeCredits =
            -feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue && it.credits != null && it.credits < 0 }
                .sumOf { it.creditsOrZero }

        val testCaseCredits =
            feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Test }
                .sumOf { it.creditsOrZero }
                .coerceAtMost(maxPointsWithBonus.toDouble())

        val accuracy = exercise.course?.accuracyOfScores?.toDouble() ?: 1.0

        val appliedNegativePoints = (codeIssueCredits + negativeCredits).rounded(accuracy)
        val receivedNegativePoints = (codeIssuePenalties + negativeCredits).rounded(accuracy)
        val positivePoints = (testCaseCredits + positiveCredits).rounded(accuracy)

        ChartValues(
            positivePoints,
            appliedNegativePoints,
            receivedNegativePoints,
            maxPoints,
            maxPointsWithBonus
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10))
        ) {
            @Suppress("LocalVariableName")
            val Bar = @Composable { percentage: Float, color: Color ->
                if (percentage > 0) {
                    Box(
                        modifier = Modifier
                            .weight(percentage)
                            .fillMaxHeight()
                            .background(color = color)
                    )
                }
            }

            Bar(chartValues.positivePointsPercentage.toFloat(), resultSuccess)
            Bar(chartValues.warningPointsPercentage.toFloat(), resultMedium)
            Bar(chartValues.errorPointsPercentage.toFloat(), resultBad)
            Bar(chartValues.nothingPercentage.toFloat(), Color.Gray)
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            val scoreResultModifier = Modifier.weight(1f)
            ScoreResult(
                modifier = scoreResultModifier,
                title = stringResource(id = R.string.result_view_feedback_overview_category_correct),
                points = chartValues.positivePoints,
                percentage = chartValues.positivePointsPercentage,
                colors = successCardColors
            )

            ScoreResult(
                modifier = scoreResultModifier,
                title = stringResource(id = R.string.result_view_feedback_overview_category_warning),
                points = chartValues.appliedNegativePoints,
                percentage = chartValues.warningPointsPercentage,
                colors = neutralCardColors
            )

            ScoreResult(
                modifier = scoreResultModifier,
                title = stringResource(id = R.string.result_view_feedback_overview_category_wrong),
                points = chartValues.receivedNegativePoints,
                percentage = chartValues.receivedNegativePoints / chartValues.maxPoints,
                colors = issueCardColors
            )
        }
    }

}

private val percentageFormat = DecimalFormat.getPercentInstance()

@Composable
private fun ScoreResult(
    modifier: Modifier,
    title: String,
    points: Double,
    percentage: Double,
    colors: CardColors
) {
    val pointText = remember(points) {
        ExercisePointsDecimalFormat.format(points)
    }

    val percentageText = remember(percentage) {
        percentageFormat.format(percentage)
    }

    Box(modifier = modifier) {
        OutlinedCard(modifier = Modifier.align(Alignment.Center), colors = colors) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = pointText,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private val issueCardColors: CardColors
    @Composable get() {
        return CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

private val neutralCardColors: CardColors
    @Composable get() {
        return CardDefaults.outlinedCardColors(
            contentColor = Color(0xFF5A5208),
            containerColor = Color(0xFFEEEBCE)
        )
    }

private val successCardColors: CardColors
    @Composable get() {
        return CardDefaults.outlinedCardColors(
            contentColor = Color(0xFF00801D),
            containerColor = Color(0xFFD1E4D4)
        )
    }

@Composable
private fun FeedbackCard(modifier: Modifier, feedbackItem: ExerciseViewModel.FeedbackItem) {
    val cardColors = when (feedbackItem.type) {
        ExerciseViewModel.FeedbackItemType.Issue -> neutralCardColors
        ExerciseViewModel.FeedbackItemType.Test ->
            if (feedbackItem.positive == true) successCardColors
            else issueCardColors
        else -> {
            if (feedbackItem.credits == null || feedbackItem.credits == 0f) {
                neutralCardColors
            } else if (feedbackItem.positive == true || (feedbackItem.credits > 0f)) {
                successCardColors
            } else {
                issueCardColors
            }
        }
    }

    OutlinedCard(
        modifier = modifier,
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = feedbackItem.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (feedbackItem.credits != null) {
                    val text: String = remember(feedbackItem.credits) {
                        DecimalFormat.getNumberInstance().format(feedbackItem.credits)
                    }

                    Text(
                        modifier = Modifier,
                        text = text,
//                        fontStyle = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (!feedbackItem.title.isNullOrBlank()) {
                Text(text = feedbackItem.title, style = MaterialTheme.typography.titleSmall)
            }

            if (feedbackItem.text != null) {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = feedbackItem.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BuildLogCard(modifier: Modifier, buildLog: BuildLogEntry) {
    val cardColors = when (buildLog.type) {
        BuildLogEntry.Type.ERROR -> issueCardColors
        BuildLogEntry.Type.WARNING -> neutralCardColors
        BuildLogEntry.Type.OTHER -> neutralCardColors
    }

    OutlinedCard(modifier = modifier, colors = cardColors) {
        var showMoreButtonDisplayed by remember {
            mutableStateOf(false)
        }

        var showWholeLog by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AnimatedContent(targetState = showWholeLog) { doShowWholeLog ->
                Text(
                    text = buildLog.log,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (doShowWholeLog) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    onTextLayout = {
                        if (it.hasVisualOverflow) {
                            showMoreButtonDisplayed = true
                        }
                    }
                )
            }

            if (showMoreButtonDisplayed) {
                Button(onClick = { showWholeLog = !showWholeLog }) {
                    Text(text = stringResource(id = R.string.result_view_build_log_show_entire_log))
                }
            }
        }
    }
}

/**
 * Rounds the value to the next multiple of accuracy.
 */
private fun Double.rounded(accuracy: Double): Double {
    return round(this * (1.0 / accuracy)) * accuracy
}

private data class ChartValues(
    val positivePoints: Double,
    val appliedNegativePoints: Double,
    val receivedNegativePoints: Double,
    val maxPoints: Float,
    val maxPointsWithBonus: Float
) {
    val positivePointsPercentage: Double = positivePoints / maxPoints
    val warningPointsPercentage: Double = appliedNegativePoints / maxPoints
    val errorPointsPercentage: Double = receivedNegativePoints / maxPoints

    val nothingPercentage: Double =
        1f - (positivePointsPercentage + warningPointsPercentage + errorPointsPercentage)
}

private class FeedbackItemProvider : PreviewParameterProvider<ExerciseViewModel.FeedbackItem> {
    override val values: Sequence<ExerciseViewModel.FeedbackItem> = sequenceOf(
        ExerciseViewModel.FeedbackItem(
            type = ExerciseViewModel.FeedbackItemType.Feedback,
            category = R.string.result_view_feedback_category_regular,
            title = "Issue with 1a",
            text = "1a is wrong || ".repeat(10),
            positive = false,
            credits = -3f,
            actualCredits = null
        ),
        ExerciseViewModel.FeedbackItem(
            type = ExerciseViewModel.FeedbackItemType.Feedback,
            category = R.string.result_view_feedback_category_regular,
            title = "Do this better",
            text = "you coul dhave done something better",
            positive = null,
            credits = null,
            actualCredits = null
        ),
        ExerciseViewModel.FeedbackItem(
            type = ExerciseViewModel.FeedbackItemType.Test,
            category = R.string.result_view_feedback_category_regular,
            title = "checkSomethingTest Wrong",
            text = "You have not changed something to something.",
            positive = false,
            credits = -3f,
            actualCredits = null
        ),
        ExerciseViewModel.FeedbackItem(
            type = ExerciseViewModel.FeedbackItemType.Test,
            category = R.string.result_view_feedback_category_regular,
            title = "checkSomethingTest Correct",
            text = "Good job, successfully does something",
            positive = true,
            credits = 1f,
            actualCredits = null
        ),
    )
}

@Preview
@Composable
private fun FeedbackCardPreview(
    @PreviewParameter(FeedbackItemProvider::class) feedbackItem: ExerciseViewModel.FeedbackItem
) {
    FeedbackCard(
        modifier = Modifier.fillMaxWidth(),
        feedbackItem = feedbackItem
    )
}

private class BuildLogEntryProvider : PreviewParameterProvider<BuildLogEntry> {
    override val values: Sequence<BuildLogEntry> = sequenceOf(
        BuildLogEntry(
            log = "Short warning log",
            type = BuildLogEntry.Type.WARNING
        ),
        BuildLogEntry(
            log = "Very long error log\n".repeat(40),
            type = BuildLogEntry.Type.ERROR
        )
    )
}

@Preview
@Composable
private fun BuildLogCardPreview(
    @PreviewParameter(BuildLogEntryProvider::class) entry: BuildLogEntry
) {
    BuildLogCard(modifier = Modifier.fillMaxWidth(), buildLog = entry)
}