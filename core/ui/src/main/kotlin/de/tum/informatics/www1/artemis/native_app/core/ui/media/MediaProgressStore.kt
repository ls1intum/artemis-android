package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context

/** Local study position only; it never updates server completion. Keys contain no tokens or URLs. */
class MediaProgressStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("lecture_media_progress", Context.MODE_PRIVATE)
    fun read(key: String): Long? = if (preferences.contains(key)) preferences.getLong(key, 0) else null
    fun save(key: String, position: Long) {
        preferences.edit().putLong(key, position.coerceAtLeast(0)).apply()
    }
}
