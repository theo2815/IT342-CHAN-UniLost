package com.hulampay.mobile.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hulampay.mobile.ui.theme.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_preference")
    }

    val theme: Flow<ThemePreference> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[THEME_KEY] ?: return@map ThemePreference.SYSTEM
        runCatching { ThemePreference.valueOf(raw) }.getOrDefault(ThemePreference.SYSTEM)
    }

    suspend fun setTheme(value: ThemePreference) {
        context.themeDataStore.edit { it[THEME_KEY] = value.name }
    }
}
