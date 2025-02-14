package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.BackgroundColorBasedTextColor
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory


@Composable
fun FaqCategoryChipRow(
    modifier: Modifier = Modifier,
    categories: Iterable<FaqCategory>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach {
            FaqCategoryChip(
                faqCategory = it
            )
        }
    }
}


@Composable
fun FaqCategoryChip(
    modifier: Modifier = Modifier,
    faqCategory: FaqCategory,
) {
    val chipColor = Color(faqCategory.color.toArgb())
    val textColor = BackgroundColorBasedTextColor.of(chipColor)

    Surface(
        modifier = modifier,
        color = chipColor,
        shape = MaterialTheme.shapes.small
    ){
        Text(
            modifier = Modifier.padding(4.dp),
            text = faqCategory.name,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
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