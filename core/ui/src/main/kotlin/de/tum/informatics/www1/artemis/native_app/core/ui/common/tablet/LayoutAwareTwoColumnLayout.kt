package de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FakeBasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.TEST_TAG_FAQ_ARTEMIS_TOP_APP_BAR_FAKE_SEARCH
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.innerShadow
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletLandscape
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

@Composable
fun LayoutAwareTwoColumnLayout(
    modifier: Modifier = Modifier,
    isSidebarOpen: Boolean,
    onSidebarToggle: () -> Unit,
    optionalColumn: @Composable (Modifier) -> Unit,
    priorityColumn: @Composable (Modifier) -> Unit,
    title: String,
    searchConfiguration: CourseSearchConfiguration = CourseSearchConfiguration.DisabledSearch,
) {
    val layout = getArtemisAppLayout()
    val (isTabletLandscape, isTabletPortrait) = layout.let { it.isTabletLandscape to it.isTabletPortrait }

    // Automatically open sidebar in landscape mode
    LaunchedEffect(isTabletLandscape) {
        if (isTabletLandscape && !isSidebarOpen) {
            onSidebarToggle()
        }
    }

    when {
        isTabletPortrait -> {
            Box(modifier = modifier.fillMaxSize()) {
                priorityColumn(Modifier.fillMaxSize())

                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { onSidebarToggle() }
                            .zIndex(1f)
                    )
                }

                Sidebar(
                    title = title,
                    optionalColumn = optionalColumn,
                    isSidebarOpen = isSidebarOpen,
                    searchConfiguration = searchConfiguration
                )
            }
        }

        isTabletLandscape -> {
            Row(
                modifier = modifier.fillMaxSize(),
            ) {
                Sidebar(
                    title = title,
                    optionalColumn = optionalColumn,
                    isSidebarOpen = isSidebarOpen,
                    searchConfiguration = searchConfiguration
                )

                VerticalDivider()

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    priorityColumn(Modifier.fillMaxSize())
                }
            }
        }

        else -> {
            // Phone
            Box(modifier = modifier) {
                priorityColumn(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun Sidebar(
    title: String,
    optionalColumn: @Composable (Modifier) -> Unit,
    isSidebarOpen: Boolean,
    searchConfiguration: CourseSearchConfiguration,
) {
    AnimatedVisibility(
        visible = isSidebarOpen,
        enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
        exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(),
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .fillMaxHeight()
                .zIndex(2f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SidebarHeader(
                title = title,
                searchConfiguration = searchConfiguration,
            )

            optionalColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun SidebarHeader(
    title: String,
    searchConfiguration: CourseSearchConfiguration,
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val closeSearch = {
        isSearchActive = false
        if (searchConfiguration is CourseSearchConfiguration.Search) {
            searchConfiguration.onUpdateQuery("")
        }
    }

    BackHandler(isSearchActive, closeSearch)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title.takeIf { it.isNotEmpty() }?.let { nonEmptyTitle ->
            Text(
                text = nonEmptyTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Show the search bar below the title
        Spacer(modifier = Modifier.height(8.dp))

        if (searchConfiguration is CourseSearchConfiguration.Search) {
            if (isSearchActive) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                BasicSearchTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    hint = searchConfiguration.hint,
                    query = searchConfiguration.query,
                    updateQuery = searchConfiguration.onUpdateQuery,
                    focusRequester = focusRequester
                )
            } else {
                FakeBasicSearchTextField(
                    modifier = Modifier
                        .testTag(TEST_TAG_FAQ_ARTEMIS_TOP_APP_BAR_FAKE_SEARCH)
                        .fillMaxWidth(),
                    hint = searchConfiguration.hint,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = { isSearchActive = it }
                )
            }
        }
    }
}
