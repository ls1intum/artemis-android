package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp

class BitmapView(
    ctx: Context,
    onBitmapCreated: (bitmap: Bitmap) -> Unit,
    content: @Composable () -> Unit
) : LinearLayoutCompat(ctx) {

    init {
        val width = 600
        val height = 670

        val view = ComposeView(ctx)
        view.visibility = View.GONE
        view.layoutParams = LayoutParams(width, height)
        this.addView(view)

        view.setContent {
            content()
        }

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bitmap = createBitmapFromView(view = view, width = width, height = height)
                println("Bitmap created: $bitmap")
                onBitmapCreated(bitmap)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
        view.layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )

        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )

        view.layout(0, 0, width, height)

        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        canvas.setBitmap(bitmap)
        view.draw(canvas)

        return bitmap
    }
}



object BitmapFromComposableUtil {

//    fun getBitmapFromComposable(
//        context: Context,
//        unscaledWidth: Int = 100,
//        unscaledHeight: Int = 100,
//        content: @Composable () -> Unit
//    ): Bitmap {
//        // Create a new ComposeView
//        val composeView = ComposeView(context).apply {
//            setContent {
//                Box(
//                    modifier = Modifier
//                        .drawWithCache {
//
//                        }
//                ) {
//                    content()
//                } }
//        }
//
//    }
}

// Extension function to convert dp to pixels
fun Dp.toPx(context: Context): Float {
    return (this * context.resources.displayMetrics.density).value
}