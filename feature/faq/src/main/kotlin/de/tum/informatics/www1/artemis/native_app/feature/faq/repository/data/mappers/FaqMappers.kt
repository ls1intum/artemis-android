package de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers

import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqState
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.remote.FaqDto


internal fun FaqDto.toFaq() = Faq(
    id = id,
    questionTitle = questionTitle,
    questionAnswer = questionAnswer,
    categories = listOf(),  // TODO: Ignored for now
    faqState = FaqState.valueOf(faqState)
)