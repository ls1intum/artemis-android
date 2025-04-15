@file:OptIn(ExperimentalCoilApi::class)

package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalAsyncImagePreviewHandler
import de.drick.compose.edgetoedgepreviewlib.CameraCutoutMode
import de.drick.compose.edgetoedgepreviewlib.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreviewlib.NavigationMode
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderStub

val backgroundColor1 = Color(0xFF58BDEC)
val backgroundColor2 = Color(0xFF115BDA)

object Phone {
    val frameWidth = 13.dp
    const val roundness = 12
    val backgroundColor = Color.White
}

@Composable
fun ScreenshotFrame(
    title: String,
    textAlign: TextAlign = TextAlign.Center,
    content: @Composable () -> Unit
) {
    ScreenshotTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                backgroundColor1,
                                backgroundColor2,
                            ),
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .padding(32.dp),
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                    ),
                    fontWeight = FontWeight.Medium,
                    textAlign = textAlign
                )

                PhoneFrame(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .padding(horizontal = 48.dp)
                        .aspectRatio(9f / 19f)
                ) {
                    CompositionLocalProvider(
                        LocalArtemisImageProvider provides ArtemisImageProviderStub(),
                        LocalAsyncImagePreviewHandler provides ScreenshotData.Util.configImagePreviewHandler(),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = Phone.frameWidth,
                color = Color.Black,
                shape = RoundedCornerShape(Phone.roundness)
            )
            .padding(Phone.frameWidth)
    ) {
        EdgeToEdgeTemplate(
            modifier = Modifier
                .clip(RoundedCornerShape(Phone.roundness - 3)),
            navMode = NavigationMode.Gesture,
            cameraCutoutMode = CameraCutoutMode.Middle,
            showInsetsBorder = false,
            isInvertedOrientation = false
        ) {
            content()
        }
    }
}

@Composable
@PlayStoreScreenshots
private fun Preview() {
    ScreenshotFrame(title = "Manage all of your courses\nin one app") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFFFFFFF)),
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .fillMaxSize()
                    .background(color = Color(0xFFd0c0f0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier
                        .padding(64.dp),
                    text = "This is a preview of the screenshot frame.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}