package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class FaqConfiguration(
    val navigationLevel: Int,
    val prev: FaqConfiguration?
) : Parcelable

@Parcelize
object NothingOpened : FaqConfiguration(0, null)

@Parcelize
data class OpenedFaq(
    val _prev: FaqConfiguration,
    val faqId: Long
) : FaqConfiguration(10, _prev)

fun getInitialFaqConfiguration(
    faqId: Long?
): FaqConfiguration = when {
    faqId != null ->  OpenedFaq(
        faqId = faqId,
        _prev = NothingOpened
    )

    else -> NothingOpened
}