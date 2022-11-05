package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.isSuccess

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
                    @Suppress("LocalVariableName")
                    val CourseTab = @Composable { index: Int, text: String, icon: ImageVector ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = text) },
                            icon = { Icon(icon, contentDescription = null) }
                        )
                    }

                    CourseTab(0, stringResource(id = R.string.course_ui_tab_exercises), Icons.Default.ListAlt)
                    CourseTab(1, stringResource(id = R.string.course_ui_tab_lectures), Icons.Default.School)
                    CourseTab(2, stringResource(id = R.string.course_ui_tab_communication), Icons.Default.Chat)
                    CourseTab(3, stringResource(id = R.string.course_ui_tab_other), Icons.Default.MoreHoriz)

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
                exercisesDataState = weeklyExercises
            ) { exerciseId ->

            }
        }
    }
}