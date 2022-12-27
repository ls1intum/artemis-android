package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.OverviewTab
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToLecture(
    lectureId: Long,
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("lecture/$lectureId/$courseId", builder)
}

fun NavGraphBuilder.lecture(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "lecture/{lectureId}/{courseId}",
        arguments = listOf(
            navArgument("lectureId") {
                type = NavType.LongType
                nullable = false
            },
            navArgument("courseId") {
                type = NavType.LongType
                nullable = false
            }
        )
    ) { backStackEntry ->
        val lectureId =
            backStackEntry.arguments?.getLong("lectureId")
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(lectureId)
        checkNotNull(courseId)

        val viewModel: LectureViewModel = koinViewModel { parametersOf(lectureId) }
        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            lectureId = lectureId,
            viewModel = viewModel,
            navController = navController,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun LectureScreen(
    modifier: Modifier,
    courseId: Long,
    lectureId: Long,
    viewModel: LectureViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    val lectureDataState by viewModel.lectureDataState.collectAsState()

    val lectureTitle = lectureDataState.bind<String?> { it.title }.orElse(null)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lectureTitle ?: "Placeholder",
                        modifier = Modifier.placeholder(lectureTitle == null)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        val selectedTabIndexState = rememberSaveable {
            mutableStateOf(0)
        }
        var selectedTabIndex by selectedTabIndexState

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                DefaultTab(
                    index = 0,
                    icon = Icons.Default.ViewHeadline,
                    textRes = R.string.lecture_view_tab_overview,
                    selectedTabIndex = selectedTabIndexState
                )

                DefaultTab(
                    index = 1,
                    icon = Icons.Default.HelpCenter,
                    textRes = R.string.lecture_view_tab_communication,
                    selectedTabIndex = selectedTabIndexState
                )

                DefaultTab(
                    index = 2,
                    icon = Icons.Default.Attachment,
                    textRes = R.string.lecture_view_tab_attachments,
                    selectedTabIndex = selectedTabIndexState
                )
            }

            BasicDataStateUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                dataState = lectureDataState,
                loadingText = stringResource(id = R.string.lecture_view_lecture_loading),
                failureText = stringResource(id = R.string.lecture_view_lecture_loading_failure),
                suspendedText = stringResource(id = R.string.lecture_view_lecture_loading_suspended),
                retryButtonText = stringResource(id = R.string.lecture_view_lecture_loading_try_again),
                onClickRetry = viewModel::requestReloadLecture
            ) { lecture ->
                when (selectedTabIndex) {
                    0 -> {
                        OverviewTab(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            description = lecture.description
                        )
                    }

                    1 -> {
                        val metisContext = remember {
                            MetisContext.Lecture(courseId = courseId, lectureId = lectureId)
                        }

                        SmartphoneMetisUi(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            metisContext = metisContext,
                            navController = navController
                        )
                    }

                    2 -> {
                        AttachmentsTab(
                            modifier = Modifier.fillMaxWidth(),
                            attachments = lecture.attachments
                        )
                    }
                }
            }
        }
    }
}