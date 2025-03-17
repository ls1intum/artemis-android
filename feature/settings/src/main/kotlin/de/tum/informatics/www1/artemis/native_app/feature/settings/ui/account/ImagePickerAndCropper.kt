package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropState
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.ImagePicker
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import kotlinx.coroutines.launch


@Composable
fun rememberCroppingImagePicker(
    imageCropper: ImageCropper,
    onCropSuccess: (ImageBitmap) -> Unit,
): ImagePicker {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    return rememberImagePicker(onImage = { uri ->
        scope.launch {
            val result = imageCropper.crop(
                uri = uri,
                context = context,
            )

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
        style = CropperStyle(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            secondaryHandles = false,
            autoZoom = false,
        ),
        topBar = {
            TopAppBar(
                title = { Text("Crop Image") },
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
                        Text("Apply")
                    }
                }
            )
        },
        cropControls = {
            // Unfortunately the crop controls are only internal and cannot be accessed by custom composables.
            // See: https://github.com/mr0xf00/easycrop/issues/4
        }
    )
}

