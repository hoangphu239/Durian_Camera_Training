package com.netsservices.dct.presentation.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


object LanguagePrefs {

    private const val DATASTORE_NAME = "settings"

    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

    private val LANGUAGE_ID = stringPreferencesKey("language_id")

    // prefix cho translations
    private const val TRANSLATION_PREFIX = "translation_"

    fun getLanguageId(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE_ID] ?: "en" }

    suspend fun setLanguageId(context: Context, langId: String) {
        context.dataStore.edit { it[LANGUAGE_ID] = langId }
    }

    suspend fun saveTranslations(
        context: Context,
        map: Map<String, String>
    ) {
        val langId = getLanguageId(context).first()
        context.dataStore.edit { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith("${TRANSLATION_PREFIX}${langId}_") }
                .forEach { prefs.remove(it) }

            map.forEach { (key, value) ->
                prefs[stringPreferencesKey("${TRANSLATION_PREFIX}${langId}_$key")] = value
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTranslations(context: Context): Flow<Map<String, String>> =
        getLanguageId(context).flatMapLatest { langId ->
            context.dataStore.data.map { prefs ->
                prefs.asMap()
                    .filterKeys { it.name.startsWith("${TRANSLATION_PREFIX}${langId}_") }
                    .mapKeys {
                        it.key.name.removePrefix("${TRANSLATION_PREFIX}${langId}_")
                    }
                    .mapValues { it.value as String }
            }
        }
}
