package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSearchTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import io.github.fornewid.placeholder.material3.placeholder

@Composable
internal fun CourseScaffold(
    modifier: Modifier = Modifier,
    courseDataState: DataState<Course>,
    isCourseTabSelected: (CourseTab) -> Boolean,
    updateSelectedCourseTab: (CourseTab) -> Unit,
    onNavigateBack: () -> Unit,
    onReloadCourse: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CourseTopAppBar(
                courseDataState = courseDataState,
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            BottomNavigationBar(
                isSelected = isCourseTabSelected,
                onUpdateSelectedTab = updateSelectedCourseTab
            )
        }
    ) { padding ->
        val animatedPadding by animateDpAsState(
            targetValue = padding.calculateTopPadding(),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "Padding Animation"
        )

        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding(),
                    top = animatedPadding
                )
                .consumeWindowInsets(padding)
                .systemBarsPadding(),           // This line is needed due to https://stackoverflow.com/a/74545344/13366254
            dataState = courseDataState,
            loadingText = stringResource(id = R.string.course_ui_loading_course_loading),
            failureText = stringResource(id = R.string.course_ui_loading_course_failed),
            retryButtonText = stringResource(id = R.string.course_ui_loading_course_try_again),
            onClickRetry = onReloadCourse
        ) {
            content()
        }
    }
}


@Composable
private fun CourseTopAppBar(
    courseDataState: DataState<Course>,
    onNavigateBack: () -> Unit
) {
    val courseTitle = courseDataState.bind<String?> { it.title }.orElse(null)

    ArtemisSearchTopAppBar(
        title = {
            Text(
                modifier = Modifier.placeholder(visible = !courseDataState.isSuccess),
                text = courseTitle.orEmpty(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        searchBarHint = stringResource(id = R.string.course_ui_tab_lectures),
        navigationIcon = { NavigationBackButton(onNavigateBack) }
    )
}


@Composable
private fun BottomNavigationBar(
    isSelected: (CourseTab) -> Boolean,
    onUpdateSelectedTab: (CourseTab) -> Unit
) {
    Surface(
        shadowElevation = Spacings.AppBarElevation
    ){
        NavigationBar {
            BottomNavigationItem.topLevelRoutes.forEach { navigationItem ->

                val labelText = stringResource(id = navigationItem.labelStringId)
                NavigationBarItem(
                    selected = isSelected(navigationItem.route),
                    label = {
                        Text(labelText)
                    },
                    icon = {
                        Icon(
                            navigationItem.icon,
                            contentDescription = labelText
                        )
                    },
                    onClick = {
                        onUpdateSelectedTab(navigationItem.route)
                    }
                )
            }
        }
    }
}


private data class BottomNavigationItem(
    val labelStringId: Int,
    val icon: ImageVector = Icons.Filled.Home,
    val route: CourseTab,
) {
    companion object {
        val topLevelRoutes: List<BottomNavigationItem> = listOf(
            BottomNavigationItem(
                labelStringId =R.string.course_ui_tab_exercises,
                icon = Icons.AutoMirrored.Filled.ListAlt,
                route = CourseTab.Exercises
            ),
            BottomNavigationItem(
                labelStringId = R.string.course_ui_tab_lectures,
                icon = Icons.Default.School,
                route = CourseTab.Lectures
            ),
            BottomNavigationItem(
                labelStringId = R.string.course_ui_tab_communication,
                icon = Icons.AutoMirrored.Filled.Chat,
                route = CourseTab.Communication
            ),
        )
    }
}

