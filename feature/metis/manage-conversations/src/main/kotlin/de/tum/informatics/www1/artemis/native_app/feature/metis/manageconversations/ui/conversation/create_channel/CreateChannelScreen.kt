package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Scaling
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.pagePadding
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.PotentiallyIllegalTextField
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_CREATE_CHANNEL_BUTTON = "create channel button"

internal const val TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH = "TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH"
internal const val TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH = "TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH"
internal const val TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH = "TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH"

@Composable
fun CreateChannelScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    CreateChannelScreen(
        modifier = modifier,
        viewModel = koinViewModel { parametersOf(courseId) },
        onConversationCreated = onConversationCreated,
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun CreateChannelScreen(
    modifier: Modifier,
    viewModel: CreateChannelViewModel,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isDisplayingErrorDialog by remember { mutableStateOf(false) }

    val name: String by viewModel.name.collectAsState()
    val description: String by viewModel.description.collectAsState()

    val isNameIllegal by viewModel.isNameIllegal.collectAsState()
    val isDescriptionIllegal by viewModel.isDescriptionIllegal.collectAsState()

    val isPublic by viewModel.isPublic.collectAsState()
    val isAnnouncement by viewModel.isAnnouncement.collectAsState()

    val canCreate by viewModel.canCreate.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_channel_title))
                },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = paddingValues.calculateTopPadding() + Spacings.ScreenTopBarSpacing)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .verticalScroll(rememberScrollState())
                .pagePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.create_channel_description),
                style = MaterialTheme.typography.bodySmall
            )

            PotentiallyIllegalTextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.create_channel_text_field_name_label),
                placeholder = stringResource(id = R.string.create_channel_text_field_name_hint),
                value = name,
                updateValue = viewModel::updateName,
                isIllegal = isNameIllegal,
                illegalStateExplanation = stringResource(
                    id = if (isNameIllegal) {
                        R.string.channel_text_field_name_invalid
                    } else R.string.create_channel_text_field_name_hint
                ),
                leadingIcon = {
                    if (isPublic) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                    }
                },
                requiredSupportText = stringResource(id = R.string.create_channel_text_field_name_required)
            )

            PotentiallyIllegalTextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.create_channel_text_field_description_label),
                placeholder = stringResource(id = R.string.create_channel_text_field_description_hint),
                value = description,
                updateValue = viewModel::updateDescription,
                isIllegal = isDescriptionIllegal,
                illegalStateExplanation = stringResource(
                    id = R.string.channel_text_field_description_invalid
                ),
                requiredSupportText = null
            )

            ChannelSettings(
                viewModel = viewModel,
                isAnnouncement = isAnnouncement
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag(TEST_TAG_CREATE_CHANNEL_BUTTON),
                enabled = canCreate,
                onClick = {
                    viewModel.createChannel { channel ->
                        if (channel != null) {
                            onConversationCreated(channel.id)
                        } else {
                            isDisplayingErrorDialog = true
                        }
                    }
                },
            ) {
                Text(text = stringResource(R.string.create_channel_button))
            }
        }
    }

    if (isDisplayingErrorDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.create_channel_failed_title),
            text = stringResource(id = R.string.create_channel_failed_message),
            confirmButtonText = stringResource(id = R.string.create_channel_failed_positive),
            dismissButtonText = null,
            onPressPositiveButton = { isDisplayingErrorDialog = false },
            onDismissRequest = { isDisplayingErrorDialog = false }
        )
    }
}

@Composable
private fun ChannelSettings(
    modifier: Modifier = Modifier,
    viewModel: CreateChannelViewModel,
    isAnnouncement: Boolean
) {
    val selectionModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.create_channel_settings_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            SegmentedSelection(
                modifier = selectionModifier,
                title = stringResource(id = R.string.create_channel_channel_accessibility_type),
                description = stringResource(id = R.string.create_channel_channel_accessibility_type_hint),
                buttonLabelOption = listOf(
                    stringResource(id = R.string.create_channel_channel_accessibility_type_public),
                    stringResource(id = R.string.create_channel_channel_accessibility_type_private)
                ),
                onCheckedChange = { viewModel.updatePublic(it) },
                testTag = TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH,
            )

            SegmentedSelection(
                modifier = selectionModifier,
                title = stringResource(id = R.string.create_channel_channel_type),
                description = stringResource(id = R.string.create_channel_channel_course_wide_type_hint),
                onCheckedChange = { viewModel.updateCourseWide(it) },
                testTag = TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH,
                buttonLabelOption = listOf(
                    stringResource(id = R.string.create_channel_channel_course_wide_type_announcement),
                    stringResource(id = R.string.create_channel_channel_course_wide_type_unrestricted)
                ),
            )

            BinarySelection(
                modifier = selectionModifier,
                title = stringResource(id = R.string.create_channel_channel_announcement_type),
                description = stringResource(id = R.string.create_channel_channel_announcement_type_hint),
                isChecked = isAnnouncement,
                onCheckedChange = { viewModel.updateAnnouncement(it) },
                switchTestTag = TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH,
            )
        }
    }
}

@Composable
private fun SegmentedSelection(
    modifier: Modifier,
    title: String,
    description: String,
    buttonLabelOption: List<String>,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    val targetOption = 0
    var selectedOption by remember { mutableIntStateOf(targetOption) }

    Column(
        modifier = modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .scale(Scaling.SWITCH)
                    .testTag(testTag)
            ) {
                buttonLabelOption.forEachIndexed { index, label ->
                    val selected = index == selectedOption
                    SegmentedButton(
                        modifier = Modifier.testTag(testTag + index),
                        selected = selected,
                        colors = SegmentedButtonDefaults.colors(
                            inactiveContainerColor = CardDefaults.cardColors().containerColor
                        ),
                        onClick = {
                            selectedOption = index
                            onCheckedChange(selectedOption == targetOption)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, buttonLabelOption.size),
                        icon = {
                            SegmentedButtonDefaults.Icon(
                                active = selected,
                                activeContent = {
                                    Icon(
                                        modifier = Modifier.padding(start = 8.dp),
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = label
                        )
                    }
                }
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun BinarySelection(
    modifier: Modifier,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTestTag: String = ""
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                modifier = Modifier
                    .scale(Scaling.SWITCH)
                    .testTag(switchTestTag),
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

