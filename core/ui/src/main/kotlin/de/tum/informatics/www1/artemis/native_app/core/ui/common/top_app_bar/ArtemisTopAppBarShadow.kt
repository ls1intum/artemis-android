package de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings


fun Modifier.dropShadowBelow(
    elevation: Dp = Spacings.AppBarElevation,
    color: Color = Color.Black.copy(0.15f)
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.maskFilter = (BlurMaskFilter(elevation.toPx(), BlurMaskFilter.Blur.NORMAL))

        frameworkPaint.color = color.toArgb()

        val rightPixel = size.width
        val bottomPixel = size.height

        canvas.drawRect(
            left = 0f,
            top = elevation.toPx(),
            right = rightPixel,
            bottom = bottomPixel,
            paint = paint,
        )
    }
}

// Inspired by the following Medium article:
// https://medium.com/@kappdev/inner-shadow-in-jetpack-compose-d80dcd56f6cf
fun Modifier.innerShadow(
    offset: Dp = 2.dp,
    color: Color = Color.Black.copy(0.5f)
) = this.drawWithContent {
    drawContent()
    drawIntoCanvas { canvas ->
        val shadowSize = Size(size.width + offset.toPx(), size.height + offset.toPx())
        val shadowOutline =
            RoundedCornerShape(12.dp).createOutline(shadowSize, layoutDirection, this)

        val paint = Paint()
        paint.color = color

        canvas.saveLayer(size.toRect(), paint)
        canvas.drawOutline(shadowOutline, paint)

        paint.asFrameworkPaint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (4.dp.toPx() > 0) {
                maskFilter = BlurMaskFilter(4.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
        }

        paint.color = Color.Black
        canvas.translate(offset.toPx(), offset.toPx())
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}
