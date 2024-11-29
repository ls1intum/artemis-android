package de.tum.informatics.www1.artemis.native_app.core.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class KSerializableNavType<T>(
    isNullableAllowed: Boolean,
    private val serializer: KSerializer<T>
) : NavType<T>(isNullableAllowed) {

    companion object {
        private val json = Json {
            coerceInputValues = true
        }
    }

    override fun get(bundle: Bundle, key: String): T? {
        return parseValue(bundle.getString(key) ?: return null)
    }

    override fun parseValue(value: String): T {
        return json.decodeFromString(serializer, value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(serializer, value))
    }
}
