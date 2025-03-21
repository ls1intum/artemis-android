package de.tum.informatics.www1.artemis.native_app.feature.faq.repository

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import kotlinx.coroutines.flow.Flow

interface FaqRepository {

    fun getFaqs(
        courseId: Long,
    ): Flow<DataState<List<Faq>>>

    fun getFaq(
        courseId: Long,
        faqId: Long,
    ): Flow<DataState<Faq>>
}