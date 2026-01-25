package com.cliche.app.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cliche.app.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val themePreference: ListPreference? = findPreference("theme")
        val languagePreference: ListPreference? = findPreference("language")

        // When preferences change, apply theme immediately
        themePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                when (newValue as String) {
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }

        languagePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                when (newValue as String) {
                    "en" -> {
                        val appLocale: LocaleListCompat = LocaleListCompat.create(Locale.forLanguageTag("en"))
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                    "fr" -> {
                        val appLocale: LocaleListCompat = LocaleListCompat.create(Locale.forLanguageTag("fr"))
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                    else -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
                true
            }
    }
}