package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.isSuccess
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun CourseUi(
    modifier: Modifier,
    viewModel: CourseViewModel,
    onNavigateBack: () -> Unit
) {
    val courseDataState by viewModel.course.collectAsState(initial = DataState.Loading())
    val weeklyExercises by viewModel.exercisesGroupedByWeek.collectAsState(initial = DataState.Loading())

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.placeholder(visible = !courseDataState.isSuccess),
                            text = courseDataState.bind { it.title }
                                .orElse("Placeholder course title")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
                var selectedTabIndex by remember { mutableStateOf(0) }

                ScrollableTabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = selectedTabIndex
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(text = stringResource(id = R.string.course_ui_tab_exercises)) },
                        icon = { Icon(Icons.Default.ListAlt, contentDescription = null) }
                    )
                }
            }


        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = courseDataState,
            loadingText = stringResource(id = R.string.course_ui_loading_course_loading),
            failureText = stringResource(id = R.string.course_ui_loading_course_failed),
            suspendedText = stringResource(id = R.string.course_ui_loading_course_suspended),
            retryButtonText = stringResource(id = R.string.course_ui_loading_course_try_again),
            onClickRetry = { viewModel.reloadCourse() }
        ) { _ ->
            ExerciseListUi(
                modifier = Modifier.fillMaxSize(),
                exercisesDataState = weeklyExercises,
                onClickExercise = { exerciseId ->

                },
                loadExerciseDetails = viewModel::getLoadExerciseDetailsFlow
            )
        }
    }
}