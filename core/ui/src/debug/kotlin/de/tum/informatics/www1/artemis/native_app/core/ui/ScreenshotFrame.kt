@file:OptIn(ExperimentalCoilApi::class)

package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalAsyncImagePreviewHandler
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider

val backgroundColor1 = Color(0xff89cff0)
val backgroundColor2 = Color(0xFF2394C9)

object Phone {
    val frameWidth = 13.dp
    const val roundness = 12
    val backgroundColor = Color.White

    val punchHoleSize = 18.dp
    val statusBarPadding = 6.dp
    val statusBarHeight = punchHoleSize + statusBarPadding * 2

    val navigationIndicatorHeight = 5.dp
    val navigationBarPadding = 6.dp
    val navigationBarHeight = navigationIndicatorHeight + navigationBarPadding * 2
}

@Composable
fun ScreenshotFrame(title: String, content: @Composable ColumnScope.() -> Unit) {
    ScreenshotTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(backgroundColor1, backgroundColor2),
                        )
                    )
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(25)
                        )
                        .padding(16.dp),
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                PhoneFrame {
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
private fun ColumnScope.PhoneFrame(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 64.dp)
                .padding(bottom = 16.dp)
                .border(
                    width = Phone.frameWidth,
                    color = Color.Black,
                    shape = RoundedCornerShape(Phone.roundness)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Phone.frameWidth),
            ) {
                FakeStatusBar()
                content()
                // TODO: inject preview window insets, see https://medium.com/@timo_86166/jetpack-compose-previews-for-edge-to-edge-design-a03b3a3713f3


                FakeNavigationBar()
            }
        }
    }
}

@Composable
private fun FakeStatusBar() {
    Box(
        modifier = Modifier
            .zIndex(1f)

            .fillMaxWidth()
            .background(color = Phone.backgroundColor)
            .padding(vertical = Phone.statusBarPadding)
    ) {
        // Punch hole
        val size = Phone.punchHoleSize
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black, shape = RoundedCornerShape(50))
                .height(size)
                .width(size)
        )
    }
}

@Composable
private fun FakeNavigationBar() {
    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxWidth()
            .background(color = Phone.backgroundColor)
            .padding(vertical = Phone.navigationBarPadding)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.DarkGray, shape = RoundedCornerShape(50))
                .height(Phone.navigationIndicatorHeight)
                .fillMaxWidth(0.25f)
        )
    }
}


@Composable
@PlayStoreScreenshots
private fun Preview() {
    ScreenshotFrame(title = "Manage all of your courses in one app") {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .statusBarsPadding()
                .weight(1f)
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