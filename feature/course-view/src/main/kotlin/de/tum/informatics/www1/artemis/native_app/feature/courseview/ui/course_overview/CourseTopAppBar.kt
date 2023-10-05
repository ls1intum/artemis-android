package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R

@Composable
internal fun CourseTopAppBar(
    selectedTabIndex: Int,
    courseDataState: DataState<Course>,
    scrollBehavior: TopAppBarScrollBehavior,
    changeTab: (Int) -> Unit,
    onReloadCourse: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    modifier = Modifier.placeholder(visible = courseDataState !is DataState.Success),
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
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex
        ) {
            @Suppress("LocalVariableName")
            val CourseTab = @Composable { index: Int, text: String, icon: ImageVector ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { changeTab(index) },
                    text = {
                        Text(
                            text = text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = { Icon(icon, contentDescription = null) }
                )
            }

            CourseTab(
                0,
                stringResource(id = R.string.course_ui_tab_exercises),
                Icons.Default.ListAlt
            )
            CourseTab(
                1,
                stringResource(id = R.string.course_ui_tab_lectures),
                Icons.Default.School
            )
            CourseTab(
                2,
                stringResource(id = R.string.course_ui_tab_communication),
                Icons.Default.Chat
            )
        }
    }
}
