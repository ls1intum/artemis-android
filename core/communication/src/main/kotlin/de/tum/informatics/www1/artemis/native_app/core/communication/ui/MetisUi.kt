package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SmartphoneMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }
    val courseWideContext = viewModel.courseWideContext.collectAsState(initial = null).value
    val metisFilter = viewModel.filter.collectAsState(initial = emptyList()).value
    val sortingStrategy = viewModel
        .sortingStrategy
        .collectAsState(initial = MetisSortingStrategy.DATE_DESCENDING)
        .value

    Column(modifier = modifier) {
        MetisFilterHeader(
            modifier = Modifier.fillMaxWidth(),
            context = metisContext,
            courseWideContext = courseWideContext,
            metisFilter = metisFilter,
            metisSortingStrategy = sortingStrategy,
            selectCourseWideContext = viewModel::updateCourseWideContext,
            onSelectFilter = { selectedFilter, isSelected ->
                if (isSelected) {
                    viewModel.addMetisFilter(selectedFilter)
                } else {
                    viewModel.removeMetisFilter(selectedFilter)
                }
            },
            onChangeMetisSortingStrategy = viewModel::updateSortingStrategy
        )

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
private fun MetisFilterHeader(
    modifier: Modifier,
    context: MetisContext,
    courseWideContext: CourseWideContext?,
    metisFilter: List<MetisFilter>,
    metisSortingStrategy: MetisSortingStrategy,
    selectCourseWideContext: (CourseWideContext?) -> Unit,
    onSelectFilter: (MetisFilter, Boolean) -> Unit,
    onChangeMetisSortingStrategy: (MetisSortingStrategy) -> Unit
) {
    @Suppress("LocalVariableName")
    val MetisFilterRow: @Composable (Modifier) -> Unit =
        @Composable { filterRowModifier ->
            FilterChipRow(
                modifier = filterRowModifier,
                filterRowType = FilterRowType.MetisFilterRowType(
                    selectedFilter = metisFilter,
                    onSelectFilter = onSelectFilter
                )
            )
        }

    Column(modifier = modifier) {
        var displayTuning by rememberSaveable(context) {
            mutableStateOf(false)
        }

        Row(modifier = modifier) {
            if (context is MetisContext.Course) {
                FilterChipRow(
                    modifier = Modifier.weight(1f),
                    filterRowType = FilterRowType.CourseWideContextFilterRowType(
                        currentCourseWideContext = courseWideContext,
                        selectCourseWideContext = selectCourseWideContext
                    )
                )

                AnimatedVisibility(
                    visible = !displayTuning
                ) {
                    IconButton(onClick = { displayTuning = true }) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null)
                    }
                }
            } else {
                MetisFilterRow(Modifier.weight(1f))
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            }
        }

        AnimatedVisibility(visible = displayTuning) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.configuration_card_title),
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            style = MaterialTheme.typography.headlineSmall
                        )

                        IconButton(
                            onClick = { displayTuning = false }
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Column(modifier = Modifier.padding(8.dp)) {
                        if (context is MetisContext.Course) {
                            MetisFilterRow(Modifier.fillMaxWidth())
                        }

                        SelectMetisSortingStrategy(
                            modifier = Modifier.fillMaxWidth(),
                            currentSortingStrategy = metisSortingStrategy,
                            onSelectSortingStrategy = onChangeMetisSortingStrategy
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a row of filter chips depending on the context.
 */
@Composable
private fun FilterChipRow(modifier: Modifier, filterRowType: FilterRowType) {
    Row(
        modifier = modifier.then(Modifier.horizontalScroll(rememberScrollState())),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (filterRowType) {
            is FilterRowType.CourseWideContextFilterRowType -> {
                CourseWideContext.values().forEach { cwc ->
                    CourseWideContextFilterChip(
                        modifier = Modifier,
                        courseWideContext = cwc,
                        selectedCourseWideContext = filterRowType.currentCourseWideContext,
                        onSelect = filterRowType.selectCourseWideContext
                    )
                }
            }

            is FilterRowType.MetisFilterRowType -> {
                MetisFilter.values().forEach { mf ->
                    MetisFilterChip(
                        modifier = Modifier,
                        metisFilter = mf,
                        selectedFilters = filterRowType.selectedFilter,
                        onSelectFilter = filterRowType.onSelectFilter
                    )
                }
            }
        }
    }
}

sealed class FilterRowType {
    class CourseWideContextFilterRowType(
        val currentCourseWideContext: CourseWideContext?,
        val selectCourseWideContext: (CourseWideContext?) -> Unit
    ) : FilterRowType()

    class MetisFilterRowType(
        val selectedFilter: List<MetisFilter>,
        val onSelectFilter: (MetisFilter, Boolean) -> Unit
    ) : FilterRowType()
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

@Composable
private fun SelectMetisSortingStrategy(
    modifier: Modifier,
    currentSortingStrategy: MetisSortingStrategy,
    onSelectSortingStrategy: (MetisSortingStrategy) -> Unit
) {
    val selectedCriteria: SortingCriteria = when (currentSortingStrategy) {
        MetisSortingStrategy.DATE_ASCENDING, MetisSortingStrategy.DATE_DESCENDING -> SortingCriteria.DATE
        MetisSortingStrategy.REPLIES_ASCENDING, MetisSortingStrategy.REPLIES_DESCENDING -> SortingCriteria.REPLIES
        MetisSortingStrategy.VOTES_ASCENDING, MetisSortingStrategy.VOTES_DESCENDING -> SortingCriteria.VOTES

    }

    val sortingOrder: SortingOrder = when (currentSortingStrategy) {
        MetisSortingStrategy.DATE_ASCENDING, MetisSortingStrategy.REPLIES_ASCENDING, MetisSortingStrategy.VOTES_ASCENDING -> SortingOrder.ASCENDING
        MetisSortingStrategy.DATE_DESCENDING, MetisSortingStrategy.REPLIES_DESCENDING, MetisSortingStrategy.VOTES_DESCENDING -> SortingOrder.DESCENDING
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(id = R.string.metis_sorting_strategy_criteria_title))

        SortingStrategyField(
            modifier = Modifier,
            text = stringResource(id = selectedCriteria.textRes),
            icon = selectedCriteria.icon
        ) {
            SortingCriteria.values().forEach { sortingCriteria ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = sortingCriteria.textRes)) },
                    leadingIcon = {
                        Icon(
                            imageVector = sortingCriteria.icon,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSelectSortingStrategy(
                            constructSortingStrategy(
                                sortingCriteria,
                                sortingOrder
                            )
                        )
                    }
                )
            }
        }

        Text(text = stringResource(id = R.string.metis_sorting_strategy_order_title))

        SortingStrategyField(
            modifier = Modifier,
            text = stringResource(id = sortingOrder.textRes),
            icon = sortingOrder.icon
        ) {
            SortingOrder.values().forEach { order ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = order.textRes)) },
                    leadingIcon = { Icon(imageVector = order.icon, contentDescription = null) },
                    onClick = {
                        onSelectSortingStrategy(
                            constructSortingStrategy(
                                selectedCriteria,
                                order
                            )
                        )
                    }
                )
            }
        }
    }
}

