package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.SurveyHintService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate


private val SURVEY_START_DATE = LocalDate.of(2025, 4, 21)
private val SURVEY_END_DATE = LocalDate.of(2025, 5, 11)

class SurveyHintServiceImpl(
    private val context: Context,
) : SurveyHintService {

    private companion object {
        private const val DATA_STORE_KEY = "survey_hint_store"

        private val KEY_SHOW_SURVEY = booleanPreferencesKey("showSurvey2")
    }

    private val Context.storage by preferencesDataStore(DATA_STORE_KEY)

    override val shouldShowSurveyHint: Flow<Boolean> = context.storage.data
        .map { it[KEY_SHOW_SURVEY] ?: isSurveyActive() }

    private fun isSurveyActive(): Boolean {
        val currentDate = LocalDate.now()
        return currentDate in SURVEY_START_DATE..SURVEY_END_DATE
    }

    override suspend fun dismissSurveyHintPermanently() {
        context.storage.edit { data ->
            data[KEY_SHOW_SURVEY] = false
        }
    }
}