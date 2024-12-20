package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R

const val TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST = "TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST"

private val HintHorizontalPadding = 16.dp
private val PopupVerticalPadding = 8.dp
private val AutoCompletionDialogMaxHeight = 270.dp

@Composable
internal fun ReplyAutoCompletePopup(
    popupPositionProvider: PopupPositionProvider,
    targetWidth: Dp,
    maxHeightFromScreen: Dp,
    autoCompleteCategories: List<AutoCompleteCategory>,
    performAutoComplete: (replacement: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Popup(
        popupPositionProvider = popupPositionProvider,
        properties = PopupProperties(dismissOnClickOutside = true),
        onDismissRequest = onDismissRequest
    ) {
        val maxHeight = min(maxHeightFromScreen, AutoCompletionDialogMaxHeight)
        ReplyAutoCompletePopupBody(
            modifier = Modifier
                .padding(vertical = PopupVerticalPadding)
                .heightIn(max = maxHeight - PopupVerticalPadding * 2)
                .width(targetWidth),
            autoCompleteCategories = autoCompleteCategories,
            performAutoComplete = performAutoComplete
        )
    }
}

@Composable
private fun ReplyAutoCompletePopupBody(
    modifier: Modifier,
    autoCompleteCategories: List<AutoCompleteCategory>,
    performAutoComplete: (replacement: String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(8.dp)
            .testTag(TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST)
    ) {

        autoCompleteCategories.forEachIndexed { categoryIndex, category ->
            item {
                AutoCompleteCategoryComposable(
                    modifier = Modifier.fillMaxWidth(),
                    name = stringResource(id = category.name)
                )
            }

            category.items.fastForEachIndexed { i, hint ->
                item(key = "${category.name}_${hint.id}") {
                    AutoCompleteHintComposable(
                        modifier = Modifier.fillMaxWidth(),
                        hint = hint,
                        onClick = { performAutoComplete(hint.replacementText) }
                    )
                }

                if (i != category.items.lastIndex) {
                    item { HorizontalDivider() }
                }
            }

            if (categoryIndex != autoCompleteCategories.lastIndex) {
                item {
                    Column {
                        HorizontalDivider()

                        Box(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoCompleteCategoryComposable(modifier: Modifier, name: String) {
    Box(modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HintHorizontalPadding),
            text = name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AutoCompleteHintComposable(
    modifier: Modifier,
    hint: AutoCompleteHint,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HintHorizontalPadding, vertical = 8.dp),
            text = hint.hint
        )
    }
}

@Preview
@Composable
private fun ReplyAutoCompletePopupBodyPreview() {
    ReplyAutoCompletePopupBody(
        modifier = Modifier.fillMaxSize(),
        autoCompleteCategories = listOf(
            AutoCompleteCategory(
                name = R.string.markdown_textfield_autocomplete_category_users,
                items = (0 until 1).map {
                    AutoCompleteHint(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString() + "a"
                    )
                }
            ),
            AutoCompleteCategory(
                name = R.string.markdown_textfield_autocomplete_category_users,
                items = (0 until 1).map {
                    AutoCompleteHint(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString() + "b"
                    )
                }
            ),
            AutoCompleteCategory(
                name = R.string.markdown_textfield_autocomplete_category_users,
                items = (0 until 1).map {
                    AutoCompleteHint(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString() + "c"
                    )
                }
            )
        ),
        performAutoComplete = {}
    )
}