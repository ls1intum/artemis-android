package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "faq_to_faq_category",
    primaryKeys = ["local_faq_id", "local_faq_category_id"],
    foreignKeys = [
        ForeignKey(
            entity = FaqEntity::class,
            parentColumns = ["local_faq_id"],
            childColumns = ["local_faq_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FaqCategoryEntity::class,
            parentColumns = ["local_faq_category_id"],
            childColumns = ["local_faq_category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FaqToFaqCategoryCrossRef(
    @ColumnInfo(name = "local_faq_id")
    val faqId: Long,
    @ColumnInfo(name = "local_faq_category_id")
    val faqCategoryId: Long
)