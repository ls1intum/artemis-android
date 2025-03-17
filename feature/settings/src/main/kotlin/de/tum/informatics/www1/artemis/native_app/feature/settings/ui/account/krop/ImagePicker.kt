package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.account.krop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.attafitamim.krop.core.images.ImageSrc
import com.attafitamim.krop.core.images.toImageSrc
import kotlinx.coroutines.launch


// Copied from the krop repo sample directory: https://github.com/tamimattafi/krop/blob/c9359c55db140bcd03cd72ef05c357f7688cc5c0/sample/composeApp/src/commonMain/kotlin/com/attafitamim/krop/sample/picker/ImagePicker.kt
// and https://github.com/tamimattafi/krop/blob/main/sample/composeApp/src/androidMain/kotlin/com/attafitamim/krop/sample/picker/ImagePicker.android.kt


interface ImagePicker {
    /** Pick an image with [mimetype] */
    fun pick(mimetype: String = "image/*")
}

/** Creates and remembers a instance of [ImagePicker] that launches
 * [ActivityResultContracts.GetContent] and calls [onImage] when the result is available */
@Composable
fun rememberImagePicker(
    onImage: (uri: ImageSrc) -> Unit
): ImagePicker {
    val context = LocalContext.current
    val contract = remember { ActivityResultContracts.GetContent() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = { uri ->
            coroutineScope.launch {
                val imageSrc = uri?.toImageSrc(context) ?: return@launch
                onImage(imageSrc)
            }
        }
    )

    return remember {
        object : ImagePicker {
            override fun pick(mimetype: String) = launcher.launch(mimetype)
        }
    }
}