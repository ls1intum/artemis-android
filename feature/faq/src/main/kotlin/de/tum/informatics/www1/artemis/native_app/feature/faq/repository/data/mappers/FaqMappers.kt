package de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers

import android.graphics.Color
import androidx.core.graphics.toColorInt
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqState
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqWithFaqCategoriesPojo
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


internal fun FaqDto.toFaq() = Faq(
    id = id,
    questionTitle = questionTitle,
    questionAnswer = questionAnswer,
    categories = categories?.map { faqCategoryFromString(it) } ?: emptyList(),
    faqState = FaqState.valueOf(faqState)
)

private fun faqCategoryFromString(string: String): FaqCategory {
    val dto = Json.decodeFromString<FaqCategoryDto>(string)
    val colorInt: Int = dto.color.toColorInt()
    val color: Color = Color.valueOf(colorInt)

    return FaqCategory(
        name = dto.category,
        color = color
    )
}

@Serializable
private data class FaqCategoryDto(
    val color: String,
    val category: String
)


internal fun Faq.toFaqEntity(
    courseClientSideId: Long,
) = FaqEntity(
    id = id,
    courseClientSideId = courseClientSideId,
    question = questionTitle,
    answer = questionAnswer,
    faqState = faqState.toString(),
)

internal fun FaqCategory.toFaqCategoryEntity(courseClientSideId: Long) = FaqCategoryEntity(
    courseClientSideId = courseClientSideId,
    color = color.toString(),
    name = name,
)

internal fun FaqWithFaqCategoriesPojo.toFaq() = Faq(
    id = faq.id,
    questionTitle = faq.question,
    questionAnswer = faq.answer,
    categories = faqCategories.map { it.toFaqCategory() },
    faqState = FaqState.valueOf(faq.faqState)
)

internal fun FaqCategoryEntity.toFaqCategory() = FaqCategory(
    name = name,
    color = Color.valueOf(color.toColorInt())
)