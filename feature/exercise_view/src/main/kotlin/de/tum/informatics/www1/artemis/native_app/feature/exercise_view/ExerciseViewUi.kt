package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
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
                    Text(
                        text = exerciseDataState.bind { it.title }.orElse(null)
                            ?: "Exercise name placeholder",
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
                    val DefaultTab = @Composable { index: Int, icon: ImageVector, textRes: Int ->
                        Tab(
                            selected = selectedTabIndex == index, onClick = { selectedTabIndex = index },
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
                    DefaultTab(1, Icons.Default.HelpCenter, R.string.exercise_view_tab_qna)
                }

                when (selectedTabIndex) {
                    0 -> {
                        ExerciseDetailsUi(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                ),
                            exercise = exercise,
                            latestResult = null,
                            hasMultipleResults = false
                        )
                    }
                }
            }
        }

    }
}