package com.netsservices.dct.data.remote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.presentation.utils.JsonUtil

object PreferenceManager {

    private const val PREF_NAME = "app_prefs"
    private const val AUTH_TOKEN = "auth_token"
    private const val USER_ID = "user_id"
    private const val SITE = "site"
    private const val DURIAN_TYPE = "durian_type"
    private const val FRINGERPRINT_STATUS = "fringerprint_status"

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

    fun saveSite(context: Context, site: Site) {
        val json = JsonUtil.toJson(site)
        getPrefs(context).edit { putString(SITE, json) }
    }

    fun getSite(context: Context): Site? {
        val json = getPrefs(context).getString(SITE, null) ?: return null
        return JsonUtil.fromJson<Site>(json)
    }

    fun saveDurianVariety(context: Context, variety: DurianItem) {
        val json = JsonUtil.toJson(variety)
        getPrefs(context).edit { putString(DURIAN_TYPE, json) }
    }

    fun getDurianVariety(context: Context): DurianItem? {
        val json = getPrefs(context).getString(DURIAN_TYPE, null) ?: return null
        return JsonUtil.fromJson<DurianItem>(json)
    }

    fun saveFingerprintStatus(context: Context, isEnable: Boolean) {
        getPrefs(context).edit { putBoolean(FRINGERPRINT_STATUS, isEnable) }
    }

    fun getFingerprintStatus(context: Context): Boolean {
        return getPrefs(context).getBoolean(FRINGERPRINT_STATUS, false)
    }
}