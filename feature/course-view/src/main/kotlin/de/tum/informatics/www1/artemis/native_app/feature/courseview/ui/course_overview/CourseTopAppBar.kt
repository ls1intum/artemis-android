package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import io.github.fornewid.placeholder.material3.placeholder


data class BottomNavigationItem(
    val label: String,
    val icon: ImageVector = Icons.Filled.Home,
//    val route: Serializable,
) {
    companion object {
        @Composable
        fun bottomNavigationItems(): List<BottomNavigationItem> {
            return listOf(
                BottomNavigationItem(
                    label = stringResource(id = R.string.course_ui_tab_exercises),
                    icon = Icons.Default.ListAlt,
                ),
                BottomNavigationItem(
                    label = stringResource(id = R.string.course_ui_tab_lectures),
                    icon = Icons.Default.School,
                ),
                BottomNavigationItem(
                    label = stringResource(id = R.string.course_ui_tab_communication),
                    icon = Icons.Default.Chat,
                ),
            )
        }
    }
}


@Composable
fun BottomNavigationBar(
    selectedTabIndex: Int,
    changeTab: (Int) -> Unit
) {
    NavigationBar {
        BottomNavigationItem.bottomNavigationItems().forEachIndexed {index,navigationItem ->

            NavigationBarItem(
                selected = index == selectedTabIndex,
                label = {
                    Text(navigationItem.label)
                },
                icon = {
                    Icon(
                        navigationItem.icon,
                        contentDescription = navigationItem.label
                    )
                },
                onClick = {
                    changeTab(index)
//                    navController.navigate(navigationItem.route) {
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
                }
            )
        }
    }
}

@Composable
internal fun CourseTopAppBar(
    courseDataState: DataState<Course>,
    scrollBehavior: TopAppBarScrollBehavior,
    onReloadCourse: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    modifier = Modifier.placeholder(visible = !courseDataState.isSuccess),
                    text = courseDataState.bind { it.title }
                        .orElse("Placeholder course title"),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onReloadCourse) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
}
