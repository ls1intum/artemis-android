package de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.PdfImage
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.HorizontalPdfReaderState
import de.tum.informatics.www1.artemis.native_app.core.ui.pdf.render.state.VerticalPdfReaderState
import kotlinx.coroutines.launch

@Composable
fun VerticalPdfView(
    state: VerticalPdfReaderState,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val ctx = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val lazyState = state.lazyState
        DisposableEffect(key1 = Unit) {
            PdfFile().load(
                coroutineScope,
                ctx,
                state,
                constraints.maxWidth,
                constraints.maxHeight,
                authToken = state.authToken,
                true
            )
            onDispose {
                state.close()
            }
        }
        state.pdfRender?.let { pdf ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .tapToZoomVertical(state, constraints),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = lazyState
            ) {
                items(pdf.pageCount) {
                    val pageContent = pdf.pageLists[it].stateFlow.collectAsState().value
                    DisposableEffect(key1 = Unit) {
                        pdf.pageLists[it].load()
                        onDispose {
                            // This caused issues and has therefore been temporarily disabled
                            //pdf.pageLists[it].recycle()
                        }
                    }
                    when (pageContent) {
                        is PageContentInt.PageContent -> {
                            PdfImage(
                                bitmap = { pageContent.bitmap.asImageBitmap() }
                            )
                        }

                        is PageContentInt.BlankPage -> BlackPage(
                            width = pageContent.width,
                            height = pageContent.height
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalPdfView(
    state: HorizontalPdfReaderState,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val ctx = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(key1 = Unit) {
            PdfFile().load(
                coroutineScope,
                ctx,
                state,
                constraints.maxWidth,
                constraints.maxHeight,
                authToken = state.authToken,
                constraints.maxHeight > constraints.maxWidth
            )
            onDispose {
                state.close()
            }
        }
        val pagerState = rememberPagerState( state.currentPage - 1 ) { state.pdfPageCount }

        state.pdfRender?.let { pdf ->
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .tapToZoomHorizontal(state, constraints),
                state = pagerState,
                userScrollEnabled = state.scale == 1f
            ) { page ->
                val pageContent = pdf.pageLists[page].stateFlow.collectAsState().value

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        state.currentPage = page + 1
                    }
                    snapshotFlow { pagerState.isScrollInProgress }.collect { isScrolling ->
                        state.isScrolling = isScrolling
                    }
                }

                DisposableEffect(key1 = Unit) {
                    pdf.pageLists[page].load()
                    onDispose {
                        pdf.pageLists[page].recycle()
                    }
                }
                when (pageContent) {
                    is PageContentInt.PageContent -> {
                        PdfImage(
                            bitmap = { pageContent.bitmap.asImageBitmap() }
                        )
                    }

                    is PageContentInt.BlankPage -> BlackPage(
                        width = pageContent.width,
                        height = pageContent.height
                    )
                }
            }
        }
    }
}

fun Modifier.tapToZoomVertical(
    state: VerticalPdfReaderState,
    constraints: Constraints
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "verticalTapToZoom"
        properties["state"] = state
    }
) {
    val coroutineScope = rememberCoroutineScope()
    this
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { tapCenter ->
                    if (!state.isZoomEnabled) return@detectTapGestures
                    if (state.mScale > 1.0f) {
                        state.mScale = 1.0f
                        state.offset = Offset(0f, 0f)
                    } else {
                        state.mScale = 3.0f
                        val center = Pair(constraints.maxWidth / 2, constraints.maxHeight / 2)
                        val xDiff = (tapCenter.x - center.first) * state.scale
                        val yDiff = ((tapCenter.y - center.second) * state.scale).coerceIn(
                            minimumValue = -(center.second * 2f),
                            maximumValue = (center.second * 2f)
                        )
                        state.offset = Offset(-xDiff, -yDiff)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                val pair = if (pan.y > 0) {
                    if (state.lazyState.canScrollBackward) {
                        Pair(0f, pan.y)
                    } else {
                        Pair(pan.y, 0f)
                    }
                } else {
                    if (state.lazyState.canScrollForward) {
                        Pair(0f, pan.y)
                    } else {
                        Pair(pan.y, 0f)
                    }
                }
                val nOffset = if (state.scale > 1f) {
                    val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
                    val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
                    Offset(
                        x = (state.offset.x + pan.x).coerceIn(
                            minimumValue = (-maxT / 2) * 1.3f,
                            maximumValue = (maxT / 2) * 1.3f
                        ),
                        y = (state.offset.y + pair.first).coerceIn(
                            minimumValue = (-maxY / 2),
                            maximumValue = (maxY / 2)
                        )
                    )
                } else {
                    Offset(0f, 0f)
                }
                state.offset = nOffset
                coroutineScope.launch {
                    state.lazyState.scrollBy((-pair.second / state.scale))
                }
            }
        }
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
        }
}

fun Modifier.tapToZoomHorizontal(
    state: HorizontalPdfReaderState,
    constraints: Constraints
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "horizontalTapToZoom"
        properties["state"] = state
    }
) {
    this
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { tapCenter ->
                    if (!state.isZoomEnabled) return@detectTapGestures
                    if (state.mScale > 1.0f) {
                        state.mScale = 1.0f
                        state.offset = Offset(0f, 0f)
                    } else {
                        state.mScale = 3.0f
                        val center = Pair(constraints.maxWidth / 2, constraints.maxHeight / 2)
                        val xDiff = (tapCenter.x - center.first) * state.scale
                        val yDiff = ((tapCenter.y - center.second) * state.scale).coerceIn(
                            minimumValue = -(center.second * 2f),
                            maximumValue = (center.second * 2f)
                        )
                        state.offset = Offset(-xDiff, -yDiff)
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
                val nOffset = if (state.scale > 1f) {
                    val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
                    val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
                    Offset(
                        x = (state.offset.x + pan.x).coerceIn(
                            minimumValue = (-maxT / 2) * 1.3f,
                            maximumValue = (maxT / 2) * 1.3f
                        ),
                        y = (state.offset.y + pan.y).coerceIn(
                            minimumValue = (-maxY / 2) * 1.3f,
                            maximumValue = (maxY / 2) * 1.3f
                        )
                    )
                } else {
                    Offset(0f, 0f)
                }
                state.offset = nOffset
            }
        }
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
        }
}

@Composable
fun BlackPage(
    width: Int,
    height: Int
) {
    Box(
        modifier = Modifier
            .size(
                width = width.dp,
                height = height.dp
            )
            .background(color = Color.White)
    )
}