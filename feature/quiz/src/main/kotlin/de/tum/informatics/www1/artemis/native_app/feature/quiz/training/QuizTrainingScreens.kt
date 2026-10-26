package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ProvideReviewImages
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.QuizQuestionBody
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable private data class QuizTrainingRoute(val courseId: Long)

fun NavController.navigateToQuizTraining(courseId: Long) {
    navigate(QuizTrainingRoute(courseId))
}

fun NavGraphBuilder.quizTraining() {
    animatedComposable<QuizTrainingRoute> { entry ->
        val route = entry.toRoute<QuizTrainingRoute>()
        val vm: QuizTrainingViewModel = koinViewModel { parametersOf(route.courseId) }
        QuizTrainingScreens(vm)
    }
}

@Composable
private fun QuizTrainingScreens(vm: QuizTrainingViewModel) {
    val state by vm.session.state.collectAsState()
    val pages = rememberNavController()
    Scaffold(
        topBar = {
            ArtemisTopAppBar(
                title = { Text(stringResource(R.string.training_title)) },
                navigationIcon = { NavigationBackButton() },
            )
        }
    ) { insets ->
        val captured = vm.captured
        if (state.stage == TrainingStage.ACCESS_LOST || captured == null) {
            Text(stringResource(R.string.training_access), Modifier.padding(insets).padding(24.dp))
            return@Scaffold
        }
        ProvideReviewImages(captured, onDenied = vm.session::invalidate) {
            Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
                NavHost(
                    pages,
                    startDestination = "overview",
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                ) {
                    composable("overview") {
                        TrainingOverviewPage(
                            state,
                            vm,
                            onStart = {
                                if (state.current == null && state.stage != TrainingStage.SUMMARY)
                                    vm.act { start() }
                                pages.navigate("session") { launchSingleTop = true }
                            },
                        )
                    }
                    composable("session") {
                        TrainingSessionPage(state, vm, onFinish = { pages.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingOverviewPage(
    state: TrainingState,
    vm: QuizTrainingViewModel,
    onStart: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.training_heading),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(stringResource(R.string.training_intro), style = MaterialTheme.typography.bodyLarge)
        state.overview?.currentUserEntry?.let { entry ->
            Text(
                stringResource(R.string.training_progress),
                style = MaterialTheme.typography.titleMedium,
            )
            entry.score?.let { Text(stringResource(R.string.training_points, it)) }
            entry.answeredCorrectly?.let {
                Text(pluralStringResource(R.plurals.training_correct, it, it))
            }
            entry.totalQuestions?.let {
                Text(
                    pluralStringResource(
                        R.plurals.training_pool,
                        it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                        it,
                    )
                )
            }
            entry.streak?.let { Text(pluralStringResource(R.plurals.training_streak, it, it)) }
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(
                    if (state.current != null || state.stage == TrainingStage.SUMMARY)
                        R.string.training_continue
                    else R.string.training_start
                )
            )
        }
        HorizontalDivider()
        Text(
            stringResource(R.string.training_privacy),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(
                if (state.overview?.hasUserSetSettings == false) R.string.training_private_first
                else R.string.training_privacy_detail
            )
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.training_public), Modifier.weight(1f))
            Switch(
                checked = state.privacy?.showInLeaderboard == true,
                onCheckedChange = { show -> vm.act { updatePrivacy(show) } },
                enabled = state.privacy != null && !state.privacySaving,
            )
        }
        if (state.overview?.hasUserSetSettings == false)
            OutlinedButton(
                onClick = { vm.act { updatePrivacy(false) } },
                enabled = !state.privacySaving,
            ) {
                Text(stringResource(R.string.training_keep_private))
            }
        if (state.overviewError) {
            Text(
                stringResource(R.string.training_overview_error),
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = { vm.act { loadOverview() } }) {
                Text(stringResource(R.string.training_retry))
            }
        }
        state.overview?.let { overview ->
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(R.string.training_leaderboard))
            }
            if (expanded) {
                overview.currentUserEntry?.selectedLeague?.let {
                    Text(stringResource(R.string.training_league, it))
                }
                if (overview.leaderboardEntries.isEmpty())
                    Text(stringResource(R.string.training_no_ranking))
                overview.leaderboardEntries.forEach { entry ->
                    Text(
                        listOfNotNull(
                                entry.rank?.let { "#$it" },
                                entry.userName,
                                entry.score?.let { stringResource(R.string.training_points, it) },
                            )
                            .joinToString(" · ")
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingSessionPage(
    state: TrainingState,
    vm: QuizTrainingViewModel,
    onFinish: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            pluralStringResource(R.plurals.training_count, state.answered, state.answered),
            style = MaterialTheme.typography.titleMedium,
        )
        when (state.stage) {
            TrainingStage.HOME,
            TrainingStage.LOADING -> {
                CircularProgressIndicator()
                Text(stringResource(R.string.training_loading))
            }
            TrainingStage.PAGE_ERROR -> {
                Text(stringResource(R.string.training_page_error))
                Button(onClick = { vm.act { retryPage() } }) {
                    Text(stringResource(R.string.training_retry))
                }
            }
            TrainingStage.RESTART_REQUIRED -> {
                Text(stringResource(R.string.training_restart_detail))
                Button(onClick = { vm.act { start() } }) {
                    Text(stringResource(R.string.training_restart))
                }
            }
            TrainingStage.UNRATED_CONFIRMATION -> {
                Text(
                    stringResource(R.string.training_unrated_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(stringResource(R.string.training_unrated_detail))
                Button(onClick = vm.session::acceptUnrated) {
                    Text(stringResource(R.string.training_practice))
                }
                OutlinedButton(onClick = onFinish) {
                    Text(stringResource(R.string.training_finish))
                }
            }
            TrainingStage.SUMMARY -> {
                Text(
                    stringResource(R.string.training_summary),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    pluralStringResource(
                        if (state.exhausted) R.plurals.training_exhausted
                        else R.plurals.training_summary_detail,
                        state.answered,
                        state.answered,
                    )
                )
                if (!state.exhausted)
                    Button(onClick = { vm.act { continueTraining() } }) {
                        Text(stringResource(R.string.training_continue))
                    }
                else
                    Button(onClick = { vm.act { start() } }) {
                        Text(stringResource(R.string.training_start))
                    }
                OutlinedButton(onClick = onFinish) {
                    Text(stringResource(R.string.training_finish))
                }
            }
            TrainingStage.ACCESS_LOST -> Text(stringResource(R.string.training_access))
            else -> {
                val current = state.current
                if (current != null) {
                    Text(
                        stringResource(
                            if (current.isRated) R.string.training_rated
                            else R.string.training_unrated_title
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (trainingQuestionSupported(current.quizQuestionWithSolutionDTO)) {
                        key(current.quizQuestionWithSolutionDTO.id, state.evaluation != null) {
                            QuizQuestionBody(
                                Modifier.fillMaxWidth(),
                                state.answered - (if (state.evaluation != null) 1 else 0),
                                state.evaluation?.let(::trainingResultData)
                                    ?: trainingEditableData(state, vm.session),
                            )
                        }
                        if (state.stage == TrainingStage.ANSWERING)
                            Button(
                                onClick = { vm.act { submit() } },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.training_submit))
                            }
                    } else {
                        Text(stringResource(R.string.training_unsupported))
                        OutlinedButton(onClick = { vm.act { skipQuestion() } }) {
                            Text(stringResource(R.string.training_skip))
                        }
                    }
                    if (state.stage == TrainingStage.SUBMITTING) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.training_submitting))
                    }
                    if (state.stage == TrainingStage.UNCERTAIN) {
                        Text(
                            stringResource(R.string.training_uncertain),
                            color = MaterialTheme.colorScheme.error,
                        )
                        Button(onClick = { vm.act { retrySubmission() } }) {
                            Text(stringResource(R.string.training_retry_submission))
                        }
                    }
                    if (state.stage == TrainingStage.EVALUATED) {
                        state.evaluation
                            ?.quizQuestion
                            ?.explanation
                            ?.takeIf { it.isNotBlank() }
                            ?.let { MarkdownText(markdown = it) }
                        Button(
                            onClick = { vm.act { next() } },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.training_next))
                        }
                    }
                    if (state.stage != TrainingStage.SUBMITTING)
                        TextButton(onClick = vm.session::finish) {
                            Text(stringResource(R.string.training_end_session))
                        }
                }
            }
        }
    }
}
