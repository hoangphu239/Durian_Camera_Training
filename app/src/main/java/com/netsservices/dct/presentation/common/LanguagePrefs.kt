package com.netsservices.dct.presentation.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object LanguagePrefs {
    private const val DATASTORE_NAME = "settings"
    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    private val TRANSLATION_EN = stringPreferencesKey("translation_en")
    private val TRANSLATION_VI = stringPreferencesKey("translation_vi")
    private val TRANSLATION_LO = stringPreferencesKey("translation_lo")
    private val TRANSLATION_TH = stringPreferencesKey("translation_th")
    private val gson = Gson()


    fun getLanguage(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }

    suspend fun setLanguage(context: Context, lang: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    suspend fun saveTranslations(
        context: Context,
        map: Map<String, String>
    ) {
        val json = gson.toJson(map)
        context.dataStore.edit {
            it[TRANSLATION_EN] = json
        }
    }

    fun getTranslations(context: Context): Flow<Map<String, String>> =
        context.dataStore.data.map { prefs ->
            val json = prefs[TRANSLATION_EN] ?: return@map emptyMap()

            try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()
            } catch (_: Exception) {
                emptyMap()
            }
        }
}
