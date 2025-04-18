package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrame
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrameUtils
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrameUtils.toRangeString
import kotlinx.parcelize.Parcelize

const val MAX_NUMBER_OF_ITEMS_WITHOUT_SUB_LABEL = 5

@Composable
internal fun <T> TimeFrameItemsLazyColumn(
    modifier: Modifier,
    timeFrameGroup: List<TimeFrame<T>>,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    query: String,
    getItemId: T.() -> Long,
    itemContent: @Composable (Modifier, T) -> Unit
) {
    val timeFrameGroupExpandedState: MutableMap<String, Boolean> = rememberSaveable(
        timeFrameGroup,
        saver = TimeFrameItemsSectionExpandedSaver
    ) {
        SnapshotStateMap<String, Boolean>().apply {
            timeFrameGroup.forEach { group ->
                val defaultExpanded = when (group) {
                    is TimeFrame.Current -> true
                    is TimeFrame.DueSoon -> true
                    else -> false
                }
                this[group.key] = defaultExpanded
            }
        }
    }

    LaunchedEffect(query, timeFrameGroup) {
        if (query.isNotBlank()) {
            timeFrameGroup.forEach { frame ->
                if (frame.items.isNotEmpty()) {
                    timeFrameGroupExpandedState[frame.key] = true
                }
            }
        } else {
            timeFrameGroupExpandedState.forEach { (key, _) ->
                val frame = timeFrameGroup.find { it.key == key } ?: return@forEach
                val defaultExpanded = when (frame) {
                    is TimeFrame.Current -> true
                    is TimeFrame.DueSoon -> true
                    else -> false
                }
                timeFrameGroupExpandedState[key] = defaultExpanded
            }
        }
    }


    LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        contentPadding = Spacings.calculateContentPaddingValues()
    ) {
        timeFrameGroup.forEach { group ->
            val isExpanded = timeFrameGroupExpandedState[group.key] == true
            item {
                TimeFrameItemsSectionHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable {
                            timeFrameGroupExpandedState[group.key] = !isExpanded
                        }
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                    group = group,
                    expanded = isExpanded,
                )
            }

            if (!isExpanded) {
                return@forEach
            }

            if (group !is TimeFrame.NoDate && group.items.size > MAX_NUMBER_OF_ITEMS_WITHOUT_SUB_LABEL) {
                val weeklyGroups = TimeFrameUtils.groupByWeek(
                    items = group.items
                )

                weeklyGroups.forEach { weeklyGroup ->
                    item {
                        // Sub-header: e.g. "21 Oct 2024 - 27 Oct 2024"
                        Text(
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                                .padding(Spacings.TimeFrameItems.small),
                            text = weeklyGroup.indicator.toRangeString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray
                        )
                    }

                    items(
                        items = weeklyGroup.items,
                        key = getItemId
                    ) { subItem ->
                        itemContent(Modifier.animateItem(), subItem)
                    }
                }
            } else {
                items(group.items, key = getItemId) { item ->
                    itemContent(Modifier.animateItem(), item)
                }
            }
        }
    }
}


/**
 * Display a title with the time frame.
 * Displays an icon button that lets the user expand and collapse the time frame items
 * @param expanded if the item group this is showing is expanded
 */
@Composable
private fun <T> TimeFrameItemsSectionHeader(
    modifier: Modifier = Modifier,
    group: TimeFrame<T>,
    expanded: Boolean
) {
    val size = group.items.size
    var groupTitle = when (group) {
        is TimeFrame.Past<*> -> stringResource(R.string.course_ui_list_time_frame_past)
        is TimeFrame.Current<*> -> stringResource(R.string.course_ui_list_time_frame_current)
        is TimeFrame.Future -> stringResource(R.string.course_ui_list_time_frame_future)
        is TimeFrame.NoDate -> stringResource(R.string.course_ui_list_time_frame_no_date)
        is TimeFrame.DueSoon -> stringResource(R.string.course_ui_list_time_frame_due_soon)
    }

    groupTitle += " ($size)"

    Row(
        modifier = modifier
            .padding(vertical = Spacings.TimeFrameItems.small)
            .testTag("${group.key}-header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacings.TimeFrameItems.small),
            text = groupTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        val contentDescription =
            stringResource(
                id = if (expanded) R.string.course_ui_list_expand_button_less_content_info
                else R.string.course_ui_list_expand_button_more_content_info
            )

        Icon(
            imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = contentDescription,
            modifier = Modifier.size(Spacings.TimeFrameItems.iconSize),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private object TimeFrameItemsSectionExpandedSaver :
    Saver<SnapshotStateMap<String, Boolean>, TimeFrameItemsSectionExpandedSaver.ParcelWrapper> {

    @Parcelize
    data class ParcelWrapper(val map: Map<String, Boolean>) : Parcelable

    override fun restore(value: ParcelWrapper): SnapshotStateMap<String, Boolean> {
        return SnapshotStateMap<String, Boolean>().apply {
            putAll(value.map)
        }
    }

    override fun SaverScope.save(value: SnapshotStateMap<String, Boolean>): ParcelWrapper =
        ParcelWrapper(value)
}
