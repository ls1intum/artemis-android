package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqToFaqCategoryCrossRef
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqWithFaqCategoriesPojo

@Dao
interface FaqDao {

    @Insert
    suspend fun insert(faq: FaqEntity): Long

    @Update
    suspend fun update(faq: FaqEntity)

    @Transaction
    @Query("SELECT * FROM faq WHERE course_local_id = :courseLocalId AND id = :faqId")
    suspend fun getById(courseLocalId: Long, faqId: Long): FaqWithFaqCategoriesPojo?

    @Transaction
    @Query("SELECT * FROM faq WHERE course_local_id = :courseLocalId")
    suspend fun getAll(courseLocalId: Long): List<FaqWithFaqCategoriesPojo>

    // Categories

    @Insert
    suspend fun insertCategory(category: FaqCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: FaqCategoryEntity)

    @Query("SELECT * FROM faq_category WHERE course_local_id = :courseLocalId AND name = :name")
    suspend fun getCategoryByName(courseLocalId: Long, name: String): FaqCategoryEntity?


    // CrossRefs

    @Upsert
    suspend fun upsertCrossRef(crossRef: FaqToFaqCategoryCrossRef)

}