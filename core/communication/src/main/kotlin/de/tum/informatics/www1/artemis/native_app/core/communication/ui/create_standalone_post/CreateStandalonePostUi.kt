package de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.common.MarkdownTextField
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getHumanReadableTextForCourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getIconForCourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.ui.common.DropdownButton

@Composable
internal fun CreateStandalonePostUi(modifier: Modifier, viewModel: CreateStandalonePostViewModel) {
    Box(modifier = modifier) {
        CreateStandalonePostUiImpl(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            context = viewModel.context.collectAsState(initial = CourseWideContext.TECH_SUPPORT).value,
            title = viewModel.title.collectAsState(initial = "").value,
            content = viewModel.content.collectAsState(initial = "").value,
            tags = viewModel.tags.collectAsState(initial = "").value,
            updateContext = viewModel::updateContext,
            updateTitle = viewModel::updateTitle,
            updateContent = viewModel::updateContent,
            updateTags = viewModel::updateTags
        )
    }
}

@Composable
private fun CreateStandalonePostUiImpl(
    modifier: Modifier,
    context: CourseWideContext,
    title: String,
    content: String,
    tags: String,
    updateContext: (CourseWideContext) -> Unit,
    updateTitle: (String) -> Unit,
    updateContent: (String) -> Unit,
    updateTags: (String) -> Unit
) {
    val inputModifier = Modifier.fillMaxWidth()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SelectContext(
            modifier = inputModifier,
            currentContext = context,
            updateContext = updateContext
        )

        TitleInput(modifier = inputModifier, title = title, updateTitle = updateTitle)

        TagInput(modifier = inputModifier, tags = tags, updateTags = updateTags)

        ContentInput(inputModifier, content, updateContent)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    }
}

@Composable
private fun SelectContext(
    modifier: Modifier,
    currentContext: CourseWideContext,
    updateContext: (CourseWideContext) -> Unit
) {

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.create_standalone_post_label_select_context),
                style = MaterialTheme.typography.titleLarge
            )

            DropdownButton(
                modifier = Modifier,
                text = getHumanReadableTextForCourseWideContext(
                    courseWideContext = currentContext
                ),
                icon = getIconForCourseWideContext(currentContext)
            ) { hide ->
                CourseWideContext.values().forEach { cwc ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = getHumanReadableTextForCourseWideContext(courseWideContext = cwc)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getIconForCourseWideContext(cwc),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            hide()
                            updateContext(cwc)
                        }
                    )
                }
            }
        }

        if (currentContext == CourseWideContext.ANNOUNCEMENT) {
            Text(
                text = stringResource(id = R.string.create_standalone_post_info_announcement),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TitleInput(modifier: Modifier, title: String, updateTitle: (String) -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.create_standalone_post_section_title),
            style = MaterialTheme.typography.titleLarge
        )

        AnimatedVisibility(visible = title.isBlank()) {
            Text(
                text = stringResource(id = R.string.create_standalone_post_title_label_required),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = updateTitle,
            label = { Text(text = stringResource(id = R.string.create_standalone_post_title_label)) }
        )
    }
}

@Composable
private fun TagInput(modifier: Modifier, tags: String, updateTags: (String) -> Unit) {
    val tagList = remember(tags) {
        tags.split(',').map { it.trim() }.filter { it.isNotBlank() }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.create_standalone_post_section_tags),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = stringResource(id = R.string.create_standalone_post_tags_info),
            style = MaterialTheme.typography.labelMedium
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = tags,
            onValueChange = updateTags
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .imePadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tagList.forEach { tagText ->
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = tagText,
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (tagList.size > 3) {
            Text(
                text = stringResource(id = R.string.create_standalone_post_more_than_three_tags_error),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ContentInput(
    modifier: Modifier,
    content: String,
    updateContent: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.create_standalone_post_section_content),
            style = MaterialTheme.typography.titleLarge
        )

        AnimatedVisibility(visible = content.isBlank()) {
            Text(
                text = stringResource(id = R.string.create_standalone_post_content_label_required),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        MarkdownTextField(
            modifier = modifier,
            text = content,
            onTextChanged = updateContent
        )
    }
}

@Preview
@Composable
private fun CreateMetisStandalonePostUiPreview() {
    var context by remember { mutableStateOf(CourseWideContext.TECH_SUPPORT) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Tag1, Tag2") }

    Surface {
        CreateStandalonePostUiImpl(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            context = context,
            title = title,
            content = content,
            tags = tags,
            updateContext = { context = it },
            updateTitle = { title = it },
            updateContent = { content = it },
            updateTags = { tags = it }
        )
    }
}