package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.Keyboard
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.keyboardAsState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EmojiPickerModalBottomSheet(
    modifier: Modifier = Modifier,
    onEmojiClicked: (Emoji) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var previousKeyboardState by remember { mutableStateOf<Keyboard?>(null) }
    val keyboardState by keyboardAsState()

    LaunchedEffect(keyboardState) {
        if (previousKeyboardState != null && keyboardState == Keyboard.Opened) {
            coroutineScope.launch {
                // There seems to be an internal call after the keyboard animation that causes the
                // sheet to go to the partially expanded state. This delay is a workaround to ensure
                // the sheet is fully expanded after the keyboard animation.
                // This here might be related: https://github.com/ls1intum/artemis-android/issues/368
                delay(300.milliseconds)
                sheetState.expand()
            }
        }

        previousKeyboardState = keyboardState
    }

    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        EmojiPicker(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            onEmojiClicked = onEmojiClicked
        )
    }
}

