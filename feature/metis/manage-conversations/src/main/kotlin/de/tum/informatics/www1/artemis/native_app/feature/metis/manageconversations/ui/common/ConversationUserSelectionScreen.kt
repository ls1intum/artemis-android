package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.AdaptiveNavigationIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelectionBaseViewModel
import kotlinx.coroutines.Deferred

@Composable
internal fun <T> ConversationUserSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: MemberSelectionBaseViewModel,
    displayFailedDialog: Boolean,
    titleRes: Int = R.string.create_personal_conversation_title,
    dialogTitleRes: Int = R.string.create_personal_conversation_failed_title,
    dialogMessageRes: Int = R.string.create_personal_conversation_failed_message,
    dialogConfirmTextRes: Int = R.string.create_personal_conversation_failed_positive,
    fabTestTag: String,
    fabIcon: ImageVector = Icons.Default.ChevronRight,
    canSubmit: Boolean,
    startJob: () -> Deferred<T?>,
    onJobCompleted: (T?) -> Unit,
    onNavigateBack: () -> Unit,
    onDismissFailedDialog: () -> Unit,
    onSidebarToggle: () -> Unit
) {
    var numberOfSelectedUsers by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = { Text(stringResource(id = titleRes)) },
                navigationIcon = {
                    AdaptiveNavigationIcon(onNavigateBack = onNavigateBack, onSidebarToggle = onSidebarToggle)
                }
            )
        },
        floatingActionButton = {
            JobAnimatedFloatingActionButton(
                modifier = Modifier
                    .imePadding()
                    .testTag(fabTestTag),
                enabled = canSubmit,
                startJob = startJob,
                onJobCompleted = onJobCompleted
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        pluralStringResource(
                            R.plurals.create_personal_conversation_members,
                            numberOfSelectedUsers,
                            numberOfSelectedUsers
                        )
                    )
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        imageVector = fabIcon,
                        contentDescription = null
                    )
                }
            }
        }
    ) { padding ->
        MemberSelection(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding() + Spacings.ScreenTopBarSpacing)
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            viewModel = viewModel,
            onUpdateSelectedUserCount = { numberOfSelectedUsers = it }
        )
    }

    if (displayFailedDialog) {
        TextAlertDialog(
            title = stringResource(dialogTitleRes),
            text = stringResource(dialogMessageRes),
            confirmButtonText = stringResource(dialogConfirmTextRes),
            dismissButtonText = null,
            onPressPositiveButton = onDismissFailedDialog,
            onDismissRequest = onDismissFailedDialog
        )
    }
}
