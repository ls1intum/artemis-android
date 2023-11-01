package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal val LocalReplyAutoCompleteHintProvider: ProvidableCompositionLocal<ReplyAutoCompleteHintProvider> = compositionLocalOf {
    object : ReplyAutoCompleteHintProvider {
        override val legalTagChars: List<Char> = emptyList()

        override fun produceAutoCompleteHints(
            tagChar: Char,
            query: String
        ): Flow<DataState<List<AutoCompleteCategory>>> = flowOf(DataState.Success(emptyList()))
    }
}

internal interface ReplyAutoCompleteHintProvider {

    val legalTagChars: List<Char>

    fun produceAutoCompleteHints(
        tagChar: Char,
        query: String
    ): Flow<DataState<List<AutoCompleteCategory>>>
}
