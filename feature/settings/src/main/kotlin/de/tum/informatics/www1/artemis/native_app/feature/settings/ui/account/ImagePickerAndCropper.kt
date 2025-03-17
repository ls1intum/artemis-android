package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.cropSrc
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.flipHorizontal
import com.attafitamim.krop.core.crop.rotLeft
import com.attafitamim.krop.core.crop.rotRight
import com.attafitamim.krop.ui.ImageCropperDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account.krop.ImagePicker
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account.krop.rememberImagePicker
import kotlinx.coroutines.launch


@Composable
fun rememberCroppingImagePicker(
    imageCropper: ImageCropper,
    onCropSuccess: (ImageBitmap) -> Unit,
): ImagePicker {
    val scope = rememberCoroutineScope()

    return rememberImagePicker(onImage = { imageSource ->
        scope.launch {
            val result = imageCropper.cropSrc(imageSource)

            when (result) {
                is CropResult.Success -> {
                    onCropSuccess(result.bitmap)
                }
                else -> { /* ignore */ }
            }
        }
    })
}

    @Composable
fun ImagePickerAndCropper(
    imageCropper: ImageCropper
) {
    val cropState = imageCropper.cropState
    LaunchedEffect(cropState) {
        if (cropState == null) return@LaunchedEffect
        val initialRectSideLength = kotlin.math.min(cropState.region.width, cropState.region.height)
        cropState.region = Rect(Offset.Zero, Size(initialRectSideLength, initialRectSideLength))
        cropState.aspectLock = true
    }

    if(cropState != null) {
        CustomizedCropperDialog(cropState)
    }
}

@Composable
private fun CustomizedCropperDialog(cropState: CropState) {
    ImageCropperDialog(
        state = cropState,
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        dialogShape = MaterialTheme.shapes.large,
        style = cropperStyle(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            secondaryHandles = false,
            autoZoom = false,
        ),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_picture_crop_title)) },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack = {
                        cropState.done(false)
                    })
                },
                actions = {
                    Button(
                        onClick = {
                            cropState.done(true)
                        }
                    ) {
                        Text(stringResource(R.string.profile_picture_crop_apply))
                    }
                }
            )
        },
        cropControls = {
            CropControls(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                state = it
            )
        }
    )
}

@Composable
private fun CropControls(
    modifier: Modifier = Modifier,
    state: CropState
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            IconButton(onClick = { state.rotLeft() }) {
                Icon(Icons.Default.RotateLeft, null)
            }
            IconButton(onClick = { state.rotRight() }) {
                Icon(Icons.Default.RotateRight, null)
            }
            IconButton(onClick = { state.flipHorizontal() }) {
                Icon(Icons.Default.Flip, null)
            }
        }
    }
}

