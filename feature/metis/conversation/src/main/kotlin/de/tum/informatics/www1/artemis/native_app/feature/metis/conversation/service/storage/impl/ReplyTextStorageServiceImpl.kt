package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class ReplyTextStorageServiceImpl(private val context: Context) : ReplyTextStorageService {

    private companion object {
        private const val DATA_STORE_NAME = "ReplyTextStorage"
    }

    private val Context.dataStore by preferencesDataStore(DATA_STORE_NAME)

    override suspend fun getStoredReplyText(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?
    ): String = context
        .dataStore
        .data
        .map { it[getKey(serverHost, courseId, conversationId, postId)].orEmpty() }
        .first()

    override suspend fun updateStoredReplyText(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?,
        text: String
    ) {
        context.dataStore.edit { data ->
            data[getKey(serverHost, courseId, conversationId, postId)] = text
        }
    }

    private fun getKey(
        serverHost: String,
        courseId: Long,
        conversationId: Long,
        postId: Long?
    ): Preferences.Key<String> =
        stringPreferencesKey("$serverHost|$courseId|$conversationId|$postId")
}