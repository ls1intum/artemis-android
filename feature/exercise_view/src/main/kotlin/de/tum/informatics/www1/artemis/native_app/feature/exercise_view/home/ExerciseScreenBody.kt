package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home

import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.overview.ExerciseOverviewTab
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext

// Ratio of metis content in contrast to the actual exercise content
const val METIS_RATIO = 0.3f

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun ExerciseScreenBody(
    modifier: Modifier,
    exerciseDataState: DataState<Exercise>,
    displayCommunicationOnSide: Boolean,
    navController: NavController,
    metisContext: MetisContext?,
    serverUrl: String,
    authToken: String,
    actions: ExerciseActions,
    webViewState: WebViewState?,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    onClickRetry: () -> Unit
) {
    ExerciseDataStateUi(
        modifier = modifier,
        onClickRetry = onClickRetry,
        value = exerciseDataState,
        onSuccess = { exercise ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val exerciseOverviewTab: @Composable (Modifier) -> Unit = { modifier: Modifier ->
                    ExerciseOverviewTab(
                        modifier = modifier,
                        exercise = exercise,
                        webViewState = webViewState,
                        setWebView = setWebView,
                        webView = webView,
                        actions = actions,
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                }

                exerciseOverviewTab(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                )

                // Commented out as we may need that code again once we display communications for exercises

//                BodyWithTabs(
//                    modifier = Modifier.fillMaxSize(),
//                    metisContext = metisContext,
//                    navController = navController
//                ) {
//                    exerciseOverviewTab(
//                        Modifier
//                            .fillMaxSize()
//                            .verticalScroll(rememberScrollState())
//                            .padding(horizontal = 8.dp)
//                    )
//                }

//                if (displayCommunicationOnSide) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        exerciseOverviewTab(
//                            Modifier
//                                .fillMaxHeight()
//                                .weight(1f - METIS_RATIO)
//                                .verticalScroll(rememberScrollState())
//                        )
//
//                        val sideBarMetisModifier = Modifier
//                            .weight(METIS_RATIO)
//                            .fillMaxHeight()
//
//                        if (metisContext != null) {
//                            SideBarMetisUi(
//                                modifier = sideBarMetisModifier,
//                                metisContext = metisContext,
//                                navController = navController,
//                                title = {
//                                    Text(
//                                        text = stringResource(id = R.string.exercise_view_tab_qna),
//                                    )
//                                }
//                            )
//                        } else {
//                            // Placeholder
//                            Box(modifier = sideBarMetisModifier)
//                        }
//                    }
//                } else {
//                    BodyWithTabs(
//                        modifier = Modifier.fillMaxSize(),
//                        metisContext = metisContext,
//                        navController = navController
//                    ) {
//                        exerciseOverviewTab(
//                            Modifier
//                                .fillMaxSize()
//                                .verticalScroll(rememberScrollState())
//                                .padding(horizontal = 8.dp)
//                        )
//                    }
//                }
            }
        }
    )
}

/**
 * Displays a tab row and shows the selected tab body.
 */
@Suppress("UNUSED_PARAMETER")
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
                        }
                    )

                    onDispose {
                        fabSettings.settings.value = null
                    }
                }

//                if (metisContext != null) {
//                    SmartphoneConversationUi(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 8.dp),
//                        metisContext = metisContext,
//                        navController = navController
//                        // The FAB is displayed by the exercise screen itself.
//                    )
//                }
            }
        }
    }
}