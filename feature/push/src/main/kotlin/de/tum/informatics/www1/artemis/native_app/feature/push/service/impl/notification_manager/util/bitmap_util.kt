package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF


fun Bitmap.toCircleShape() = addCornerRadius(width / 2)

// From https://stackoverflow.com/a/18229496
fun Bitmap.addCornerRadius(pixels: Int): Bitmap {
    val output = Bitmap.createBitmap(
        width, height, Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)
    val roundPx = pixels.toFloat()

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

    val softwareCopy = this.copy(Bitmap.Config.ARGB_8888, true)     // https://stackoverflow.com/a/66837703/13366254
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(softwareCopy, rect, rect, paint)

    return output
}