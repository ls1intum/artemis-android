package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.endOfPagePadding
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ImageFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider

@Composable
fun ArtemisImageView(
    modifier: Modifier = Modifier,
    imageFile: ImageFile,
    dismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }

    val artemisImageProvider = LocalArtemisImageProvider.current
    val painter = artemisImageProvider.rememberArtemisAsyncImagePainter(imagePath = imageFile.imagePath)
    val painterState by painter.state.collectAsState()

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(1f, 5f)
            offset += pan
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (painterState is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.None,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
                    .then(gestureModifier)
            )
        } else if (painterState is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = imageFile.filename,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .endOfPagePadding()
        ) {
            if (showMenu) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                modifier = Modifier
            ) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = null)
            }

            ImageActionDropDownMenu(
                modifier = Modifier,
                isExpanded = showMenu,
                onDismissRequest = { showMenu = false },
                downloadImage = {
                    showMenu = false
                    //imageFile.onDownload()
                },
                shareImage = {
                    showMenu = false
                    //imageFile.onShare()
                }
            )
        }
    }
}


@Composable
private fun ImageActionDropDownMenu(
    modifier: Modifier,
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    downloadImage: () -> Unit,
    shareImage: () -> Unit,
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.image_view_download_menu_item)) },
            onClick = downloadImage
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.image_view_share_menu_item)) },
            onClick = shareImage
        )
    }
}