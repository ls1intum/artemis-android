package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

private const val MAX_DIMENSION_SIZE_IN_PIXEL = 200        // The webapp seems to use 200x200 px
// See https://github.com/ls1intum/Artemis/blob/develop/src/main/java/de/tum/cit/aet/artemis/core/web/AccountResource.java#L72
private const val MAX_FILE_SIZE_IN_MEGABYTES = 0.1f
private const val LOWEST_REASONABLE_COMPRESSION_QUALITY = 5

object ProfilePictureBitmapUtil {

    fun ensureSizeConstraints(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSize = maxOf(width, height)
        if (maxSize <= MAX_DIMENSION_SIZE_IN_PIXEL) {
            return bitmap
        }
        return Bitmap.createScaledBitmap(bitmap, MAX_DIMENSION_SIZE_IN_PIXEL, MAX_DIMENSION_SIZE_IN_PIXEL, false)
    }

    fun toJpegCompressedByteArray(bitmap: Bitmap): ByteArray? {
        var quality = 100   // Maximum quality
        val outputStream = ByteArrayOutputStream()

        do {
            if (quality < LOWEST_REASONABLE_COMPRESSION_QUALITY) {
                return null
            }

            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality = (quality * 0.8f).toInt()
        } while (outputStream.size() > MAX_FILE_SIZE_IN_MEGABYTES * 1024 * 1024)

        return outputStream.toByteArray()
    }
}