/**
 * Display a clickable text with button that shows a dialog on click.
 */
@Composable
private fun SortingStrategyField(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    dialogContent: @Composable ColumnScope.() -> Unit
) {
    var displayDialog by remember {
        mutableStateOf(false)
    }

    OutlinedButton(modifier = modifier, onClick = { displayDialog = true }) {
        var displayText by remember { mutableStateOf(true) }

        if (displayText) {
            Text(
                text = text,
                onTextLayout = {
                    if (it.hasVisualOverflow) {
                        displayText = false
                    }
                },
                maxLines = 1
            )
        } else {
            Icon(imageVector = icon, contentDescription = null)
        }

        Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)

        DropdownMenu(expanded = displayDialog, onDismissRequest = { displayDialog = false }) {
            dialogContent()
        }
    }
}

private enum class SortingCriteria(val textRes: Int, val icon: ImageVector) {
    DATE(R.string.metis_sorting_strategy_criteria_date, Icons.Default.History),
    REPLIES(R.string.metis_sorting_strategy_criteria_replies, Icons.Default.Comment),
    VOTES(R.string.metis_sorting_strategy_criteria_votes, Icons.Default.Recommend)
}

private enum class SortingOrder(val textRes: Int, val icon: ImageVector) {
    DESCENDING(R.string.metis_sorting_strategy_order_descending, Icons.Default.ArrowDownward),
    ASCENDING(R.string.metis_sorting_strategy_order_ascending, Icons.Default.ArrowUpward),
}

private fun constructSortingStrategy(
    sortingCriteria: SortingCriteria,
    sortingOrder: SortingOrder
): MetisSortingStrategy {
    return when (sortingCriteria) {
        SortingCriteria.DATE -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.DATE_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.DATE_ASCENDING
            }
        }

        SortingCriteria.REPLIES -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.REPLIES_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.REPLIES_ASCENDING
            }
        }

        SortingCriteria.VOTES -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.VOTES_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.VOTES_ASCENDING
            }
        }
    }
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
    var courseWideContext: CourseWideContext? by remember { mutableStateOf(null) }
    var filter: List<MetisFilter> by remember { mutableStateOf(emptyList()) }
    var sortingStrategy: MetisSortingStrategy by remember {
        mutableStateOf(MetisSortingStrategy.DATE_DESCENDING)
    }

    MetisFilterHeader(
        modifier = Modifier.fillMaxWidth(),
        context = metisContext,
        courseWideContext = courseWideContext,
        metisFilter = filter,
        selectCourseWideContext = { new -> courseWideContext = new },
        onSelectFilter = { f, selected ->
            filter = if (selected) {
                filter + f
            } else {
                filter.filterNot { it == f }
            }
        },
        metisSortingStrategy = sortingStrategy,
        onChangeMetisSortingStrategy = { sortingStrategy = it }
    )
}

@Preview
@Composable
private fun SelectMetisSortingStrategyPreview() {
    var current: MetisSortingStrategy by remember { mutableStateOf(MetisSortingStrategy.DATE_DESCENDING) }

    SelectMetisSortingStrategy(
        modifier = Modifier.fillMaxWidth(),
        currentSortingStrategy = current,
        onSelectSortingStrategy = { current = it }
    )
}