package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NotificationButton

@Composable
fun CourseTabletNavigation(
    modifier: Modifier = Modifier,
    courseDataState: DataState<Course>,
    isSelected: (CourseTab) -> Boolean,
    onUpdateSelectedTab: (CourseTab) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNotificationSection: () -> Unit,
) {
    val navItems = getNavigationItems(courseDataState)
    val selectedIndex = navItems.indexOfFirst { isSelected(it.route) }.coerceAtLeast(0)

    ArtemisTopAppBar(
        modifier = modifier,
        navigationIcon = {
            NavigationBackButton(onNavigateBack)
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer( modifier = Modifier.weight(1f) )

                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val selected = index == selectedIndex
                        val label = stringResource(id = item.labelStringId)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.background  else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clickable { onUpdateSelectedTab(item.route) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer( modifier = Modifier.weight(1f) )

                NotificationButton { onNavigateNotificationSection() }
            }
        }
    )
}



