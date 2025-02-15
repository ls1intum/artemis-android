package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqToFaqCategoryCrossRef
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqWithFaqCategoriesPojo

@Dao
interface FaqDao {

    @Insert
    suspend fun insert(faq: FaqEntity): FaqEntity

    @Update
    suspend fun update(faq: FaqEntity): FaqEntity

    @Query("SELECT * FROM faq WHERE course_client_side_id = :courseId AND id = :faqId")
    suspend fun getById(courseId: Long, faqId: Long): FaqWithFaqCategoriesPojo?

    @Query("SELECT * FROM faq WHERE course_client_side_id = :courseId")
    suspend fun getAll(courseId: Long): List<FaqWithFaqCategoriesPojo>

    // Categories

    @Insert
    suspend fun insertCategory(category: FaqCategoryEntity): FaqCategoryEntity

    @Update
    suspend fun updateCategory(category: FaqCategoryEntity): FaqCategoryEntity

    @Query("SELECT * FROM faq_category WHERE course_client_side_id = :courseId AND name = :name")
    suspend fun getCategoryByName(courseId: Long, name: String): FaqCategoryEntity?


    // CrossRefs

    @Upsert
    suspend fun upsertCrossRef(crossRef: FaqToFaqCategoryCrossRef): FaqToFaqCategoryCrossRef

}