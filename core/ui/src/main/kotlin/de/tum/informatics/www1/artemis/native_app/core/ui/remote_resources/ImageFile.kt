package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources

import android.content.Context
import java.io.File

/**
 * Class representing an image file that can be downloaded and shared.
 * Used for lecture unit attachments.
 *
 * @property imagePath The path of the image file.
 * @property authToken The authentication token used for downloading the file.
 * @property filename The name of the file.
 */
class ImageFile(
    val imagePath: String,
    override val authToken: String,
    override val filename: String
): BaseFile(authToken, filename, imagePath) {


    override fun share(context: Context, file: File) {
        TODO("Not yet implemented")
    }

}