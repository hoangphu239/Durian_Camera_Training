package com.netsservices.dct.presentation.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object LanguagePrefs {
    private const val DATASTORE_NAME = "settings"
    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    fun getLanguage(context: Context): Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "en" }

    suspend fun setLanguage(context: Context, lang: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    fun getLanguageNow(context: Context): String {
        return runBlocking {
            getLanguage(context).first()
        }
    }
}
