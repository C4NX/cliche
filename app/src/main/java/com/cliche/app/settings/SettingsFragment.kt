package com.cliche.app.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.cliche.app.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}