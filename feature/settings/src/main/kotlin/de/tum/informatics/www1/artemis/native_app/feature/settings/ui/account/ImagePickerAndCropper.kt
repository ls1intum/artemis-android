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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.RoundRectCropShape
import com.attafitamim.krop.core.crop.cropperStyle
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
            val result = imageCropper.crop { imageSource }

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
            aspects = listOf(AspectRatio(1, 1)),
            shapes = listOf(RoundRectCropShape(15))
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
        }
    )
}