package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getString
import androidx.core.content.FileProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import java.io.File

/**
 * Class representing an image file that can be downloaded and shared.
 * Used for lecture unit attachments.
 *
 * @property url The URL of the PDF file.
 * @property authToken The authentication token used for downloading the file.
 * @property filename The name of the file.
 */
class ImageFile(
    override val url: String,
    override val authToken: String,
    override val filename: String
): BaseFile(authToken, filename, url) {

    override fun share(context: Context, file: File) {
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            clipData = ClipData(
                "image",
                arrayOf("image/*"),
                ClipData.Item(imageUri)
            )
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(
                shareIntent,
                getString(context, R.string.image_view_share_title)
            )
        )
    }
}