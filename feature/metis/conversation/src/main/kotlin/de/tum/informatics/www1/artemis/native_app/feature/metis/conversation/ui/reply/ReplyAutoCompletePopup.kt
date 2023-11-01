package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R

private val HintHorizontalPadding = 16.dp

@Composable
internal fun ReplyAutoCompletePopup(
    autoCompleteCategories: List<AutoCompleteCategory>,
    performAutoComplete: (replacement: String) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Popup(
        popupPositionProvider = ReplyAutoCompletePopupPositionProvider,
        properties = PopupProperties()
    ) {
        ReplyAutoCompletePopupBody(
            modifier = Modifier.heightIn(max = screenHeight * 0.5f),
            autoCompleteCategories = autoCompleteCategories,
            performAutoComplete = performAutoComplete
        )
    }
}

private object ReplyAutoCompletePopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = anchorBounds.topLeft - IntOffset(0, popupContentSize.height)

}

@Composable
private fun ReplyAutoCompletePopupBody(
    modifier: Modifier,
    autoCompleteCategories: List<AutoCompleteCategory>,
    performAutoComplete: (replacement: String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 45f, topEnd = 45f)
            )
            .padding(top = 8.dp)
    ) {
        autoCompleteCategories.forEach { category ->
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
                    item { Divider() }
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
            style = MaterialTheme.typography.titleSmall
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
                items = (0 until 10).map {
                    AutoCompleteHint(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString()
                    )
                }
            )
        ),
        performAutoComplete = {}
    )
}