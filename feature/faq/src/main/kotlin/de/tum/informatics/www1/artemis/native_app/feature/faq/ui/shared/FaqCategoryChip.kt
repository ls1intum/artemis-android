package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.BackgroundColorBasedTextColor
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory


@Composable
internal fun FaqCategoryChipRow(
    modifier: Modifier = Modifier,
    categories: Iterable<FaqCategory>
) {
    SelectableFaqCategoryChipRow(
        modifier = modifier,
        selectableFaqCategories = categories.map {
            SelectableFaqCategory(
                category = it,
                selectionConfig = FaqCategoryChipSelectionConfig.Disabled
            )
        }
    )
}


@Composable
internal fun SelectableFaqCategoryChipRow(
    modifier: Modifier = Modifier,
    selectableFaqCategories: Iterable<SelectableFaqCategory>
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        selectableFaqCategories.forEach {
            FaqCategoryChip(
                faqCategory = it.category,
                selectionConfig = it.selectionConfig
            )
        }
    }
}


@Composable
internal fun FaqCategoryChip(
    modifier: Modifier = Modifier,
    faqCategory: FaqCategory,
    selectionConfig: FaqCategoryChipSelectionConfig = FaqCategoryChipSelectionConfig.Disabled
) {
    val chipColor = Color(faqCategory.color.toArgb())
    val textColor = BackgroundColorBasedTextColor.of(chipColor)

    val enabledConfig = selectionConfig as? FaqCategoryChipSelectionConfig.Enabled

    FilterChip(
        modifier = modifier,
        enabled = enabledConfig != null,
        selected = enabledConfig?.isSelected ?: false,
        onClick = enabledConfig?.onClick ?: {},
        colors = FilterChipDefaults.filterChipColors().copy(
            disabledContainerColor = chipColor,
            disabledLabelColor = textColor,
        ),
        leadingIcon = {
            if (enabledConfig?.isSelected ?: false) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        label = {
            Text(
                modifier = Modifier.padding(4.dp),
                text = faqCategory.name,
            )
        }
    )
}


@Preview(showBackground = true, widthDp = 380, heightDp = 700)
@Composable
private fun FaqCategoryChipPreview() {
    MaterialTheme {
        FaqCategoryChip(
            faqCategory = FaqCategory(
                color = android.graphics.Color.valueOf(0xFFFFAA22),
                name = "Category"
            )
        )
    }
}