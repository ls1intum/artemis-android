package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources

import android.content.Context
import java.io.File

class ImageFile(
    val imagePath: String,
    override val authToken: String,
    override val filename: String
): BaseFile(authToken, filename, imagePath) {


    override fun share(context: Context, file: File) {
        TODO("Not yet implemented")
    }

}