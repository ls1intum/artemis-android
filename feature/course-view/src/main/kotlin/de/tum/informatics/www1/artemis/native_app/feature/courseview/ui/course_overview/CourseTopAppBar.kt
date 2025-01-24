package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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


internal data class BottomNavigationItem(
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


@Composable
internal fun BottomNavigationBar(
    isSelected: (CourseTab) -> Boolean,
    onUpdateSelectedTab: (CourseTab) -> Unit
) {
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


@Composable
internal fun CourseTopAppBar(
    courseDataState: DataState<Course>,
    onNavigateBack: () -> Unit
) {
    val courseTitle = courseDataState.bind<String?> { it.title }.orElse(null)

    Column {
        TopAppBar(
            title = {
                Text(
                    modifier = Modifier.placeholder(visible = !courseDataState.isSuccess),
                    text = courseTitle.orEmpty(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )
    }
}
