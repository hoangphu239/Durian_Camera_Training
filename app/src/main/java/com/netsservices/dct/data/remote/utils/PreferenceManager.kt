package com.netsservices.dct.data.remote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.domain.model.DeviceInfo
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.utils.JsonUtil

object PreferenceManager {

    private const val PREF_NAME = "app_prefs"
    private const val AUTH_TOKEN = "auth_token"
    private const val USER_ID = "user_id"
    private const val ACTION = "action"
    private const val SITE = "site"
    private const val DEVICE_INFO = "device_info"
    private const val DURIAN_TYPE = "durian_type"
    private const val SCAN_MODE = "scan_mode"

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

    fun saveScanMode(context: Context, mode: ScanMode) {
        getPrefs(context).edit {
            putString(SCAN_MODE, mode.name)
        }
    }

    fun getScanMode(context: Context): ScanMode? {
        val value = getPrefs(context).getString(SCAN_MODE, null)
        return value?.let { ScanMode.valueOf(it) }
    }

    fun clearScanMode(context: Context) {
        getPrefs(context).edit { remove(SCAN_MODE) }
    }

    fun clearDurianVariety(context: Context) {
        getPrefs(context).edit { remove(DURIAN_TYPE) }
    }

    fun saveAction(context: Context, action: String) {
        getPrefs(context).edit { putString(ACTION, action) }
    }

    fun getAction(context: Context): String {
        return getPrefs(context).getString(ACTION, "") ?: ""
    }

    fun saveDeviceInfo(context: Context, device: DeviceInfo) {
        val json = JsonUtil.toJson(device)
        getPrefs(context).edit { putString(DEVICE_INFO, json) }
    }

    fun getDeviceInfo(context: Context): DeviceInfo? {
        val json = getPrefs(context).getString(DEVICE_INFO, null) ?: return null
        return JsonUtil.fromJson<DeviceInfo>(json)
    }

    fun clearAction(context: Context) {
        getPrefs(context).edit { remove(ACTION) }
    }

    fun clearData(context: Context) {
        getPrefs(context).edit { putString(AUTH_TOKEN, "") }
        getPrefs(context).edit { putString(USER_ID, "") }
    }
}