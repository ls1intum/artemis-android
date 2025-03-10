package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicArtemisTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.ForwardedMessagePreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost

@Composable
fun PostForwardBottomSheet(
    post: IBasePost,
    forwardMessageUseCase: ForwardMessageUseCase,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pagePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.post_forward_title),
                style = MaterialTheme.typography.titleMedium
            )

            MemberSelection(
                modifier = Modifier.fillMaxWidth(),
                viewModel = forwardMessageUseCase,
                isConversationSelectionEnabled = true,
                memberSelectionMode = MemberSelectionMode.MemberSelectionDropdown,
                onUpdateSelectedUserCount = { }
            )

            BasicArtemisTextField(
                modifier = Modifier.fillMaxWidth(),
                value = TextFieldValue(""),
                onValueChange = {},
                hint = "Add a text here",
                backgroundColor = MaterialTheme.colorScheme.background,
                onFocusChanged = {  },
            )

            ForwardedMessagePreview(
                modifier = Modifier.fillMaxWidth(),
                forwardedPost = post
            )
        }
    }
}