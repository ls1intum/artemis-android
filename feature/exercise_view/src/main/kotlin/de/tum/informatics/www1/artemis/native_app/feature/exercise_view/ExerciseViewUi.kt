package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.tabs.details.ExerciseDetailsTab
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.tabs.overview.ExerciseOverviewTab
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToExercise(exerciseId: Int, builder: NavOptionsBuilder.() -> Unit) {
    navigate("exercise/$exerciseId", builder)
}

fun NavGraphBuilder.exercise(onNavigateBack: () -> Unit) {
    composable(
        route = "exercise/{exerciseId}",
        arguments = listOf(navArgument("exerciseId") {
            type = NavType.IntType
            nullable = false
        }
        )
    ) { backStackEntry ->
        val exerciseId =
            backStackEntry.arguments?.getInt("exerciseId")
        checkNotNull(exerciseId)

        ExerciseScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(exerciseId) },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
internal fun ExerciseScreen(
    modifier: Modifier,
    viewModel: ExerciseViewModel,
    onNavigateBack: () -> Unit
) {
    val exerciseDataState = viewModel.exercise.collectAsState(initial = DataState.Loading()).value

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            MediumTopAppBar(
                title = {
                    val fontSize = LocalTextStyle.current.fontSize

                    val (titleText, inlineContent) = remember(exerciseDataState) {
                        val text = buildAnnotatedString {
                            appendInlineContent("icon")
                            append(" ")
                            append(
                                exerciseDataState.bind { it.title }.orElse(null)
                                    ?: "Exercise name placeholder"
                            )
                        }

                        val inlineContent = mapOf(
                            "icon" to InlineTextContent(
                                Placeholder(fontSize, fontSize, PlaceholderVerticalAlign.TextCenter)
                            ) {
                                Icon(
                                    imageVector = exerciseDataState.bind { getExerciseTypeIcon(it) }
                                        .orElse(Icons.Default.Downloading),
                                    contentDescription = null
                                )
                            }
                        )

                        text to inlineContent
                    }

                    Text(
                        text = titleText,
                        inlineContent = inlineContent,
                        modifier = Modifier.placeholder(exerciseDataState !is DataState.Success)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = exerciseDataState,
            loadingText = stringResource(id = R.string.exercise_view_loading),
            failureText = stringResource(id = R.string.exercise_view_failure),
            suspendedText = stringResource(id = R.string.exercise_view_suspended),
            retryButtonText = stringResource(id = R.string.exercise_view_try_again),
            onClickRetry = { viewModel.requestReloadExercise() }) { exercise ->

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                var selectedTabIndex by remember { mutableStateOf(0) }

                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedTabIndex
                ) {
                    @Suppress("LocalVariableName")
                    val DefaultTab = @Composable { index: Int, icon: ImageVector, textRes: Int ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = stringResource(id = textRes))
                            }
                        )
                    }

                    DefaultTab(0, Icons.Default.ViewHeadline, R.string.exercise_view_tab_overview)
                    DefaultTab(1, Icons.Default.Info, R.string.exercise_view_tab_info)
                    DefaultTab(2, Icons.Default.HelpCenter, R.string.exercise_view_tab_qna)
                }

                val tabModifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )

                when (selectedTabIndex) {
                    0 -> {
                        ExerciseOverviewTab(modifier = tabModifier, exercise = exercise)
                    }
                    1 -> {
                        ExerciseDetailsTab(
                            modifier = tabModifier,
                            exercise = exercise,
                            latestResult = null
                        )
                    }
                }
            }
        }

    }
}