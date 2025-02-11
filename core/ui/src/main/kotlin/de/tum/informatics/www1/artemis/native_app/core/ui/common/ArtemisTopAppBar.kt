package de.tum.informatics.www1.artemis.native_app.core.ui.common

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ComponentColors

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
    )
) {
    Surface(
        shadowElevation = Spacings.AppBarElevation,
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

@Composable
fun ArtemisSearchTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    searchBarHint: String,
    query: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = ComponentColors.ArtemisTopAppBar.background,
    ),
    updateQuery: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ArtemisTopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSearchActive,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it }).togetherWith(
                            fadeOut(
                                tween(
                                    300
                                )
                            ) + slideOutVertically { -it })
                    }
                ) { searchActive ->
                    if (searchActive) {
                        BasicSearchTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .innerShadow(
                                    offset = 2.dp,
                                    color = ComponentColors.ArtemisTopAppBar.searchBarShadow
                                ),
                            backgroundColor = MaterialTheme.colorScheme.background,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            hint = searchBarHint,
                            query = query,
                            updateQuery = updateQuery,
                            focusRequester = focusRequester
                        )

                        LaunchedEffect(Unit) {
                            if (isSearchActive) focusRequester.requestFocus()
                        }

                    } else {
                        title()
                    }
                }
            },
            modifier = modifier,
            navigationIcon = if (isSearchActive) {
                { NavigationBackButton({ isSearchActive = false }) }
            } else navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior
        )

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .dropShadowBelow()
            ) {
                FakeBasicSearchTextField(
                    modifier = Modifier
                        .padding(16.dp)
                        .innerShadow(
                            offset = 2.dp,
                            color = ComponentColors.ArtemisTopAppBar.searchBarShadow
                        ),
                    hint = searchBarHint,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onClick = {
                        isSearchActive = true
                    }
                )
            }
        }
    }
}

private fun Modifier.dropShadowBelow(
    elevation: Dp = Spacings.AppBarElevation,
    color: Color = Color.Black.copy(0.15f)
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.maskFilter = (BlurMaskFilter(elevation.toPx(), BlurMaskFilter.Blur.NORMAL))

        frameworkPaint.color = color.toArgb()

        val rightPixel = size.width
        val bottomPixel = size.height

        canvas.drawRect(
            left = 0f,
            top = elevation.toPx(),
            right = rightPixel,
            bottom = bottomPixel,
            paint = paint,
        )
    }
}

// Inspired by the following Medium article:
// https://medium.com/@kappdev/inner-shadow-in-jetpack-compose-d80dcd56f6cf
private fun Modifier.innerShadow(
    offset: Dp = 2.dp,
    color: Color = Color.Black.copy(0.5f)
) = this.drawWithContent {
    drawContent()
    drawIntoCanvas { canvas ->
        val shadowSize = Size(size.width + offset.toPx(), size.height + offset.toPx())
        val shadowOutline =
            RoundedCornerShape(12.dp).createOutline(shadowSize, layoutDirection, this)

        val paint = Paint()
        paint.color = color

        canvas.saveLayer(size.toRect(), paint)
        canvas.drawOutline(shadowOutline, paint)

        paint.asFrameworkPaint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (4.dp.toPx() > 0) {
                maskFilter = BlurMaskFilter(4.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
        }

        paint.color = Color.Black
        canvas.translate(offset.toPx(), offset.toPx())
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}
