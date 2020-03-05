package com.wwt.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceProvider(private val context: Context) {

    fun saveData(key: String, name: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(key, name)
        editor.apply()
    }

    fun getData(key: String): String? {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getString(key, "")
    }

    fun clear(key: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun shouldPromptForLocationPermission(): Boolean {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(NEEDS_LOCATION_PROMPT, false)
    }

    fun savePromptForLocationPermission(showAppSettingsDialog: Boolean) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        sharedPref.edit()?.putBoolean(NEEDS_LOCATION_PROMPT, showAppSettingsDialog)?.apply()
    }

    fun saveStringData(key: String, name: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(key, name)
        editor.apply()
    }

    fun getStringData(key: String): String {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getString(key, "") ?: ""
    }

    fun saveBooleanData(key: String, name: Boolean) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(key, name)
        editor.apply()
    }

    fun getBooleanData(key: String): Boolean {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(key, false)
    }

    fun saveIntData(key: String, value: Int) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getIntData(key: String): Int {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getInt(key, 0)
    }

    fun saveLongData(key: String, value: Long) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLongData(key: String): Long {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPref.getLong(key, 0)
    }

    fun clearAllData() {
        val sharedPref: SharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val SHARED_PREF_KEY = "sharedPrefKey"
        private const val NEEDS_LOCATION_PROMPT = "NEEDS_LOCATION_PROMPT"

        const val USER_DATA_ID = "USER_ID"
        const val USER_DATA_EMAIL = "email"
        const val USER_IMAGE_URL = "image_url"
        const val USER_DATA_FAVORITES_HYDERABAD = "USER_DATA_FAVORITES_HYDERABAD"
        const val USER_DATA_FAVORITES_DENVER = "USER_DATA_FAVORITES_DENVER"
        const val IS_OFFLINE = "isOffline"
        const val BUILDING_ID = "BuildingId"
        const val IS_UPDATE_FOR_LOGIN = "IS_UPDATE_FOR_LOGIN"
        const val LOCAL_IMAGE_URL = "LOCAL_IMAGE_URL"
        const val SAVED_TEAM_MEMBERS_INFO = "SAVED_TEAM_MEMBERS_INFO"
        const val DISABLE_VIEW = "DISABLE_VIEW"
    }
}

object SharedPreferenceFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var sharedPreferenceProvider: SharedPreferenceProvider
}