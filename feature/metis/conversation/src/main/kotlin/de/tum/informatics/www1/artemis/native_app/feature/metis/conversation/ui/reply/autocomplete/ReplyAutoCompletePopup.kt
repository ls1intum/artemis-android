package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisPopupSurface
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture

const val TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST = "TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST"

@Composable
internal fun ReplyAutoCompletePopup(
    popupPositionProvider: PopupPositionProvider,
    targetWidth: Dp,
    maxHeightFromScreen: Dp,
    autoCompleteCategories: List<AutoCompleteHintCollection>,
    performAutoComplete: (replacement: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Popup(
        popupPositionProvider = popupPositionProvider,
        properties = PopupProperties(dismissOnClickOutside = true),
        onDismissRequest = onDismissRequest
    ) {
        val maxHeight = min(maxHeightFromScreen, Spacings.Popup.maxHeight)
        val verticalPadding = Spacings.Popup.ContentVerticalPadding
        ReplyAutoCompletePopupBody(
            modifier = Modifier
                .padding(vertical = verticalPadding, horizontal = 8.dp)
                .heightIn(max = maxHeight - verticalPadding * 2)
                .width(targetWidth),
            autoCompleteCategories = autoCompleteCategories,
            performAutoComplete = performAutoComplete
        )
    }
}

@Composable
private fun ReplyAutoCompletePopupBody(
    modifier: Modifier,
    autoCompleteCategories: List<AutoCompleteHintCollection>,
    performAutoComplete: (replacement: String) -> Unit
) {
    ArtemisPopupSurface(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .testTag(TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST)
        ) {

            autoCompleteCategories.forEachIndexed { categoryIndex, category ->
                stickyHeader {
                    AutoCompleteCategoryComposable(
                        modifier = Modifier.fillMaxWidth(),
                        name = stringResource(id = category.type.title)
                    )
                }

                category.items.fastForEachIndexed { i, hint ->
                    when (hint) {
                        is AutoCompleteHint.UserSearchQueryTooShort -> {
                            item {
                                EmptyListHint(
                                    modifier = Modifier.fillMaxWidth(),
                                    imageVector = Icons.Default.Keyboard,
                                    hint = stringResource(
                                        id = R.string.conversation_member_selection_query_too_short_popup,
                                        MemberSelectionBaseViewModel.MINIMUM_QUERY_LENGTH
                                    )
                                )
                            }
                        }

                        is AutoCompleteHint.Data -> {
                            item(key = "${category.type}_${hint.id}") {
                                AutoCompleteHintComposable(
                                    modifier = Modifier.fillMaxWidth(),
                                    hint = hint,
                                    onClick = { performAutoComplete(hint.replacementText) }
                                )
                            }
                        }
                    }
                }

                if (categoryIndex != autoCompleteCategories.lastIndex) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoCompleteCategoryComposable(modifier: Modifier, name: String) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(Spacings.Popup.tonalElevation))
            .padding(horizontal = Spacings.Popup.HintHorizontalPadding, vertical = 4.dp),
        text = name,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AutoCompleteHintComposable(
    modifier: Modifier,
    hint: AutoCompleteHint.Data,
    onClick: () -> Unit
) {
    val horizontalPadding = Spacings.Popup.HintHorizontalPadding

    Row(
        modifier = modifier
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(horizontalPadding)
    ) {
        hint.icon?.let {
            val iconSize = 24.dp
            when (it) {
                is AutoCompleteIcon.DrawableFromImageVector -> Icon(
                    imageVector = it.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                is AutoCompleteIcon.DrawableFromId -> Icon(
                    painter = painterResource(id = it.id ?: return@Row),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                is AutoCompleteIcon.ProfilePicture -> ProfilePicture(
                    modifier = Modifier.size(iconSize),
                    profilePictureData = it.pictureData
                )
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = hint.hint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun ReplyAutoCompletePopupBodyPreview() {
    ReplyAutoCompletePopupBody(
        modifier = Modifier.fillMaxSize(),
        autoCompleteCategories = listOf(
            AutoCompleteHintCollection(
                type = AutoCompleteType.USERS,
                items = (0 until 1).map {
                    AutoCompleteHint.Data(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString() + "a"
                    )
                }
            ),
            AutoCompleteHintCollection(
                type = AutoCompleteType.USERS,
                items = (0 until 1).map {
                    AutoCompleteHint.Data(
                        hint = "Hint $it",
                        replacementText = "",
                        id = it.toString() + "b"
                    )
                }
            ),
            AutoCompleteHintCollection(
                type = AutoCompleteType.USERS,
                items = (0 until 1).map {
                    AutoCompleteHint.Data(
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