package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.web.WebViewState
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

// Ratio of metis content in contrast to the actual exercise content
const val METIS_RATIO = 0.3f

@Composable
internal fun ExerciseScreenBody(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    displayCommunicationOnSide: Boolean,
    gradedParticipation: Participation?,
    authToken: String,
    navController: NavController,
    metisContext: MetisContext?,
    onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit,
    onParticipateInQuiz: (isPractice: Boolean) -> Unit,
    onViewResult: () -> Unit,
    onClickRetry: () -> Unit,
    onClickStartExercise: () -> Unit,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?
) {
    ExerciseDataStateUi(
        modifier = modifier,
        onClickRetry = onClickRetry,
        value = exerciseDataState,
        onSuccess = { exercise ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
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
                                onParticipateInQuiz(true)
                            },
                            onClickStartQuiz = {
                                onParticipateInQuiz(false)
                            },
                            onClickOpenQuiz = {
                                onParticipateInQuiz(false)
                            },
                            onViewResult = onViewResult,
                            webViewState = webViewState,
                            setWebView = setWebView,
                            webView = webView
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

                        val sideBarMetisModifier = Modifier
                            .weight(METIS_RATIO)
                            .fillMaxHeight()

                        if (metisContext != null) {
                            SideBarMetisUi(
                                modifier = sideBarMetisModifier,
                                metisContext = metisContext,
                                navController = navController,
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.exercise_view_tab_qna),
                                    )
                                }
                            )
                        } else {
                            // Placeholder
                            Box(modifier = sideBarMetisModifier)
                        }
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
    metisContext: MetisContext?,
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
                            if (metisContext != null) {
                                navController.navigateToCreateStandalonePostScreen(metisContext) {}
                            }
                        }
                    )

                    onDispose {
                        fabSettings.settings.value = null
                    }
                }

                if (metisContext != null) {
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
}