package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.web.rememberWebViewState
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SideBarMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post.navigateToCreateStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview.ExerciseOverviewTab
import io.ktor.http.*

// Ratio of metis content in contrast to the actual exercise content
const val METIS_RATIO = 0.3f

@Composable
internal fun ExerciseScreenBody(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    displayCommunicationOnSide: Boolean,
    serverUrl: String,
    gradedParticipation: Participation?,
    authToken: String,
    navController: NavController,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, isPractice: Boolean) -> Unit,
    onViewResult: () -> Unit,
    onClickRetry: () -> Unit,
    onClickStartExercise: () -> Unit
) {
    ExerciseDataStateUi(
        modifier = modifier,
        onClickRetry = onClickRetry,
        value = exerciseDataState,
        onSuccess = { exercise ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val courseId: Long = exercise.course?.id ?: return@Column

                // Maybe add a replacement ui
                val exerciseId = exercise.id

                val metisContext = remember(courseId, exerciseId) {
                    MetisContext.Exercise(courseId = courseId, exerciseId)
                }

                val url = remember(serverUrl, courseId, exercise.id) {
                    URLBuilder(serverUrl).apply {
                        appendPathSegments(
                            "courses",
                            courseId.toString(),
                            "exercises",
                            exercise.id.toString()
                        )
                    }
                        .buildString()
                }

                val webViewState = rememberWebViewState(url = url)

                // Retain web view instance to avoid reloading when switching between tabs
                var savedWebView: WebView? by remember { mutableStateOf(null) }

                val exerciseOverviewTab = remember {
                    movableContentOf { modifier: Modifier ->
                        ExerciseOverviewTab(
                            modifier = modifier,
                            authToken = authToken,
                            exercise = exercise,
                            gradedParticipation = gradedParticipation,
                            onClickStartExercise = onClickStartExercise,
                            onClickOpenTextExercise = onViewTextExerciseParticipationScreen,
                            onClickPracticeQuiz = {
                                onParticipateInQuiz(courseId, true)
                            },
                            onClickStartQuiz = {
                                onParticipateInQuiz(courseId, false)
                            },
                            onClickOpenQuiz = {
                                onParticipateInQuiz(courseId, false)
                            },
                            onViewResult = onViewResult,
                            webViewState = webViewState,
                            setWebView = { savedWebView = it },
                            webView = savedWebView
                        )
                    }
                }

                if (displayCommunicationOnSide) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        exerciseOverviewTab(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f - METIS_RATIO)
                                .verticalScroll(rememberScrollState())
                        )

                        SideBarMetisUi(
                            modifier = Modifier
                                .weight(METIS_RATIO)
                                .fillMaxHeight(),
                            metisContext = metisContext,
                            navController = navController,
                            title = {
                                Text(
                                    text = stringResource(id = R.string.exercise_view_tab_qna),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                    }
                } else {
                    BodyWithTabs(
                        modifier = Modifier.fillMaxSize(),
                        metisContext = metisContext,
                        navController = navController
                    ) {
                        exerciseOverviewTab(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    )
}

/**
 * Displays a tab row and shows the selected tab body.
 */
@Composable
private fun BodyWithTabs(
    modifier: Modifier,
    metisContext: MetisContext.Exercise,
    navController: NavController,
    exerciseOverviewTab: @Composable () -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    val onUpdateSelectedTabIndex = { new: Int -> selectedTabIndex = new }

    Column(modifier = modifier) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex
        ) {
            DefaultTab(
                0,
                Icons.Default.ViewHeadline,
                R.string.exercise_view_tab_overview,
                selectedTabIndex,
                onUpdateSelectedTabIndex
            )

            DefaultTab(
                1,
                Icons.Default.HelpCenter,
                R.string.exercise_view_tab_qna,
                selectedTabIndex,
                onUpdateSelectedTabIndex
            )
        }

        when (selectedTabIndex) {
            0 -> {
                exerciseOverviewTab()
            }

            1 -> {
                val fabSettings = LocalExerciseScreenFloatingActionButton.current

                // Enable and disable disable floating action button
                DisposableEffect(Unit) {
                    fabSettings.settings.value = FloatingActionButtonSettings(
                        icon = Icons.Default.Create,
                        contentDescription = null,
                        onClick = {
                            navController.navigateToCreateStandalonePostScreen(metisContext) {}
                        }
                    )

                    onDispose {
                        fabSettings.settings.value = null
                    }
                }

                SmartphoneMetisUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    metisContext = metisContext,
                    navController = navController,
                    displayFab = false // The FAB is displayed by the exercise screen itself.
                )
            }
        }
    }
}