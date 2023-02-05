package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun EmojiView(modifier: Modifier, emojiUnicode: String, emojiFontSize: TextUnit) {
    check(emojiFontSize.isSp)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AppCompatTextView(context).apply {
                setTextColor(Color.Black.toArgb())
                text = emojiUnicode
                textSize = emojiFontSize.value
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        },
        update = { textView ->
            textView.text = emojiUnicode
        }
    )
}
