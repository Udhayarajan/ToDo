package com.mugames.todo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.mugames.todo.data.AppDatabase
import java.util.*

class ToDoApplication : Application() {
    companion object {
        fun localeContext(context: Context): Context {
            val localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.key_lang), Locale.getDefault().language)
            localeCode?.let {
                val locale = Locale(localeCode)
                Locale.setDefault(locale)
                return context.createConfigurationContext(Configuration().apply {
                    setLocale(locale)
                })
            }
            return context
        }

        fun setAppTheme(context: Context) {
            context.apply {
                val themes = resources.getStringArray(R.array.theme_values)
                val currentTheme = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.key_theme), themes[0])
                val mode = when (currentTheme) {
                    themes[1] -> AppCompatDelegate.MODE_NIGHT_NO
                    themes[2] -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let { localeContext(it) })
    }
}