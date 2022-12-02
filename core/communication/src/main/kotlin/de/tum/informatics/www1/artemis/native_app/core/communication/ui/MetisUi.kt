package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisStandalonePostList
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.ViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity.CourseWideContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SmartphoneMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }

    Column(modifier = modifier) {
        MetisFilterHeader(modifier = Modifier.fillMaxWidth(), context = metisContext)

        MetisStandalonePostList(
            modifier = modifier,
            viewModel = viewModel,
            onClickReply = { clientPostId ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    viewType = ViewType.WRITE_COMMENT
                ) {}
            },
            onClickViewReplies = { clientPostId ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    viewType = ViewType.REPLIES
                ) {}
            }
        )
    }
}

@Composable
private fun MetisFilterHeader(modifier: Modifier, context: MetisContext) {
    Row(modifier = modifier) {
        FilterChipRow(modifier = Modifier.weight(1f), context = context)

        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        }

        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null)
        }
    }
}

/**
 * Displays a row of filter chips depending on the context.
 */
@Composable
private fun FilterChipRow(modifier: Modifier, context: MetisContext) {
    Row(
        modifier = modifier.then(Modifier.horizontalScroll(rememberScrollState())),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (context is MetisContext.Course) {
            var courseWideContext: CourseWideContext? by
            rememberSaveable(context) { mutableStateOf(null) }

            val selectCourseWideContext = { new: CourseWideContext? -> courseWideContext = new }

            CourseWideContext.values().forEach { cwc ->
                CourseWideContextFilterChip(
                    modifier = Modifier,
                    courseWideContext = cwc,
                    selectedCourseWideContext = courseWideContext,
                    onSelect = selectCourseWideContext
                )
            }
        } else if (context is MetisContext.Exercise || context is MetisContext.Lecture) {
            var filter: List<MetisFilter> by rememberSaveable(context) {
                mutableStateOf(emptyList())
            }

            MetisFilter.values().forEach { mf ->
                MetisFilterChip(
                    modifier =Modifier,
                    metisFilter = mf,
                    selectedFilters = filter,
                    onSelectFilter = { selectedFilter, isSelected ->
                        filter = if (isSelected) {
                            filter + selectedFilter
                        } else {
                            filter.filterNot { it == selectedFilter }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CourseWideContextFilterChip(
    modifier: Modifier,
    courseWideContext: CourseWideContext,
    selectedCourseWideContext: CourseWideContext?,
    onSelect: (CourseWideContext?) -> Unit
) {
    val icon = when (courseWideContext) {
        CourseWideContext.TECH_SUPPORT -> Icons.Default.Support
        CourseWideContext.ORGANIZATION -> Icons.Default.CalendarMonth
        CourseWideContext.RANDOM -> Icons.Default.QuestionMark
        CourseWideContext.ANNOUNCEMENT -> Icons.Default.Campaign
    }

    val text = stringResource(
        id = when (courseWideContext) {
            CourseWideContext.TECH_SUPPORT -> R.string.course_wide_context_tech_support
            CourseWideContext.ORGANIZATION -> R.string.course_wide_context_organization
            CourseWideContext.RANDOM -> R.string.course_wide_context_random
            CourseWideContext.ANNOUNCEMENT -> R.string.course_wide_context_announcement
        }
    )

    val isSelected = courseWideContext == selectedCourseWideContext
    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = {
            if (isSelected) {
                onSelect(null)
            } else {
                onSelect(courseWideContext)
            }
        },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        label = {
            Text(text = text)
        }
    )
}

@Composable
private fun MetisFilterChip(
    modifier: Modifier,
    metisFilter: MetisFilter,
    selectedFilters: List<MetisFilter>,
    onSelectFilter: (MetisFilter, Boolean) -> Unit
) {
    val icon = when (metisFilter) {
        MetisFilter.CREATED_BY_CLIENT -> Icons.Default.Smartphone
        MetisFilter.WITH_REACTION -> Icons.Default.Reply
        MetisFilter.RESOLVED -> Icons.Default.Done
    }

    val text = stringResource(
        id = when (metisFilter) {
            MetisFilter.CREATED_BY_CLIENT -> R.string.metis_filter_client_created
            MetisFilter.WITH_REACTION -> R.string.metis_filter_with_reaction
            MetisFilter.RESOLVED -> R.string.metis_filter_resolved
        }
    )

    val isSelected = metisFilter in selectedFilters
    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = {
            if (isSelected) {
                onSelectFilter(metisFilter, false)
            } else {
                onSelectFilter(metisFilter, true)
            }
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        },
        label = {
            Text(text = text)
        }
    )
}

private class MetisContextProvider : PreviewParameterProvider<MetisContext> {
    override val values: Sequence<MetisContext> = sequenceOf(
        MetisContext.Course(0),
        MetisContext.Exercise(0, 0),
        MetisContext.Lecture(0, 0)
    )
}

@Preview
@Composable
private fun MetisFilterHeaderPreview(
    @PreviewParameter(provider = MetisContextProvider::class) metisContext: MetisContext
) {
    MetisFilterHeader(modifier = Modifier.fillMaxWidth(), context = metisContext)
}