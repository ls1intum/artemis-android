package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.JobAnimatedFloatingActionButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.navigateToConversationDetailScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToCreateChannelScreen(
    courseId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/create_channel", builder)
}

fun NavGraphBuilder.createChannelScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    composable(
        route = "course/{courseId}/create_channel",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false }
        )
    ) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        CreateChannelScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            onConversationCreated = { conversationId ->
                navController.popBackStack()
                navController.navigateToConversationDetailScreen(courseId, conversationId) { }
            },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun CreateChannelScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: CreateChannelViewModel = koinViewModel { parametersOf(courseId) }

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
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_channel_title))
                },
                navigationIcon = {
                    NavigationBackButton(onNavigateBack)
                }
            )
        },
        floatingActionButton = {
            JobAnimatedFloatingActionButton(
                enabled = canCreate,
                startJob = viewModel::createChannel,
                onJobCompleted = { channel ->
                    if (channel != null) {
                        onConversationCreated(channel.id)
                    } else {
                        isDisplayingErrorDialog = true
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.create_channel_description)
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
                        R.string.create_channel_text_field_name_invalid
                    } else R.string.create_channel_text_field_name_hint
                )
            )

            PotentiallyIllegalTextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.create_channel_text_field_description_label),
                placeholder = stringResource(id = R.string.create_channel_text_field_description_hint),
                value = description,
                updateValue = viewModel::updateDescription,
                isIllegal = isDescriptionIllegal,
                illegalStateExplanation = stringResource(
                    id = R.string.create_channel_text_field_description_invalid
                )
            )

            BinarySelection(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.create_channel_channel_accessibility_type),
                description = stringResource(id = R.string.create_channel_channel_accessibility_type_hint),
                choiceOne = stringResource(id = R.string.create_channel_channel_accessibility_type_public),
                choiceTwo = stringResource(id = R.string.create_channel_channel_accessibility_type_private),
                choice = isPublic,
                updateChoice = viewModel::updatePublic
            )

            BinarySelection(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.create_channel_channel_announcement_type),
                description = stringResource(id = R.string.create_channel_channel_announcement_type_hint),
                choiceOne = stringResource(id = R.string.create_channel_channel_announcement_type_announcement),
                choiceTwo = stringResource(id = R.string.create_channel_channel_announcement_type_unrestricted),
                choice = isAnnouncement,
                updateChoice = viewModel::updateAnnouncement
            )
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
private fun PotentiallyIllegalTextField(
    modifier: Modifier,
    label: String,
    value: String,
    placeholder: String,
    updateValue: (String) -> Unit,
    isIllegal: Boolean,
    illegalStateExplanation: String
) {
    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = label) },
        value = value,
        onValueChange = updateValue,
        placeholder = { Text(text = placeholder) },
        supportingText = {
            if (isIllegal) {
                Text(text = illegalStateExplanation)
            }
        },
        isError = isIllegal
    )
}

@Composable
private fun BinarySelection(
    modifier: Modifier,
    title: String,
    description: String,
    choiceOne: String,
    choiceTwo: String,
    choice: Boolean,
    updateChoice: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.titleSmall
        )

        Column {
            RadioButtonWithText(
                modifier = Modifier.fillMaxWidth(),
                isChecked = choice,
                onClick = { updateChoice(true) },
                text = choiceOne
            )

            RadioButtonWithText(
                modifier = Modifier.fillMaxWidth(),
                isChecked = !choice,
                onClick = { updateChoice(false) },
                text = choiceTwo
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = description,
        )
    }
}

@Composable
private fun RadioButtonWithText(
    modifier: Modifier,
    isChecked: Boolean,
    onClick: () -> Unit,
    text: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isChecked, onClick = onClick)

        Text(text = text)
    }
}
