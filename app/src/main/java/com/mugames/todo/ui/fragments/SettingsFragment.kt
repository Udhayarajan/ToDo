package com.mugames.todo.ui.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.mugames.todo.R

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val themeKey = findPreference<ListPreference>(string(R.string.key_theme))
        val langKey = findPreference<ListPreference>(string(R.string.key_lang))

        themeKey?.setOnPreferenceChangeListener { _, newValue ->
            when(themeKey.findIndexOfValue(newValue.toString())) {
                0 -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
                1 -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
//            requireActivity().recreate()
            true
        }
        langKey?.setOnPreferenceChangeListener { _, _ ->
            requireActivity().recreate()
            true
        }
    }

    private fun string(id: Int): String = requireContext().getString(id)
}