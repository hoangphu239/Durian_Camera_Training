package com.netsservices.dct.data.remote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {

    private const val PREF_NAME = "app_prefs"
    private const val AUTH_TOKEN = "auth_token"
    private const val USER_ID = "user_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAuthToken(context: Context, token: String) {
        getPrefs(context).edit { putString(AUTH_TOKEN, token) }
    }

    fun getAuthToken(context: Context): String {
        return getPrefs(context).getString(AUTH_TOKEN, "") ?: ""
    }

    fun saveUserId(context: Context, token: String) {
        getPrefs(context).edit { putString(USER_ID, token) }
    }

    fun getUserId(context: Context): String {
        return getPrefs(context).getString(USER_ID, "") ?: ""
    }
}