package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class FaqWithFaqCategoriesPojo(
    @Embedded val faq: FaqEntity,
    @Relation(
        parentColumn = "client_side_id",
        entityColumn = "faq_id",
        associateBy = Junction(FaqToFaqCategoryCrossRef::class)
    )
    val faqCategories: List<FaqCategoryEntity>
) {
}