package de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletLandscape
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait


@Composable
fun LayoutAwareTwoColumnLayout(
    modifier: Modifier = Modifier,
    isSidebarOpen: Boolean,
    onSidebarToggle: () -> Unit,
    optionalColumn: @Composable (Modifier) -> Unit,
    priorityColumn: @Composable (Modifier) -> Unit
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

                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
                    exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .zIndex(2f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        optionalColumn(Modifier.fillMaxSize())
                    }
                }
            }
        }

        isTabletLandscape -> {
            Row(
                modifier = modifier.fillMaxSize(),
            ) {
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally { fullWidth -> -fullWidth } + fadeIn(),
                    exit = slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .zIndex(2f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        optionalColumn(Modifier.fillMaxSize())
                    }
                }

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

