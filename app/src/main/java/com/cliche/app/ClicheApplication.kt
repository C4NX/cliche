package com.cliche.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ClicheApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    private fun applyTheme() {
        // Get Preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themeValue = prefs.getString("theme", "system")

        // Apply Theme
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}