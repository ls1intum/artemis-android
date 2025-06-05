package de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FakeBasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

private const val animatingDuration = 300
const val TEST_TAG_FAQ_ARTEMIS_TOP_APP_BAR_FAKE_SEARCH = "ARTEMIS_TOP_APP_BAR_FAKE_SEARCH"

/**
 * A top app bar only featuring a title and a navigation icon using the style of the bottom app bar to
 * maintain a consistent look and feel.
 */
@Composable
fun ArtemisTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = ComponentColors.ArtemisTopAppBar.background,
    ),
    isElevated: Boolean = true
) {
    Surface(
        shadowElevation = if (isElevated) Spacings.AppBarElevation else 0.dp,
    ) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}

/**
 * An extension of the [ArtemisTopAppBar] that includes a search bar and provides smooth search transitions.
 */
@Composable
fun ArtemisSearchTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    searchBarHint: String,
    query: String,
    lineCount: Int = 1,
    searchBarTestTag: String? = null,
    collapsingContentState: CollapsingContentState,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = ComponentColors.ArtemisTopAppBar.background,
    ),
    updateQuery: (String) -> Unit,
    notificationIcon: @Composable () -> Unit = {}
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val closeSearch = {
        isSearchActive = false
        updateQuery("")
    }

    BackHandler(isSearchActive, closeSearch)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ArtemisTopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSearchActive,
                    transitionSpec = {
                        val titleEnter = fadeIn(tween(animatingDuration)) + slideInVertically { it }
                        val titleExit = fadeOut(tween(animatingDuration)) + slideOutVertically { -it }
                        titleEnter.togetherWith(titleExit)
                    }
                ) { searchActive ->
                    if (!searchActive) {
                        title()
                        notificationIcon()
                        return@AnimatedContent
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    BasicSearchTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        hint = searchBarHint,
                        query = query,
                        testTag = searchBarTestTag,
                        updateQuery = updateQuery,
                        focusRequester = focusRequester
                    )
                }
            },
            modifier = modifier,
            navigationIcon = if (isSearchActive) {
                { NavigationBackButton(closeSearch) }
            } else navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior
        )

        val visibilityCondition =
            if (collapsingContentState.isCollapsingEnabled) !isSearchActive && !collapsingContentState.isCollapsed else !isSearchActive

        CollapsingSurface(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (visibilityCondition) Modifier.dropShadowBelow() else Modifier),
            lineCount = lineCount,
            searchBarHint = searchBarHint,
            visibilityCondition = visibilityCondition,
            collapsingContentState = collapsingContentState,
            onClick = { isSearchActive = it }
        )
    }
}

@Composable
private fun CollapsingSurface(
    modifier: Modifier,
    lineCount: Int,
    searchBarHint: String,
    visibilityCondition: Boolean,
    collapsingContentState: CollapsingContentState,
    onClick: (Boolean) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = visibilityCondition,
        transitionSpec = {
            val searchEnter = fadeIn(tween(animatingDuration)) + slideInVertically { -it }
            val searchExit = fadeOut(tween(animatingDuration)) + slideOutVertically { -it }

            if (targetState) {
                searchEnter.togetherWith(fadeOut(tween(animatingDuration)))
            } else {
                fadeIn(tween(animatingDuration)).togetherWith(searchExit)
            }
        },
    ) { isVisible ->
        if (!isVisible) {
            Spacer(Modifier.fillMaxWidth())
            return@AnimatedContent
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    collapsingContentState.collapsingHeight = it.height.toFloat()
                },
            color = MaterialTheme.colorScheme.surface,
        ) {
            FakeBasicSearchTextField(
                modifier = Modifier
                    .testTag(TEST_TAG_FAQ_ARTEMIS_TOP_APP_BAR_FAKE_SEARCH)
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                    .padding(bottom = 16.dp)
                    .then(if (lineCount > 1) Modifier.padding(top = 16.dp) else Modifier),
                hint = searchBarHint,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                onClick = onClick
            )
        }
    }
}