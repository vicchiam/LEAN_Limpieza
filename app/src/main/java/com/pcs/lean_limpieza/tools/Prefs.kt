package com.pcs.lean_limpieza.tools

import android.content.Context
import android.content.SharedPreferences
import com.pcs.lean_limpieza.R

class Prefs (context: Context) {

    private val PREFS_FILENAME = "com.pcs.lean_logistica.prefs"
    private val ID_APP = "id_app"
    private val SETTINGS_URL = "settings_url"
    private val SETTINGS_CENTER = "settings_center"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    private val defaultURL: String = context.resources.getString(R.string.default_url)

    var settingsUrl: String
        get() = prefs.getString(SETTINGS_URL, defaultURL) ?: ""
        set(value) = prefs.edit().putString(SETTINGS_URL, value).apply()

    var settingsCenter: Int
        get() = prefs.getInt(SETTINGS_CENTER, 0)
        set(value) = prefs.edit().putInt(SETTINGS_CENTER, value).apply()

    var idApp: Int
        get() = prefs.getInt(ID_APP,0)
        set(value) = prefs.edit().putInt(ID_APP, value).apply()
}