package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.StoredReplyDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

// All service instances share DataStore's single owner for this file.
private val Context.replyTextDataStore by preferencesDataStore("ReplyTextStorage")

internal class ReplyTextStorageServiceImpl(private val context: Context) : ReplyTextStorageService {
    override suspend fun getStoredReplyText(serverHost: String, courseId: Long, conversationId: Long, postId: Long?): String =
        observeStoredReplyDraft(serverHost, courseId, conversationId, postId).first().text

    override fun observeStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?): Flow<StoredReplyDraft> {
        val key = getKey(serverHost, courseId, conversationId, postId)
        return context.replyTextDataStore.data.map { it.draft(key) }.distinctUntilChanged()
    }

    override suspend fun updateStoredReplyText(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, text: String) {
        updateStoredReplyDraft(serverHost, courseId, conversationId, postId, StoredReplyDraft(text, UUID.randomUUID().toString()))
    }

    override suspend fun updateStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, draft: StoredReplyDraft) {
        val key = getKey(serverHost, courseId, conversationId, postId)
        context.replyTextDataStore.edit { data ->
            data[key] = draft.text
            data[revisionKey(key)] = draft.revision ?: UUID.randomUUID().toString()
        }
    }

    override suspend fun clearStoredReplyDraftAfterSend(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, sentDraft: StoredReplyDraft): StoredReplyDraft {
        val key = getKey(serverHost, courseId, conversationId, postId)
        val data = context.replyTextDataStore.edit { data ->
            if (data.draft(key) == sentDraft) {
                data[key] = ""
                data[revisionKey(key)] = UUID.randomUUID().toString()
            }
        }
        return data.draft(key)
    }

    private fun Preferences.draft(key: Preferences.Key<String>) = StoredReplyDraft(this[key].orEmpty(), this[revisionKey(key)])

    private fun revisionKey(key: Preferences.Key<String>) = stringPreferencesKey("${key.name}|revision")

    private fun getKey(serverHost: String, courseId: Long, conversationId: Long, postId: Long?): Preferences.Key<String> =
        stringPreferencesKey("$serverHost|$courseId|$conversationId|$postId")
}
